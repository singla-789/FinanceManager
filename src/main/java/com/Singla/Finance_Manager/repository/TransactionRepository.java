package com.Singla.Finance_Manager.repository;

import com.Singla.Finance_Manager.entity.Category;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.Transaction;
import com.Singla.Finance_Manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserAndIsDeletedFalse(Long id, User user);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    long countByCategoryAndIsDeletedFalse(Category category);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.isDeleted = false " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:type IS NULL OR t.category.type = :type) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findFilteredTransactions(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            @Param("type") CategoryType type
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = :type AND t.date >= :startDate AND t.isDeleted = false")
    BigDecimal sumAmountByUserAndTypeAndDateAfterEqual(
            @Param("user") User user,
            @Param("type") CategoryType type,
            @Param("startDate") LocalDate startDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.isDeleted = false " +
           "AND t.date >= :startDate AND t.date <= :endDate")
    List<Transaction> findByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
