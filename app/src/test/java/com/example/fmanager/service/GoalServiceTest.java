package com.example.fmanager.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.example.fmanager.dto.GoalCreateDto;
import com.example.fmanager.dto.GoalGetDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Client;
import com.example.fmanager.models.Goal;
import com.example.fmanager.repository.ClientRepository;
import com.example.fmanager.repository.GoalRepository;
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
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private GoalService goalService;

    private Goal goal;
    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1);
        client.setUsername("testuser");

        goal = new Goal();
        goal.setId(1);
        goal.setName("Save for vacation");
        goal.setTargetAmount(5000);
        goal.setClient(client);
    }

    @Test
    void getGoalById_Success() {
        when(goalRepository.findById(1)).thenReturn(Optional.of(goal));

        Optional<GoalGetDto> result = goalService.getGoalById(1);

        assertTrue(result.isPresent());
        assertEquals("Save for vacation", result.get().getName());
    }

    @Test
    void getGoalById_NotFound() {
        when(goalRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> goalService.getGoalById(1));
    }

    @Test
    void getAllGoals() {
        when(goalRepository.findAll()).thenReturn(List.of(goal));

        List<GoalGetDto> result = goalService.getAllGoals();

        assertEquals(1, result.size());
        assertEquals("Save for vacation", result.get(0).getName());
    }

    @Test
    void findByClientId_Success() {
        when(goalRepository.findByClientId(1)).thenReturn(List.of(goal));

        List<GoalGetDto> result = goalService.findByClientId(1);

        assertEquals(1, result.size());
        assertEquals("Save for vacation", result.get(0).getName());
    }

    @Test
    void createGoal_Success() {
        GoalCreateDto createDto = new GoalCreateDto(
                "Save for vacation",
                10000,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                1);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        Goal result = goalService.createGoal(createDto);

        assertNotNull(result);
        assertEquals("Save for vacation", result.getName());
    }

    @Test
    void updateGoal_Success() {
        GoalCreateDto updateDto = new GoalCreateDto("Save for house", 20000, LocalDate.now(), LocalDate.now().plusYears(2), 1);
        when(goalRepository.findById(1)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalGetDto result = goalService.updateGoal(1, updateDto);

        assertEquals("Save for house", result.getName());
    }

    @Test
    void deleteGoal_Success() {
        when(goalRepository.findById(1)).thenReturn(Optional.of(goal));
        doNothing().when(goalRepository).delete(goal);

        assertDoesNotThrow(() -> goalService.deleteGoal(1));
        verify(goalRepository, times(1)).delete(goal);
    }
}
