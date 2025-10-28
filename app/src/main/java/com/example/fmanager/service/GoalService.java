package com.example.fmanager.service;

import static com.example.fmanager.exception.NotFoundMessages.CLIENT_NOT_FOUND_MESSAGE;
import static com.example.fmanager.exception.NotFoundMessages.GOAL_NOT_FOUND_MESSAGE;

import com.example.fmanager.dto.GoalCreateDto;
import com.example.fmanager.dto.GoalGetDto;
import com.example.fmanager.dto.TransactionCreateDto;
import com.example.fmanager.exception.InvalidDataException;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Client;
import com.example.fmanager.models.Goal;
import com.example.fmanager.repository.ClientRepository;
import com.example.fmanager.repository.GoalRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GoalService {
    private final GoalRepository goalRepository;
    private final InMemoryCache cache;
    private final ClientRepository clientRepository;
    private final TransactionService transactionService; // Инжектируем TransactionService

    public GoalService(GoalRepository goalRepository,
                       InMemoryCache cache,
                       ClientRepository clientRepository,
                       TransactionService transactionService) {
        this.goalRepository = goalRepository;
        this.transactionService = transactionService;
        this.cache = cache;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public GoalGetDto addFundsToGoal(int goalId, TransactionCreateDto transactionDto) {
        if (transactionDto.getAmount() <= 0) {
            throw new InvalidDataException("Amount to save must be positive.");
        }
        if (transactionDto.getAccountId() == null || transactionDto.getCategoryId() == null) {
            throw new InvalidDataException("AccountId and CategoryId are required.");
        }
        double amountToAddToGoal = Math.abs(transactionDto.getAmount());
        TransactionCreateDto actualTransactionDto = new TransactionCreateDto();
        actualTransactionDto.setAccountId(transactionDto.getAccountId());
        actualTransactionDto.setCategoryId(transactionDto.getCategoryId());
        actualTransactionDto.setDate(transactionDto.getDate());
        actualTransactionDto.setDescription(transactionDto.getDescription());
        actualTransactionDto.setAmount(-amountToAddToGoal);
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException(GOAL_NOT_FOUND_MESSAGE));
        transactionService.createTransaction(actualTransactionDto);
        double currentAmount = goal.getCurrentAmount() != 0 ? goal.getCurrentAmount() : 0.0;
        goal.setCurrentAmount(currentAmount + amountToAddToGoal);
        Goal updatedGoal = goalRepository.save(goal);
        clearCacheForClient(updatedGoal.getClient().getId());
        return GoalGetDto.convertToDto(updatedGoal);
    }

    public Optional<GoalGetDto> getGoalById(int id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(GOAL_NOT_FOUND_MESSAGE));
        return Optional.of(GoalGetDto.convertToDto(goal));
    }

    public List<GoalGetDto> getAllGoals() {
        List<Goal> goals = goalRepository.findAll();
        List<GoalGetDto> goalsDtos = new ArrayList<>();
        for (Goal goal : goals) {
            goalsDtos.add(GoalGetDto.convertToDto(goal));
        }
        return goalsDtos;
    }

    public List<GoalGetDto> findByClientId(int clientId) {
        String cacheKey = "goals_client_" + clientId;
        if (cache.containsKey(cacheKey)) {
            return (List<GoalGetDto>) cache.get(cacheKey);
        }
        List<Goal> goals = goalRepository.findByClientId(clientId);
        List<GoalGetDto> goalsDtos = new ArrayList<>();
        for (Goal goal : goals) {
            goalsDtos.add(GoalGetDto.convertToDto(goal));
        }
        cache.put(cacheKey, goalsDtos);
        return goalsDtos;
    }

    public void clearCacheForClient(int clientId) {
        String cacheKey = "goals_client_" + clientId;
        cache.remove(cacheKey);
    }

    public Goal createGoal(GoalCreateDto goalCreateDto) {
        Client client = clientRepository.findById(goalCreateDto.getClientId())
                .orElseThrow(() -> new RuntimeException(CLIENT_NOT_FOUND_MESSAGE));
        Goal goal = new Goal();
        goal.setName(goalCreateDto.getName());
        goal.setStartDate(goalCreateDto.getStartDate());
        goal.setEndDate(goalCreateDto.getEndDate());
        goal.setTargetAmount(goalCreateDto.getTargetAmount());
        goal.setClient(client);
        Goal savedGoal = goalRepository.save(goal);
        clearCacheForClient(savedGoal.getClient().getId());
        return savedGoal;
    }

    @Transactional
    public GoalGetDto updateGoal(int id, GoalCreateDto goalDetails) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(GOAL_NOT_FOUND_MESSAGE));
        goal.setName(goalDetails.getName());
        goal.setTargetAmount(goalDetails.getTargetAmount());
        goal.setEndDate(goalDetails.getEndDate());
        goal.setStartDate(goalDetails.getStartDate());
        Goal savedGoal = goalRepository.save(goal);
        clearCacheForClient(savedGoal.getClient().getId());
        return GoalGetDto.convertToDto(savedGoal);
    }

    @Transactional
    public void deleteGoal(int id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(GOAL_NOT_FOUND_MESSAGE));
        clearCacheForClient(goal.getClient().getId());
        goalRepository.delete(goal);
    }
}
