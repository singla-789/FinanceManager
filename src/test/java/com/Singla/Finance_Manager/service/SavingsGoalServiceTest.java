package com.Singla.Finance_Manager.service;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.goal.GoalCreateRequest;
import com.Singla.Finance_Manager.dto.goal.GoalListResponse;
import com.Singla.Finance_Manager.dto.goal.GoalResponse;
import com.Singla.Finance_Manager.dto.goal.GoalUpdateRequest;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.SavingsGoal;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.exception.BadRequestException;
import com.Singla.Finance_Manager.exception.ForbiddenException;
import com.Singla.Finance_Manager.exception.ResourceNotFoundException;
import com.Singla.Finance_Manager.repository.SavingsGoalRepository;
import com.Singla.Finance_Manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "User One", "+123");
        testUser.setId(1L);

        otherUser = new User("other@example.com", "pass", "User Two", "+456");
        otherUser.setId(2L);
    }

    @Test
    void testCreateGoalSuccess() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.now().plusMonths(6);

        GoalCreateRequest request = new GoalCreateRequest(
                "Emergency Fund",
                BigDecimal.valueOf(5000.00),
                targetDate,
                startDate
        );

        SavingsGoal savedGoal = new SavingsGoal(testUser, "Emergency Fund", BigDecimal.valueOf(5000.00), startDate, targetDate);
        savedGoal.setId(10L);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(savedGoal);

        when(transactionRepository.sumAmountByUserAndTypeAndDateAfterEqual(eq(testUser), eq(CategoryType.INCOME), eq(startDate)))
                .thenReturn(BigDecimal.valueOf(3000.00));
        when(transactionRepository.sumAmountByUserAndTypeAndDateAfterEqual(eq(testUser), eq(CategoryType.EXPENSE), eq(startDate)))
                .thenReturn(BigDecimal.valueOf(2000.00));

        GoalResponse response = savingsGoalService.createGoal(testUser, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(BigDecimal.valueOf(1000.00).setScale(2), response.getCurrentProgress());
        assertEquals(20.0, response.getProgressPercentage());
        assertEquals(BigDecimal.valueOf(4000.00).setScale(2), response.getRemainingAmount());
    }

    @Test
    void testCreateGoalTargetDateBeforeStartDateThrowsBadRequest() {
        GoalCreateRequest request = new GoalCreateRequest(
                "Trip",
                BigDecimal.valueOf(1000.00),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 5, 1)
        );

        assertThrows(BadRequestException.class, () -> savingsGoalService.createGoal(testUser, request));
    }

    @Test
    void testGetAllGoals() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.now().plusYears(1);
        SavingsGoal goal = new SavingsGoal(testUser, "Car", BigDecimal.valueOf(10000.00), startDate, targetDate);
        goal.setId(1L);

        when(savingsGoalRepository.findByUserOrderByIdAsc(testUser)).thenReturn(List.of(goal));
        when(transactionRepository.sumAmountByUserAndTypeAndDateAfterEqual(eq(testUser), eq(CategoryType.INCOME), eq(startDate)))
                .thenReturn(BigDecimal.valueOf(5000.00));
        when(transactionRepository.sumAmountByUserAndTypeAndDateAfterEqual(eq(testUser), eq(CategoryType.EXPENSE), eq(startDate)))
                .thenReturn(BigDecimal.valueOf(0.00));

        GoalListResponse response = savingsGoalService.getAllGoals(testUser);

        assertEquals(1, response.getGoals().size());
        assertEquals(50.0, response.getGoals().get(0).getProgressPercentage());
    }

    @Test
    void testGetGoalByIdOtherUserThrowsForbidden() {
        SavingsGoal goal = new SavingsGoal(otherUser, "Car", BigDecimal.valueOf(10000.00), LocalDate.now(), LocalDate.now().plusYears(1));
        goal.setId(2L);

        when(savingsGoalRepository.findById(2L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class, () -> savingsGoalService.getGoalById(testUser, 2L));
    }

    @Test
    void testDeleteGoalSuccess() {
        SavingsGoal goal = new SavingsGoal(testUser, "Car", BigDecimal.valueOf(10000.00), LocalDate.now(), LocalDate.now().plusYears(1));
        goal.setId(1L);

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        MessageResponse response = savingsGoalService.deleteGoal(testUser, 1L);

        assertEquals("Goal deleted successfully", response.getMessage());
        verify(savingsGoalRepository).delete(goal);
    }
}
