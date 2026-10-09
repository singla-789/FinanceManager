package com.Singla.Finance_Manager;

import com.Singla.Finance_Manager.dto.auth.LoginRequest;
import com.Singla.Finance_Manager.dto.auth.RegisterRequest;
import com.Singla.Finance_Manager.dto.category.CategoryRequest;
import com.Singla.Finance_Manager.dto.goal.GoalCreateRequest;
import com.Singla.Finance_Manager.dto.transaction.TransactionCreateRequest;
import com.Singla.Finance_Manager.dto.transaction.TransactionUpdateRequest;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonalFinanceManagerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static MockHttpSession userASession;
    private static MockHttpSession userBSession;
    private static Long userATransactionId;
    private static Long userAGoalId;

    @Test
    @Order(1)
    void testUnauthenticatedAccessRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void testRegisterAndLoginUserA() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("alice@example.com", "password123", "Alice Doe", "+1234567890");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").isNumber());

        // Duplicate registration returns 409
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isConflict());

        // Login User A
        LoginRequest loginReq = new LoginRequest("alice@example.com", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        userASession = (MockHttpSession) loginResult.getRequest().getSession();
    }

    @Test
    @Order(3)
    void testRegisterAndLoginUserB() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("bob@example.com", "password456", "Bob Smith", "+9876543210");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("bob@example.com", "password456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        userBSession = (MockHttpSession) loginResult.getRequest().getSession();
    }

    @Test
    @Order(4)
    void testCategoryManagement() throws Exception {
        // User A lists default categories
        mockMvc.perform(get("/api/categories").session(userASession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$.categories[?(@.name == 'Salary')].isCustom").value(false))
                .andExpect(jsonPath("$.categories[?(@.name == 'Food')].isCustom").value(false));

        // User A creates custom category
        CategoryRequest customCat = new CategoryRequest("Freelance", CategoryType.INCOME);
        mockMvc.perform(post("/api/categories")
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customCat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.isCustom").value(true));

        // Duplicate custom category returns 409
        mockMvc.perform(post("/api/categories")
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customCat)))
                .andExpect(status().isConflict());

        // Attempting to delete default category returns 403
        mockMvc.perform(delete("/api/categories/Salary").session(userASession))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void testTransactionManagement() throws Exception {
        // Validation: Future date should return 400
        TransactionCreateRequest futureTx = new TransactionCreateRequest(
                BigDecimal.valueOf(1000.00),
                LocalDate.now().plusDays(2),
                "Salary",
                "Future Salary"
        );
        mockMvc.perform(post("/api/transactions")
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTx)))
                .andExpect(status().isBadRequest());

        // Create valid Income Transaction for User A
        TransactionCreateRequest incomeTx = new TransactionCreateRequest(
                BigDecimal.valueOf(50000.00),
                LocalDate.of(2024, 1, 15),
                "Salary",
                "January Salary"
        );
        MvcResult txResult = mockMvc.perform(post("/api/transactions")
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomeTx)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50000.00))
                .andExpect(jsonPath("$.category").value("Salary"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andReturn();

        userATransactionId = objectMapper.readTree(txResult.getResponse().getContentAsString()).get("id").asLong();

        // Create Expense Transaction for User A
        TransactionCreateRequest expenseTx = new TransactionCreateRequest(
                BigDecimal.valueOf(15000.00),
                LocalDate.of(2024, 1, 20),
                "Rent",
                "January Rent"
        );
        mockMvc.perform(post("/api/transactions")
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseTx)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EXPENSE"));

        // Get transactions with filters
        mockMvc.perform(get("/api/transactions")
                        .session(userASession)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)));

        // Update transaction - date change is ignored (200), date remains unchanged
        TransactionUpdateRequest dateChangeReq = new TransactionUpdateRequest(
                BigDecimal.valueOf(55000.00),
                "Updated",
                null,
                LocalDate.of(2024, 1, 16)
        );
        mockMvc.perform(put("/api/transactions/" + userATransactionId)
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dateChangeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2024-01-15"));

        // Update transaction amount & description (200)
        TransactionUpdateRequest validUpdate = new TransactionUpdateRequest(
                BigDecimal.valueOf(60000.00),
                "Updated January Salary",
                null,
                null
        );
        mockMvc.perform(put("/api/transactions/" + userATransactionId)
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(60000.00))
                .andExpect(jsonPath("$.description").value("Updated January Salary"));
    }

    @Test
    @Order(6)
    void testSavingsGoals() throws Exception {
        // Create Goal for User A
        GoalCreateRequest goalReq = new GoalCreateRequest(
                "Emergency Fund",
                BigDecimal.valueOf(50000.00),
                LocalDate.now().plusYears(1),
                LocalDate.of(2024, 1, 1)
        );
        MvcResult goalResult = mockMvc.perform(post("/api/goals")
                        .session(userASession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(50000.00))
                .andExpect(jsonPath("$.currentProgress").value(45000.00)) // 60000 income - 15000 expense
                .andExpect(jsonPath("$.progressPercentage").value(90.0))
                .andExpect(jsonPath("$.remainingAmount").value(5000.00))
                .andReturn();

        userAGoalId = objectMapper.readTree(goalResult.getResponse().getContentAsString()).get("id").asLong();

        // Get Goal By ID
        mockMvc.perform(get("/api/goals/" + userAGoalId).session(userASession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userAGoalId));
    }

    @Test
    @Order(7)
    void testReportsAndAnalytics() throws Exception {
        // Monthly Report
        mockMvc.perform(get("/api/reports/monthly/2024/1").session(userASession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(60000.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(15000.00))
                .andExpect(jsonPath("$.netSavings").value(45000.00));

        // Yearly Report
        mockMvc.perform(get("/api/reports/yearly/2024").session(userASession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(60000.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(15000.00))
                .andExpect(jsonPath("$.netSavings").value(45000.00));
    }

    @Test
    @Order(8)
    void testDataIsolationBetweenUsers() throws Exception {
        // User B cannot access User A's transaction (403 Forbidden)
        mockMvc.perform(put("/api/transactions/" + userATransactionId)
                        .session(userBSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransactionUpdateRequest(BigDecimal.valueOf(100.0), null, null, null))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/transactions/" + userATransactionId).session(userBSession))
                .andExpect(status().isForbidden());

        // User B cannot access User A's goal (403 Forbidden)
        mockMvc.perform(get("/api/goals/" + userAGoalId).session(userBSession))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/goals/" + userAGoalId).session(userBSession))
                .andExpect(status().isForbidden());

        // User B has empty transactions list initially
        mockMvc.perform(get("/api/transactions").session(userBSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)));
    }

    @Test
    @Order(9)
    void testLogoutFlow() throws Exception {
        mockMvc.perform(post("/api/auth/logout").session(userASession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));

        // Subsequent requests with invalidated session return 401
        mockMvc.perform(get("/api/transactions").session(userASession))
                .andExpect(status().isUnauthorized());
    }
}
