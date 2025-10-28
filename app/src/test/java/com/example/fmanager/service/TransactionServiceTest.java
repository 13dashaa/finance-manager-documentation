package com.example.fmanager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.fmanager.dto.TransactionCreateDto;
import com.example.fmanager.dto.TransactionGetDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Account;
import com.example.fmanager.models.Category;
import com.example.fmanager.models.Client;
import com.example.fmanager.models.Transaction;
import com.example.fmanager.repository.AccountRepository;
import com.example.fmanager.repository.CategoryRepository;
import com.example.fmanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Account account;
    private Category category;
    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1);

        account = new Account();
        account.setId(1);
        account.setClient(client);
        account.setBalance(1000);

        category = new Category();
        category.setId(1);

        transaction = new Transaction();
        transaction.setId(1);
        transaction.setAmount(1000);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test Transaction");
        transaction.setAccount(account);
        transaction.setCategory(category);
    }

    @Test
    void getTransactionById_Success() {
        when(transactionRepository.findById(1)).thenReturn(Optional.of(transaction));

        Optional<TransactionGetDto> result = transactionService.getTransactionById(1);

        assertTrue(result.isPresent());
        assertEquals(transaction.getDescription(), result.get().getDescription());
    }

    @Test
    void getTransactionById_NotFound() {
        when(transactionRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> transactionService.getTransactionById(1));
    }

    @Test
    void createTransaction_Success() {
        TransactionCreateDto dto = new TransactionCreateDto(
                "New Transaction",
                2000,
                LocalDateTime.now(),
                1,
                1
        );
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(2);
        savedTransaction.setAmount(dto.getAmount());
        savedTransaction.setDate(dto.getDate());
        savedTransaction.setDescription(dto.getDescription());
        savedTransaction.setAccount(account);
        savedTransaction.setCategory(category);

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        Transaction result = transactionService.createTransaction(dto);

        assertNotNull(result);
        assertEquals(savedTransaction.getAmount(), result.getAmount());
    }

    @Test
    void getAllTransactions_Success() {
        Transaction transaction2 = new Transaction();
        transaction2.setId(2);
        transaction2.setAmount(500);
        transaction2.setDate(LocalDateTime.now());
        transaction2.setDescription("Second Transaction");
        transaction2.setAccount(account);
        transaction2.setCategory(category);

        List<Transaction> transactions = List.of(transaction, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<TransactionGetDto> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(transaction.getDescription(), result.get(0).getDescription());
        assertEquals(transaction2.getDescription(), result.get(1).getDescription());
    }

    @Test
    void findByClientIdAndCategoryId_CacheHit() {
        List<TransactionGetDto> cachedTransactions = List.of(
                TransactionGetDto.convertToDto(transaction)
        );
        String cacheKey = "transactions_client_1_category_1";

        when(cache.containsKey(cacheKey)).thenReturn(true);
        when(cache.get(cacheKey)).thenReturn(cachedTransactions);

        List<TransactionGetDto> result = transactionService.findByClientIdAndCategoryId(1, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction.getDescription(), result.get(0).getDescription());
    }

    @Test
    void findByClientIdAndCategoryId_CacheMiss() {
        Transaction transaction2 = new Transaction();
        transaction2.setId(2);
        transaction2.setAmount(500);
        transaction2.setDate(LocalDateTime.now());
        transaction2.setDescription("Second Transaction");
        transaction2.setAccount(account);
        transaction2.setCategory(category);

        List<Transaction> transactions = List.of(transaction, transaction2);
        String cacheKey = "transactions_client_1_category_1";

        when(cache.containsKey(cacheKey)).thenReturn(false);
        when(transactionRepository.findAllByClientIdAndCategoryId(1, 1)).thenReturn(transactions);

        List<TransactionGetDto> result = transactionService.findByClientIdAndCategoryId(1, 1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(transaction.getDescription(), result.get(0).getDescription());
        assertEquals(transaction2.getDescription(), result.get(1).getDescription());

        verify(cache).put(eq(cacheKey), any(List.class));
    }

    @Test
    void updateTransaction_Success() {
        TransactionCreateDto updateDto = new TransactionCreateDto(
                "Updated Transaction",
                3,
                LocalDateTime.now(),
                1,
                1);

        when(transactionRepository.findById(1)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));  // Исправление
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionGetDto result = transactionService.updateTransaction(1, updateDto);

        assertEquals(updateDto.getDescription(), result.getDescription());
    }

    @Test
    void deleteTransaction_Success() {
        when(transactionRepository.findById(1)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));  // Исправление
        doNothing().when(transactionRepository).delete(transaction);

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1));
        verify(transactionRepository, times(1)).delete(transaction);
    }
}
