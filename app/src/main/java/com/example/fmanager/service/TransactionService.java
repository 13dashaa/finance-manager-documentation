package com.example.fmanager.service;

import static com.example.fmanager.exception.NotFoundMessages.ACCOUNT_NOT_FOUND_MESSAGE;
import static com.example.fmanager.exception.NotFoundMessages.CATEGORY_NOT_FOUND_MESSAGE;
import static com.example.fmanager.exception.NotFoundMessages.TRANSACTION_NOT_FOUND_MESSAGE;

import com.example.fmanager.dto.TransactionCreateDto;
import com.example.fmanager.dto.TransactionGetDto;
import com.example.fmanager.exception.BudgetLimitExceededException;
import com.example.fmanager.exception.InvalidDataException;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Account;
import com.example.fmanager.models.Budget;
import com.example.fmanager.models.Category;
import com.example.fmanager.models.Transaction;
import com.example.fmanager.repository.AccountRepository;
import com.example.fmanager.repository.BudgetRepository;
import com.example.fmanager.repository.CategoryRepository;
import com.example.fmanager.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final InMemoryCache cache;

    public TransactionService(TransactionRepository transactionsRepository,
                              AccountRepository accountRepository,
                              InMemoryCache cache,
                              CategoryRepository categoryRepository,
                              BudgetRepository budgetRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionsRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.cache = cache;
    }

    public List<TransactionGetDto> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        List<TransactionGetDto> transactionGetDtos = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionGetDtos.add(TransactionGetDto.convertToDto(transaction));
        }
        return transactionGetDtos;
    }

    public List<TransactionGetDto> findByClientIdAndCategoryId(int clientId, int categoryId) {
        String cacheKey = "transactions_client_" + clientId + "_category_" + categoryId;
        if (cache.containsKey(cacheKey)) {
            return (List<TransactionGetDto>) cache.get(cacheKey);
        }
        List<Transaction> transactions = transactionRepository
                .findAllByClientIdAndCategoryId(clientId, categoryId);
        List<TransactionGetDto> transactionGetDtos = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionGetDtos.add(TransactionGetDto.convertToDto(transaction));
        }
        cache.put(cacheKey, transactionGetDtos);
        return transactionGetDtos;
    }

    public void clearCacheForClientAndCategory(int clientId, int categoryId) {
        String cacheKey = "transactions_client_" + clientId + "_category_" + categoryId;
        cache.remove(cacheKey);
    }

    public Optional<TransactionGetDto> getTransactionById(int id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND_MESSAGE));
        return Optional.of(TransactionGetDto.convertToDto(transaction));
    }

    @Transactional
    public Transaction createTransaction(TransactionCreateDto transactionCreateDto) {
        // 1. Создаем и валидируем транзакцию
        Transaction transaction = new Transaction();
        transaction.setDate(transactionCreateDto.getDate());
        transaction.setDescription(transactionCreateDto.getDescription());

        Account account = accountRepository.findById(transactionCreateDto.getAccountId())
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));

        // 2. Проверяем баланс счета (для расходных операций)
        if (account.getBalance() + transactionCreateDto.getAmount() < 0) {
            throw new InvalidDataException("Insufficient funds in the account");
        }

        transaction.setAmount(transactionCreateDto.getAmount());
        account.setBalance(account.getBalance() + transaction.getAmount());
        accountRepository.save(account);
        transaction.setAccount(account);

        Category category = categoryRepository.findById(transactionCreateDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE));
        transaction.setCategory(category);

        // 3. Для расходов: пробуем обновить бюджеты (если они есть)
        if (transaction.getAmount() < 0) {
            List<Budget> budgets = budgetRepository.findByCategoryIdAndClientId(
                    category.getId(),
                    account.getClient().getId()
            );

            // Не кидаем ошибку если бюджетов нет - просто пропускаем
            for (Budget budget : budgets) {
                double newAvailableSum = budget.getAvailableSum() + transaction.getAmount();

                if (newAvailableSum >= 0) {
                    budget.setAvailableSum(newAvailableSum);
                    budgetRepository.save(budget);
                } else {
                    throw new BudgetLimitExceededException(
                    String.format("Budget limit '%s' exceeded! Available: %.2f, required: %.2f",
                                    budget.getCategory().getName(),
                                    budget.getAvailableSum(),
                                    Math.abs(transaction.getAmount()))
                    );
                }
            }
        }

        // 4. Сохраняем транзакцию
        Transaction savedTransaction = transactionRepository.save(transaction);
        clearCacheForClientAndCategory(account.getClient().getId(), category.getId());

        return savedTransaction;
    }

    @Transactional
    public TransactionGetDto updateTransaction(int id, TransactionCreateDto transactionDetails) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND_MESSAGE));
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setDate(transactionDetails.getDate());
        Account account = accountRepository.findById(transaction.getAccount().getId())
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND_MESSAGE));
        double amountDifference = transactionDetails.getAmount() - transaction.getAmount();
        if (account.getBalance() + amountDifference < 0) {
            throw new InvalidDataException(
                    "Insufficient funds: transaction update would result in negative balance"
            );
        }
        transaction.setAmount(transactionDetails.getAmount());
        Transaction savedTransaction = transactionRepository.save(transaction);
        account.setBalance(account.getBalance() + amountDifference);
        accountRepository.save(account);
        clearCacheForClientAndCategory(account.getClient().getId(),
                savedTransaction.getCategory().getId());
        return TransactionGetDto.convertToDto(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(int id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND_MESSAGE));
        Account account = accountRepository.findById(transaction.getAccount().getId())
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND_MESSAGE));
        clearCacheForClientAndCategory(account.getClient().getId(),
                transaction.getCategory().getId());
        transactionRepository.delete(transaction);
    }
}
