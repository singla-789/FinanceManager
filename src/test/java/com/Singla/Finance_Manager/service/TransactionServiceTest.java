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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private User otherUser;
    private Category salaryCategory;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "User One", "+123");
        testUser.setId(1L);

        otherUser = new User("other@example.com", "pass", "User Two", "+456");
        otherUser.setId(2L);

        salaryCategory = new Category("Salary", CategoryType.INCOME, false, null);
        salaryCategory.setId(10L);
    }

    @Test
    void testCreateTransactionSuccess() {
        TransactionCreateRequest request = new TransactionCreateRequest(
                BigDecimal.valueOf(50000.00),
                LocalDate.now(),
                "Salary",
                "Monthly Salary"
        );

        when(categoryService.getCategoryByNameAccessible("Salary", testUser)).thenReturn(salaryCategory);

        Transaction savedTx = new Transaction(testUser, salaryCategory, BigDecimal.valueOf(50000.00), LocalDate.now(), "Monthly Salary");
        savedTx.setId(100L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.createTransaction(testUser, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BigDecimal.valueOf(50000.00), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(CategoryType.INCOME, response.getType());
    }

    @Test
    void testCreateTransactionFutureDateThrowsBadRequest() {
        TransactionCreateRequest request = new TransactionCreateRequest(
                BigDecimal.valueOf(1000.00),
                LocalDate.now().plusDays(1),
                "Salary",
                "Future Salary"
        );

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(testUser, request));
    }

    @Test
    void testGetTransactionsFiltered() {
        Transaction tx1 = new Transaction(testUser, salaryCategory, BigDecimal.valueOf(50000.00), LocalDate.of(2024, 1, 15), "Salary");
        tx1.setId(1L);

        when(transactionRepository.findFilteredTransactions(testUser, null, null, null, null, null))
                .thenReturn(List.of(tx1));

        TransactionListResponse response = transactionService.getTransactions(testUser, null, null, null, null, null);

        assertEquals(1, response.getTransactions().size());
        assertEquals(1L, response.getTransactions().get(0).getId());
    }

    @Test
    void testUpdateTransactionSuccess() {
        Transaction existing = new Transaction(testUser, salaryCategory, BigDecimal.valueOf(50000.00), LocalDate.of(2024, 1, 15), "Salary");
        existing.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionUpdateRequest request = new TransactionUpdateRequest(
                BigDecimal.valueOf(60000.00),
                "Updated Salary",
                null,
                null
        );

        TransactionResponse response = transactionService.updateTransaction(testUser, 1L, request);

        assertEquals(BigDecimal.valueOf(60000.00), response.getAmount());
        assertEquals("Updated Salary", response.getDescription());
    }

    @Test
    void testUpdateTransactionDateIsIgnored() {
        Transaction existing = new Transaction(testUser, salaryCategory, BigDecimal.valueOf(50000.00), LocalDate.of(2024, 1, 15), "Salary");
        existing.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionUpdateRequest request = new TransactionUpdateRequest(
                BigDecimal.valueOf(60000.00),
                "Updated",
                null,
                LocalDate.of(2024, 1, 16)
        );

        TransactionResponse response = transactionService.updateTransaction(testUser, 1L, request);
        assertEquals(LocalDate.of(2024, 1, 15), response.getDate());
        assertEquals(BigDecimal.valueOf(60000.00), response.getAmount());
    }

    @Test
    void testUpdateTransactionOtherUserThrowsForbidden() {
        Transaction existing = new Transaction(otherUser, salaryCategory, BigDecimal.valueOf(50000.00), LocalDate.of(2024, 1, 15), "Salary");
        existing.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));

        TransactionUpdateRequest request = new TransactionUpdateRequest(BigDecimal.valueOf(60000.00), null, null, null);

        assertThrows(ForbiddenException.class, () -> transactionService.updateTransaction(testUser, 1L, request));
    }

    @Test
    void testDeleteTransactionSuccess() {
        Transaction existing = new Transaction(testUser, salaryCategory, BigDecimal.valueOf(50000.00), LocalDate.of(2024, 1, 15), "Salary");
        existing.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));

        MessageResponse response = transactionService.deleteTransaction(testUser, 1L);

        assertEquals("Transaction deleted successfully", response.getMessage());
        assertTrue(existing.getIsDeleted());
        verify(transactionRepository).save(existing);
    }
}
