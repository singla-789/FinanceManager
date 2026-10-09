package com.Singla.Finance_Manager.dto.goal;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalUpdateRequest {

    @Positive(message = "Target amount must be a positive decimal value")
    private BigDecimal targetAmount;

    @Future(message = "Target date must be a future date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    public GoalUpdateRequest() {
    }

    public GoalUpdateRequest(BigDecimal targetAmount, LocalDate targetDate) {
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
}
