package com.Singla.Finance_Manager.dto.report;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class MonthlyReportResponse {

    private int month;
    private int year;
    private Map<String, BigDecimal> totalIncome = new LinkedHashMap<>();
    private Map<String, BigDecimal> totalExpenses = new LinkedHashMap<>();
    private BigDecimal netSavings;

    public MonthlyReportResponse() {
    }

    public MonthlyReportResponse(int month, int year, Map<String, BigDecimal> totalIncome, Map<String, BigDecimal> totalExpenses, BigDecimal netSavings) {
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome != null ? totalIncome : new LinkedHashMap<>();
        this.totalExpenses = totalExpenses != null ? totalExpenses : new LinkedHashMap<>();
        this.netSavings = netSavings;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Map<String, BigDecimal> getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Map<String, BigDecimal> totalIncome) {
        this.totalIncome = totalIncome;
    }

    public Map<String, BigDecimal> getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(Map<String, BigDecimal> totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = netSavings;
    }
}
