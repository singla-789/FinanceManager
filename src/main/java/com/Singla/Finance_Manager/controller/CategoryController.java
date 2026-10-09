package com.Singla.Finance_Manager.controller;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.category.CategoryDto;
import com.Singla.Finance_Manager.dto.category.CategoryListResponse;
import com.Singla.Finance_Manager.dto.category.CategoryRequest;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.service.CategoryService;
import com.Singla.Finance_Manager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories() {
        User user = userService.getCurrentAuthenticatedUser();
        CategoryListResponse response = categoryService.getAllCategories(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        CategoryDto response = categoryService.createCustomCategory(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable String name) {
        User user = userService.getCurrentAuthenticatedUser();
        MessageResponse response = categoryService.deleteCategory(user, name);
        return ResponseEntity.ok(response);
    }
}
