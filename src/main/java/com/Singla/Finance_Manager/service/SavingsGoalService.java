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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
                              TransactionRepository transactionRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public GoalResponse createGoal(User user, GoalCreateRequest request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        if (request.getTargetDate().isBefore(startDate) || request.getTargetDate().isEqual(startDate)) {
            throw new BadRequestException("Target date must be strictly after the start date");
        }

        if (request.getTargetDate().isBefore(LocalDate.now()) || request.getTargetDate().isEqual(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }

        SavingsGoal goal = new SavingsGoal(
                user,
                request.getGoalName().trim(),
                request.getTargetAmount(),
                startDate,
                request.getTargetDate()
        );

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return calculateAndMapGoal(saved, user);
    }

    @Transactional(readOnly = true)
    public GoalListResponse getAllGoals(User user) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserOrderByIdAsc(user);
        List<GoalResponse> dtos = goals.stream()
                .map(goal -> calculateAndMapGoal(goal, user))
                .collect(Collectors.toList());
        return new GoalListResponse(dtos);
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoalById(User user, Long id) {
        SavingsGoal goal = getGoalWithOwnershipCheck(user, id);
        return calculateAndMapGoal(goal, user);
    }

    @Transactional
    public GoalResponse updateGoal(User user, Long id, GoalUpdateRequest request) {
        SavingsGoal goal = getGoalWithOwnershipCheck(user, id);

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            if (request.getTargetDate().isBefore(goal.getStartDate()) || request.getTargetDate().isEqual(goal.getStartDate())) {
                throw new BadRequestException("Target date must be strictly after the start date");
            }
            if (request.getTargetDate().isBefore(LocalDate.now()) || request.getTargetDate().isEqual(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return calculateAndMapGoal(updated, user);
    }

    @Transactional
    public MessageResponse deleteGoal(User user, Long id) {
        SavingsGoal goal = getGoalWithOwnershipCheck(user, id);
        savingsGoalRepository.delete(goal);
        return new MessageResponse("Goal deleted successfully");
    }

    private SavingsGoal getGoalWithOwnershipCheck(User user, Long id) {
        Optional<SavingsGoal> goalOpt = savingsGoalRepository.findById(id);
        if (goalOpt.isEmpty()) {
            throw new ResourceNotFoundException("Goal with ID " + id + " not found");
        }

        SavingsGoal goal = goalOpt.get();
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this goal");
        }

        return goal;
    }

    private GoalResponse calculateAndMapGoal(SavingsGoal goal, User user) {
        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndDateAfterEqual(
                user, CategoryType.INCOME, goal.getStartDate()
        );
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserAndTypeAndDateAfterEqual(
                user, CategoryType.EXPENSE, goal.getStartDate()
        );

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal currentProgress = totalIncome.subtract(totalExpenses);

        // Calculate progress percentage: (currentProgress / targetAmount) * 100
        double percentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = currentProgress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // Calculate remaining amount: max(0, targetAmount - currentProgress)
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            remainingAmount = remainingAmount.setScale(2, RoundingMode.HALF_UP);
        }

        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount().setScale(2, RoundingMode.HALF_UP),
                goal.getTargetDate(),
                goal.getStartDate(),
                currentProgress.setScale(2, RoundingMode.HALF_UP),
                percentage,
                remainingAmount
        );
    }
}
