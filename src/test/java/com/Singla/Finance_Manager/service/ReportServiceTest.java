package com.Singla.Finance_Manager.service;

import com.Singla.Finance_Manager.dto.report.MonthlyReportResponse;
import com.Singla.Finance_Manager.dto.report.YearlyReportResponse;
import com.Singla.Finance_Manager.entity.Category;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.Transaction;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.exception.BadRequestException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private Category salaryCategory;
    private Category foodCategory;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "User One", "+123");
        testUser.setId(1L);

        salaryCategory = new Category("Salary", CategoryType.INCOME, false, null);
        foodCategory = new Category("Food", CategoryType.EXPENSE, false, null);
    }

    @Test
    void testGetMonthlyReportSuccess() {
        Transaction t1 = new Transaction(testUser, salaryCategory, BigDecimal.valueOf(3000.00), LocalDate.of(2024, 1, 10), "Salary");
        Transaction t2 = new Transaction(testUser, foodCategory, BigDecimal.valueOf(400.00), LocalDate.of(2024, 1, 12), "Groceries");

        when(transactionRepository.findByUserAndDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        MonthlyReportResponse response = reportService.getMonthlyReport(testUser, 2024, 1);

        assertNotNull(response);
        assertEquals(1, response.getMonth());
        assertEquals(2024, response.getYear());
        assertEquals(BigDecimal.valueOf(3000.00).setScale(2), response.getTotalIncome().get("Salary"));
        assertEquals(BigDecimal.valueOf(400.00).setScale(2), response.getTotalExpenses().get("Food"));
        assertEquals(BigDecimal.valueOf(2600.00).setScale(2), response.getNetSavings());
    }

    @Test
    void testGetMonthlyReportInvalidMonthThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> reportService.getMonthlyReport(testUser, 2024, 13));
        assertThrows(BadRequestException.class, () -> reportService.getMonthlyReport(testUser, 2024, 0));
    }

    @Test
    void testGetYearlyReportSuccess() {
        Transaction t1 = new Transaction(testUser, salaryCategory, BigDecimal.valueOf(36000.00), LocalDate.of(2024, 6, 1), "Annual Salary");
        Transaction t2 = new Transaction(testUser, foodCategory, BigDecimal.valueOf(4800.00), LocalDate.of(2024, 6, 5), "Food");

        when(transactionRepository.findByUserAndDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        YearlyReportResponse response = reportService.getYearlyReport(testUser, 2024);

        assertNotNull(response);
        assertEquals(2024, response.getYear());
        assertEquals(BigDecimal.valueOf(36000.00).setScale(2), response.getTotalIncome().get("Salary"));
        assertEquals(BigDecimal.valueOf(4800.00).setScale(2), response.getTotalExpenses().get("Food"));
        assertEquals(BigDecimal.valueOf(31200.00).setScale(2), response.getNetSavings());
    }
}
