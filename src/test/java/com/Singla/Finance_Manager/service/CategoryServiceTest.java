package com.Singla.Finance_Manager.service;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.category.CategoryDto;
import com.Singla.Finance_Manager.dto.category.CategoryListResponse;
import com.Singla.Finance_Manager.dto.category.CategoryRequest;
import com.Singla.Finance_Manager.entity.Category;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.exception.BadRequestException;
import com.Singla.Finance_Manager.exception.DuplicateResourceException;
import com.Singla.Finance_Manager.exception.ForbiddenException;
import com.Singla.Finance_Manager.exception.ResourceNotFoundException;
import com.Singla.Finance_Manager.repository.CategoryRepository;
import com.Singla.Finance_Manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "pass", "Test User", "+12345");
        testUser.setId(1L);
    }

    @Test
    void testGetAllCategories() {
        Category salary = new Category("Salary", CategoryType.INCOME, false, null);
        Category freelance = new Category("Freelance", CategoryType.INCOME, true, testUser);

        when(categoryRepository.findAllAccessibleByUser(testUser)).thenReturn(List.of(salary, freelance));

        CategoryListResponse response = categoryService.getAllCategories(testUser);

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
        assertEquals("Salary", response.getCategories().get(0).getName());
        assertFalse(response.getCategories().get(0).getIsCustom());
        assertEquals("Freelance", response.getCategories().get(1).getName());
        assertTrue(response.getCategories().get(1).getIsCustom());
    }

    @Test
    void testCreateCustomCategorySuccess() {
        CategoryRequest request = new CategoryRequest("Freelance", CategoryType.INCOME);
        when(categoryRepository.existsByNameAndUserIsNull("Freelance")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("Freelance", testUser)).thenReturn(false);

        Category savedCategory = new Category("Freelance", CategoryType.INCOME, true, testUser);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryDto dto = categoryService.createCustomCategory(testUser, request);

        assertNotNull(dto);
        assertEquals("Freelance", dto.getName());
        assertEquals(CategoryType.INCOME, dto.getType());
        assertTrue(dto.getIsCustom());
    }

    @Test
    void testCreateCustomCategoryDuplicateDefaultNameThrowsConflict() {
        CategoryRequest request = new CategoryRequest("Salary", CategoryType.INCOME);
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCustomCategory(testUser, request));
    }

    @Test
    void testCreateCustomCategoryDuplicateCustomNameThrowsConflict() {
        CategoryRequest request = new CategoryRequest("Freelance", CategoryType.INCOME);
        when(categoryRepository.existsByNameAndUserIsNull("Freelance")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("Freelance", testUser)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCustomCategory(testUser, request));
    }

    @Test
    void testDeleteCategoryDefaultThrowsForbidden() {
        when(categoryRepository.findByNameAndUserIsNull("Salary"))
                .thenReturn(Optional.of(new Category("Salary", CategoryType.INCOME, false, null)));

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCategory(testUser, "Salary"));
    }

    @Test
    void testDeleteCategoryNotFoundThrowsNotFound() {
        when(categoryRepository.findByNameAndUserIsNull("NonExistent")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("NonExistent", testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(testUser, "NonExistent"));
    }

    @Test
    void testDeleteCategoryReferencedByTransactionsThrowsBadRequest() {
        Category customCat = new Category("Freelance", CategoryType.INCOME, true, testUser);
        when(categoryRepository.findByNameAndUserIsNull("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Freelance", testUser)).thenReturn(Optional.of(customCat));
        when(transactionRepository.countByCategoryAndIsDeletedFalse(customCat)).thenReturn(3L);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory(testUser, "Freelance"));
    }

    @Test
    void testDeleteCategorySuccess() {
        Category customCat = new Category("Freelance", CategoryType.INCOME, true, testUser);
        when(categoryRepository.findByNameAndUserIsNull("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Freelance", testUser)).thenReturn(Optional.of(customCat));
        when(transactionRepository.countByCategoryAndIsDeletedFalse(customCat)).thenReturn(0L);

        MessageResponse response = categoryService.deleteCategory(testUser, "Freelance");

        assertEquals("Category deleted successfully", response.getMessage());
        verify(categoryRepository).delete(customCat);
    }
}
