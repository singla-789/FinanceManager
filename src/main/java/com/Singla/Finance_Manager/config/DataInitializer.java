package com.Singla.Finance_Manager.config;

import com.Singla.Finance_Manager.entity.Category;
import com.Singla.Finance_Manager.entity.CategoryType;
import com.Singla.Finance_Manager.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds predefined default categories into the database upon application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedDefaultCategories();
    }

    private void seedDefaultCategories() {
        // Default Income categories
        seedCategoryIfNotExists("Salary", CategoryType.INCOME);

        // Default Expense categories
        List<String> defaultExpenses = List.of(
                "Food",
                "Rent",
                "Transportation",
                "Entertainment",
                "Healthcare",
                "Utilities"
        );

        for (String expenseCategory : defaultExpenses) {
            seedCategoryIfNotExists(expenseCategory, CategoryType.EXPENSE);
        }
    }

    private void seedCategoryIfNotExists(String name, CategoryType type) {
        if (!categoryRepository.existsByNameAndUserIsNull(name)) {
            Category category = new Category(name, type, false, null);
            categoryRepository.save(category);
        }
    }
}
