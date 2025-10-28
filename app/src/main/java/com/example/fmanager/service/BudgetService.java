package com.example.fmanager.service;

import static com.example.fmanager.exception.NotFoundMessages.BUDGET_NOT_FOUND_MESSAGE;
import static com.example.fmanager.exception.NotFoundMessages.CATEGORY_NOT_FOUND_MESSAGE;
import static com.example.fmanager.exception.NotFoundMessages.CLIENT_NOT_FOUND_MESSAGE;

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
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {
    private BudgetRepository budgetRepository;
    private CategoryRepository categoryRepository;
    private ClientRepository clientRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         ClientRepository clientRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.clientRepository = clientRepository;
    }

    public List<BudgetGetDto> getBudgetsByClientIdAndCategoryId(int clientId, int categoryId) {
        if (!clientRepository.existsById(clientId)) {
            throw new NotFoundException(CLIENT_NOT_FOUND_MESSAGE + clientId);
        }
        List<Budget> budgets = categoryRepository.findById(categoryId)
                .map(category -> budgetRepository.findByCategoryIdAndClientId(category.getId(), clientId))
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE + categoryId));
        return budgets.stream()
                .map(BudgetGetDto::convertToDto)
                .toList();
    }

    public List<BudgetGetDto> getAllBudgets() {
        List<Budget> budgets = budgetRepository.findAll();
        List<BudgetGetDto> budgetGetDtos = new ArrayList<>();
        for (Budget budget : budgets) {
            budgetGetDtos.add(BudgetGetDto.convertToDto(budget));
        }
        return budgetGetDtos;
    }

    public Optional<BudgetGetDto> getBudgetById(int id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BUDGET_NOT_FOUND_MESSAGE));
        return Optional.of(BudgetGetDto.convertToDto(budget));
    }

    public Budget createBudget(BudgetCreateDto budgetCreateDto) {
        Set<Client> clients = new HashSet<>();
        for (Integer clientId : budgetCreateDto.getClientIds()) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException(CLIENT_NOT_FOUND_MESSAGE + clientId));
            clients.add(client);
        }
        Category category = categoryRepository.findById(budgetCreateDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND_MESSAGE));
        Budget budget = new Budget();
        budget.setPeriod(budgetCreateDto.getPeriod());
        budget.setLimitation(budgetCreateDto.getLimitation());
        budget.setCategory(category);
        budget.setClients(clients);
        return budgetRepository.save(budget);
    }

    @Transactional
    public BudgetGetDto updateBudget(int id, BudgetUpdateDto budgetDetails) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BUDGET_NOT_FOUND_MESSAGE));
        budget.setPeriod(budgetDetails.getPeriod());
        budget.setLimitation(budgetDetails.getLimitation());
        Set<Client> clients = new HashSet<>();
        for (Integer clientId : budgetDetails.getClientIds()) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException(CLIENT_NOT_FOUND_MESSAGE + clientId));
            clients.add(client);
        }
        budget.setClients(clients);
        return BudgetGetDto.convertToDto(budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(int id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BUDGET_NOT_FOUND_MESSAGE));
        budgetRepository.delete(budget);
    }
}
