package com.Singla.Finance_Manager.controller;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.goal.GoalCreateRequest;
import com.Singla.Finance_Manager.dto.goal.GoalListResponse;
import com.Singla.Finance_Manager.dto.goal.GoalResponse;
import com.Singla.Finance_Manager.dto.goal.GoalUpdateRequest;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.service.SavingsGoalService;
import com.Singla.Finance_Manager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingsGoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class SavingsGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SavingsGoalService savingsGoalService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "User", "+123");
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
    }

    @Test
    @WithMockUser
    void testCreateGoal() throws Exception {
        GoalCreateRequest request = new GoalCreateRequest(
                "Emergency Fund",
                BigDecimal.valueOf(5000.00),
                LocalDate.now().plusMonths(6),
                LocalDate.now()
        );
        GoalResponse response = new GoalResponse(
                1L, "Emergency Fund", BigDecimal.valueOf(5000.00),
                LocalDate.now().plusMonths(6), LocalDate.now(),
                BigDecimal.valueOf(1000.00), 20.0, BigDecimal.valueOf(4000.00)
        );
        when(savingsGoalService.createGoal(eq(testUser), any(GoalCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.progressPercentage").value(20.0));
    }

    @Test
    @WithMockUser
    void testGetAllGoals() throws Exception {
        GoalResponse response = new GoalResponse(1L, "Emergency Fund", BigDecimal.valueOf(5000.00),
                LocalDate.now().plusMonths(6), LocalDate.now(),
                BigDecimal.valueOf(1000.00), 20.0, BigDecimal.valueOf(4000.00));
        when(savingsGoalService.getAllGoals(testUser)).thenReturn(new GoalListResponse(List.of(response)));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].id").value(1));
    }

    @Test
    @WithMockUser
    void testUpdateGoal() throws Exception {
        GoalUpdateRequest request = new GoalUpdateRequest(BigDecimal.valueOf(6000.00), LocalDate.now().plusMonths(8));
        GoalResponse response = new GoalResponse(1L, "Emergency Fund", BigDecimal.valueOf(6000.00),
                LocalDate.now().plusMonths(8), LocalDate.now(),
                BigDecimal.valueOf(1000.00), 16.67, BigDecimal.valueOf(5000.00));
        when(savingsGoalService.updateGoal(eq(testUser), eq(1L), any(GoalUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(6000.00));
    }

    @Test
    @WithMockUser
    void testDeleteGoal() throws Exception {
        when(savingsGoalService.deleteGoal(eq(testUser), eq(1L)))
                .thenReturn(new MessageResponse("Goal deleted successfully"));

        mockMvc.perform(delete("/api/goals/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }
}
