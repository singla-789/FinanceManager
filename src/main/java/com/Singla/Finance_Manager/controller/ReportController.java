package com.Singla.Finance_Manager.controller;

import com.Singla.Finance_Manager.dto.report.MonthlyReportResponse;
import com.Singla.Finance_Manager.dto.report.YearlyReportResponse;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.service.ReportService;
import com.Singla.Finance_Manager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        User user = userService.getCurrentAuthenticatedUser();
        MonthlyReportResponse response = reportService.getMonthlyReport(user, year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable int year) {
        User user = userService.getCurrentAuthenticatedUser();
        YearlyReportResponse response = reportService.getYearlyReport(user, year);
        return ResponseEntity.ok(response);
    }
}
