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
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionCreateRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        TransactionResponse response = transactionService.createTransaction(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CategoryType type) {
        User user = userService.getCurrentAuthenticatedUser();
        TransactionListResponse response = transactionService.getTransactions(user, startDate, endDate, categoryId, category, type);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        TransactionResponse response = transactionService.updateTransaction(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(@PathVariable Long id) {
        User user = userService.getCurrentAuthenticatedUser();
        MessageResponse response = transactionService.deleteTransaction(user, id);
        return ResponseEntity.ok(response);
    }
}
