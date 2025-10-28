package com.example.fmanager.service;

import static com.example.fmanager.exception.NotFoundMessages.ACCOUNT_NOT_FOUND_MESSAGE;

import com.example.fmanager.dto.AccountCreateDto;
import com.example.fmanager.dto.AccountGetDto;
import com.example.fmanager.dto.AccountUpdateDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Account;
import com.example.fmanager.models.Client;
import com.example.fmanager.repository.AccountRepository;
import com.example.fmanager.repository.CategoryRepository;
import com.example.fmanager.repository.ClientRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ClientRepository clientRepository;
    private final TransactionService transactionService;
    private final InMemoryCache cache;

    public AccountService(AccountRepository accountRepository,
                          InMemoryCache cache,
                          CategoryRepository categoryRepository,
                          TransactionService transactionService,
                          ClientRepository clientRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.clientRepository = clientRepository;
        this.transactionService = transactionService;
        this.cache = cache;
    }

    public Optional<AccountGetDto> getAccountById(int id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
        return Optional.of(AccountGetDto.convertToDto(account));
    }

    public List<AccountGetDto> findAll() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountGetDto> accountGetDtos = new ArrayList<>();
        for (Account account : accounts) {
            accountGetDtos.add(AccountGetDto.convertToDto(account));
        }
        return accountGetDtos;
    }

    public List<AccountGetDto> findByClientId(int clientId) {
        String cacheKey = "accounts_client_" + clientId;
        if (cache.containsKey(cacheKey)) {
            return (List<AccountGetDto>) cache.get(cacheKey);
        }
        List<Account> accounts = accountRepository.findAllByClientId(clientId);
        List<AccountGetDto> accountGetDtos = new ArrayList<>();
        for (Account account : accounts) {
            accountGetDtos.add(AccountGetDto.convertToDto(account));
        }
        cache.put(cacheKey, accountGetDtos);
        return accountGetDtos;
    }

    public List<AccountGetDto> findByClientUsername(String clientUsername) {
        return accountRepository.findByClientUsername(clientUsername).stream()
                .map(AccountGetDto::convertToDto)
                .toList();
    }

    public void clearCacheForClient(int clientId) {
        String cacheKey = "accounts_client_" + clientId;
        cache.remove(cacheKey);
    }

    @Transactional
    public Account createAccount(AccountCreateDto accountCreateDto) {
        Client client = clientRepository.findById(accountCreateDto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Account account = new Account();
        account.setName(accountCreateDto.getName());
        account.setBalance(accountCreateDto.getBalance());
        account.setClient(client);
        Account savedAccount = accountRepository.save(account);
        List<Integer> categoryIds = categoryRepository.findCategoryIdsByClientId(
                savedAccount.getClient().getId()
        );
        for (Integer categoryId : categoryIds) {
            transactionService.clearCacheForClientAndCategory(
                    savedAccount.getClient().getId(), categoryId
            );
        }
        clearCacheForClient(savedAccount.getClient().getId());
        return savedAccount;
    }

    @Transactional
    public AccountGetDto updateAccount(int id, AccountUpdateDto accountDetails) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
        account.setName(accountDetails.getName());
        account.setBalance(accountDetails.getBalance());
        Account savedAccount = accountRepository.save(account);
        clearCacheForClient(savedAccount.getClient().getId());
        return AccountGetDto.convertToDto(savedAccount);
    }

    @Transactional
    public void deleteAccount(int id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
        clearCacheForClient(account.getClient().getId());
        accountRepository.delete(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
