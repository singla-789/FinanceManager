package com.Singla.Finance_Manager.service;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.category.CategoryDto;
import com.Singla.Finance_Manager.dto.category.CategoryListResponse;
import com.Singla.Finance_Manager.dto.category.CategoryRequest;
import com.Singla.Finance_Manager.entity.Category;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.exception.BadRequestException;
import com.Singla.Finance_Manager.exception.DuplicateResourceException;
import com.Singla.Finance_Manager.exception.ForbiddenException;
import com.Singla.Finance_Manager.exception.ResourceNotFoundException;
import com.Singla.Finance_Manager.repository.CategoryRepository;
import com.Singla.Finance_Manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public CategoryListResponse getAllCategories(User user) {
        List<Category> categories = categoryRepository.findAllAccessibleByUser(user);
        List<CategoryDto> dtos = categories.stream()
                .map(c -> new CategoryDto(c.getName(), c.getType(), c.getIsCustom()))
                .collect(Collectors.toList());
        return new CategoryListResponse(dtos);
    }

    @Transactional
    public CategoryDto createCustomCategory(User user, CategoryRequest request) {
        String trimmedName = request.getName().trim();

        // Check if category name conflicts with default categories
        if (categoryRepository.existsByNameAndUserIsNull(trimmedName)) {
            throw new DuplicateResourceException("A default category with name '" + trimmedName + "' already exists");
        }

        // Check if category name conflicts with existing custom categories for this user
        if (categoryRepository.existsByNameAndUser(trimmedName, user)) {
            throw new DuplicateResourceException("Category '" + trimmedName + "' already exists for user");
        }

        Category customCategory = new Category(
                trimmedName,
                request.getType(),
                true,
                user
        );

        Category saved = categoryRepository.save(customCategory);
        return new CategoryDto(saved.getName(), saved.getType(), saved.getIsCustom());
    }

    @Transactional
    public MessageResponse deleteCategory(User user, String categoryName) {
        String trimmedName = categoryName.trim();

        // Check if it is a default category
        Optional<Category> defaultCatOpt = categoryRepository.findByNameAndUserIsNull(trimmedName);
        if (defaultCatOpt.isPresent()) {
            throw new ForbiddenException("Default categories cannot be deleted or modified");
        }

        // Find user's custom category
        Category customCategory = categoryRepository.findByNameAndUser(trimmedName, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category '" + trimmedName + "' not found"));

        // Check if referenced by active transactions
        long refCount = transactionRepository.countByCategoryAndIsDeletedFalse(customCategory);
        if (refCount > 0) {
            throw new BadRequestException("Category '" + trimmedName + "' cannot be deleted as it is currently referenced by transactions");
        }

        categoryRepository.delete(customCategory);
        return new MessageResponse("Category deleted successfully");
    }

    @Transactional(readOnly = true)
    public Category getCategoryByNameAccessible(String name, User user) {
        String trimmedName = name.trim();
        return categoryRepository.findByNameAccessibleByUser(trimmedName, user)
                .orElseThrow(() -> new BadRequestException("Category '" + trimmedName + "' is invalid or not accessible"));
    }
}
