package com.Singla.Finance_Manager.dto.category;

import com.Singla.Finance_Manager.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryRequest {

    @NotBlank(message = "Category name is mandatory")
    private String name;

    @NotNull(message = "Category type is mandatory (INCOME or EXPENSE)")
    private CategoryType type;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, CategoryType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }
}
