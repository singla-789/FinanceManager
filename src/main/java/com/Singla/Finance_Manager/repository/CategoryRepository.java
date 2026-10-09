package com.Singla.Finance_Manager.repository;

import com.Singla.Finance_Manager.entity.Category;
import com.Singla.Finance_Manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndUserIsNull(String name);

    Optional<Category> findByNameAndUser(String name, User user);

    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user ORDER BY c.id ASC")
    List<Category> findAllAccessibleByUser(@Param("user") User user);

    boolean existsByNameAndUserIsNull(String name);

    boolean existsByNameAndUser(String name, User user);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user IS NULL OR c.user = :user)")
    Optional<Category> findByNameAccessibleByUser(@Param("name") String name, @Param("user") User user);
}
