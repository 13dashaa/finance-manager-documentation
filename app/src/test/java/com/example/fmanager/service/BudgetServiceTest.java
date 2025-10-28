package com.example.fmanager.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.example.fmanager.dto.BudgetCreateDto;
import com.example.fmanager.dto.BudgetGetDto;
import com.example.fmanager.dto.BudgetUpdateDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Budget;
import com.example.fmanager.models.Category;
import com.example.fmanager.models.Client;
import com.example.fmanager.repository.BudgetRepository;
import com.example.fmanager.repository.CategoryRepository;
import com.example.fmanager.repository.ClientRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private BudgetService budgetService;

    private Budget budget;
    private Category category;
    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1);
        client.setUsername("testuser");

        category = new Category();
        category.setId(1);
        category.setName("Food");

        budget = new Budget();
        budget.setId(1);
        budget.setPeriod(20);
        budget.setLimitation(1000);
        budget.setCategory(category);
        budget.setClients(Set.of(client));
    }

    @Test
    void getAllBudgets() {
        when(budgetRepository.findAll()).thenReturn(List.of(budget));

        List<BudgetGetDto> result = budgetService.getAllBudgets();

        assertEquals(1, result.size());
        assertEquals(20, result.get(0).getPeriod());
    }

    @Test
    void getBudgetById_Success() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));

        Optional<BudgetGetDto> result = budgetService.getBudgetById(1);

        assertTrue(result.isPresent());
        assertEquals(20, result.get().getPeriod());
    }

    @Test
    void getBudgetById_NotFound() {
        when(budgetRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> budgetService.getBudgetById(1));
    }

    @Test
    void createBudget_Success() {
        BudgetCreateDto dto = new BudgetCreateDto(
                24,
                1500,
                1,
                new HashSet<>(List.of(1)));
        Budget savedBudget = new Budget();
        savedBudget.setId(2);
        savedBudget.setPeriod(dto.getPeriod());
        savedBudget.setLimitation(dto.getLimitation());
        savedBudget.setClients(Set.of(client));
        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);

        Budget result = budgetService.createBudget(dto);

        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals(24, result.getPeriod());
    }

    @Test
    void updateBudget_Success() {
        BudgetUpdateDto updateDto = new BudgetUpdateDto(
                20,
                2000,
                new HashSet<>(List.of(1)));
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        BudgetGetDto result = budgetService.updateBudget(1, updateDto);

        assertEquals(20, result.getPeriod());
    }

    @Test
    void deleteBudget_Success() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        doNothing().when(budgetRepository).delete(budget);

        assertDoesNotThrow(() -> budgetService.deleteBudget(1));
        verify(budgetRepository, times(1)).delete(budget);
    }

    @Test
    void deleteBudget_NotFound() {
        when(budgetRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> budgetService.deleteBudget(1));
    }
}
