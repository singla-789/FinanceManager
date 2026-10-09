package com.Singla.Finance_Manager.controller;

import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.category.CategoryDto;
import com.Singla.Finance_Manager.dto.category.CategoryListResponse;
import com.Singla.Finance_Manager.dto.category.CategoryRequest;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.service.CategoryService;
import com.Singla.Finance_Manager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "User", "+123");
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
    }

    @Test
    @WithMockUser
    void testGetAllCategories() throws Exception {
        CategoryDto salary = new CategoryDto("Salary", CategoryType.INCOME, false);
        when(categoryService.getAllCategories(testUser)).thenReturn(new CategoryListResponse(List.of(salary)));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Salary"));
    }

    @Test
    @WithMockUser
    void testCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Freelance", CategoryType.INCOME);
        CategoryDto dto = new CategoryDto("Freelance", CategoryType.INCOME, true);
        when(categoryService.createCustomCategory(eq(testUser), any(CategoryRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    @WithMockUser
    void testDeleteCategory() throws Exception {
        when(categoryService.deleteCategory(eq(testUser), eq("Freelance")))
                .thenReturn(new MessageResponse("Category deleted successfully"));

        mockMvc.perform(delete("/api/categories/Freelance")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }
}
