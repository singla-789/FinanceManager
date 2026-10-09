package com.Singla.Finance_Manager.dto.category;

import com.Singla.Finance_Manager.entity.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryDto {

    private String name;
    private CategoryType type;

    @JsonProperty("isCustom")
    private Boolean isCustom;

    public CategoryDto() {
    }

    public CategoryDto(String name, CategoryType type, Boolean isCustom) {
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
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

    @JsonProperty("isCustom")
    public Boolean getIsCustom() {
        return isCustom;
    }

    @JsonProperty("isCustom")
    public void setIsCustom(Boolean custom) {
        isCustom = custom;
    }
}
