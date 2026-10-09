package com.Singla.Finance_Manager.service;

import com.Singla.Finance_Manager.dto.report.MonthlyReportResponse;
import com.Singla.Finance_Manager.dto.report.YearlyReportResponse;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.Transaction;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.exception.BadRequestException;
import com.Singla.Finance_Manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(User user, int year, int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Invalid month: " + month + ". Month must be between 1 and 12.");
        }
        if (year < 1900 || year > 2100) {
            throw new BadRequestException("Invalid year: " + year);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> totalExpenses = new LinkedHashMap<>();
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome.put(categoryName, totalIncome.getOrDefault(categoryName, BigDecimal.ZERO).add(amount).setScale(2, RoundingMode.HALF_UP));
                incomeSum = incomeSum.add(amount);
            } else {
                totalExpenses.put(categoryName, totalExpenses.getOrDefault(categoryName, BigDecimal.ZERO).add(amount).setScale(2, RoundingMode.HALF_UP));
                expenseSum = expenseSum.add(amount);
            }
        }

        BigDecimal diff = incomeSum.subtract(expenseSum);
        BigDecimal netSavings = diff.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : diff.setScale(2, RoundingMode.HALF_UP);
        return new MonthlyReportResponse(month, year, totalIncome, totalExpenses, netSavings);
    }

    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(User user, int year) {
        if (year < 1900 || year > 2100) {
            throw new BadRequestException("Invalid year: " + year);
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> totalExpenses = new LinkedHashMap<>();
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome.put(categoryName, totalIncome.getOrDefault(categoryName, BigDecimal.ZERO).add(amount).setScale(2, RoundingMode.HALF_UP));
                incomeSum = incomeSum.add(amount);
            } else {
                totalExpenses.put(categoryName, totalExpenses.getOrDefault(categoryName, BigDecimal.ZERO).add(amount).setScale(2, RoundingMode.HALF_UP));
                expenseSum = expenseSum.add(amount);
            }
        }

        BigDecimal diff = incomeSum.subtract(expenseSum);
        BigDecimal netSavings = diff.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : diff.setScale(2, RoundingMode.HALF_UP);
        return new YearlyReportResponse(year, totalIncome, totalExpenses, netSavings);
    }
}
