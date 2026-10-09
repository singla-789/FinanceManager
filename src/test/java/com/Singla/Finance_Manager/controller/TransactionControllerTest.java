package com.Singla.Finance_Manager.controller;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.transaction.TransactionCreateRequest;
import com.Singla.Finance_Manager.dto.transaction.TransactionListResponse;
import com.Singla.Finance_Manager.dto.transaction.TransactionResponse;
import com.Singla.Finance_Manager.dto.transaction.TransactionUpdateRequest;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

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
    void testCreateTransaction() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                BigDecimal.valueOf(50000.00),
                LocalDate.now(),
                "Salary",
                "Salary desc"
        );
        TransactionResponse response = new TransactionResponse(1L, BigDecimal.valueOf(50000.00), LocalDate.now(), "Salary", "Salary desc", CategoryType.INCOME);
        when(transactionService.createTransaction(eq(testUser), any(TransactionCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(50000.00))
                .andExpect(jsonPath("$.category").value("Salary"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @WithMockUser
    void testGetTransactions() throws Exception {
        TransactionResponse tx = new TransactionResponse(1L, BigDecimal.valueOf(50000.00), LocalDate.now(), "Salary", "desc", CategoryType.INCOME);
        when(transactionService.getTransactions(eq(testUser), any(), any(), any(), any(), any()))
                .thenReturn(new TransactionListResponse(List.of(tx)));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value(1));
    }

    @Test
    @WithMockUser
    void testUpdateTransaction() throws Exception {
        TransactionUpdateRequest request = new TransactionUpdateRequest(BigDecimal.valueOf(60000.00), "updated", null, null);
        TransactionResponse response = new TransactionResponse(1L, BigDecimal.valueOf(60000.00), LocalDate.now(), "Salary", "updated", CategoryType.INCOME);
        when(transactionService.updateTransaction(eq(testUser), eq(1L), any(TransactionUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(60000.00));
    }

    @Test
    @WithMockUser
    void testDeleteTransaction() throws Exception {
        when(transactionService.deleteTransaction(eq(testUser), eq(1L)))
                .thenReturn(new MessageResponse("Transaction deleted successfully"));

        mockMvc.perform(delete("/api/transactions/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
