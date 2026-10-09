package com.Singla.Finance_Manager.service;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.transaction.TransactionCreateRequest;
import com.Singla.Finance_Manager.dto.transaction.TransactionListResponse;
import com.Singla.Finance_Manager.dto.transaction.TransactionResponse;
import com.Singla.Finance_Manager.dto.transaction.TransactionUpdateRequest;
import com.Singla.Finance_Manager.entity.Category;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.Transaction;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.exception.BadRequestException;
import com.Singla.Finance_Manager.exception.ForbiddenException;
import com.Singla.Finance_Manager.exception.ResourceNotFoundException;
import com.Singla.Finance_Manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public TransactionResponse createTransaction(User user, TransactionCreateRequest request) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be a future date");
        }

        Category category = categoryService.getCategoryByNameAccessible(request.getCategory(), user);

        Transaction transaction = new Transaction(
                user,
                category,
                request.getAmount(),
                request.getDate(),
                request.getDescription()
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(User user, LocalDate startDate, LocalDate endDate, Long categoryId, CategoryType type) {
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(user, startDate, endDate, categoryId, type);
        List<TransactionResponse> dtos = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new TransactionListResponse(dtos);
    }

    @Transactional
    public TransactionResponse updateTransaction(User user, Long id, TransactionUpdateRequest request) {
        Optional<Transaction> existingOpt = transactionRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Transaction with ID " + id + " not found");
        }

        Transaction transaction = existingOpt.get();
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to modify this transaction");
        }

        if (transaction.getIsDeleted()) {
            throw new ResourceNotFoundException("Transaction with ID " + id + " not found");
        }

        // Date immutability check
        if (request.getDate() != null && !request.getDate().equals(transaction.getDate())) {
            throw new BadRequestException("Transaction date field cannot be modified");
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            Category category = categoryService.getCategoryByNameAccessible(request.getCategory(), user);
            transaction.setCategory(category);
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Transactional
    public MessageResponse deleteTransaction(User user, Long id) {
        Optional<Transaction> existingOpt = transactionRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Transaction with ID " + id + " not found");
        }

        Transaction transaction = existingOpt.get();
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this transaction");
        }

        if (transaction.getIsDeleted()) {
            throw new ResourceNotFoundException("Transaction with ID " + id + " not found");
        }

        transaction.setIsDeleted(true);
        transactionRepository.save(transaction);

        return new MessageResponse("Transaction deleted successfully");
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getDate(),
                t.getCategory().getName(),
                t.getDescription(),
                t.getCategory().getType()
        );
    }
}
