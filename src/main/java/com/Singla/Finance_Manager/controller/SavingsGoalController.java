package com.Singla.Finance_Manager.controller;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.goal.GoalCreateRequest;
import com.Singla.Finance_Manager.dto.goal.GoalListResponse;
import com.Singla.Finance_Manager.dto.goal.GoalResponse;
import com.Singla.Finance_Manager.dto.goal.GoalUpdateRequest;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.service.SavingsGoalService;
import com.Singla.Finance_Manager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final UserService userService;

    public SavingsGoalController(SavingsGoalService savingsGoalService, UserService userService) {
        this.savingsGoalService = savingsGoalService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalCreateRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        GoalResponse response = savingsGoalService.createGoal(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<GoalListResponse> getAllGoals() {
        User user = userService.getCurrentAuthenticatedUser();
        GoalListResponse response = savingsGoalService.getAllGoals(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        User user = userService.getCurrentAuthenticatedUser();
        GoalResponse response = savingsGoalService.getGoalById(user, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        GoalResponse response = savingsGoalService.updateGoal(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id) {
        User user = userService.getCurrentAuthenticatedUser();
        MessageResponse response = savingsGoalService.deleteGoal(user, id);
        return ResponseEntity.ok(response);
    }
}
