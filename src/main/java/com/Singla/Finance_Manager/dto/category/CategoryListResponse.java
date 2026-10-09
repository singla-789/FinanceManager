package com.Singla.Finance_Manager.dto.category;

import java.util.ArrayList;
import java.util.List;

public class CategoryListResponse {

    private List<CategoryDto> categories = new ArrayList<>();

    public CategoryListResponse() {
    }

    public CategoryListResponse(List<CategoryDto> categories) {
        this.categories = categories;
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }
}
