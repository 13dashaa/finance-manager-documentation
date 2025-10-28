package com.example.fmanager.service;

import static com.example.fmanager.exception.NotFoundMessages.CATEGORY_NOT_FOUND_MESSAGE;

import com.example.fmanager.dto.CategoryCreateDto;
import com.example.fmanager.dto.CategoryGetDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Category;
import com.example.fmanager.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final InMemoryCache cache;

    public CategoryService(CategoryRepository categoryRepository, InMemoryCache cache) {
        this.categoryRepository = categoryRepository;
        this.cache = cache;
    }

    public List<CategoryGetDto> findAll() {
        String cacheKey = "all_categories";
        if (cache.containsKey(cacheKey)) {
            return (List<CategoryGetDto>) cache.get(cacheKey);
        }
        List<Category> categories = categoryRepository.findAll();
        List<CategoryGetDto> categoryGetDtos = new ArrayList<>();
        for (Category category : categories) {
            categoryGetDtos.add(CategoryGetDto.convertToDto(category));
        }
        cache.put(cacheKey, categoryGetDtos);
        return categoryGetDtos;
    }

    public Optional<CategoryGetDto> findById(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE));
        return Optional.of(CategoryGetDto.convertToDto(category));
    }

    public Category createCategory(CategoryCreateDto categoryCreateDto) {
        Category category = new Category();
        category.setName(categoryCreateDto.getName());
        Category savedCategory = categoryRepository.save(category);
        clearCategoryCache();
        return savedCategory;
    }

    @Transactional
    public CategoryGetDto updateCategory(int id, CategoryCreateDto categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE));
        category.setName(categoryDetails.getName());
        Category savedCategory = categoryRepository.save(category);
        clearCategoryCache();
        return CategoryGetDto.convertToDto(savedCategory);
    }

    @Transactional
    public void deleteCategory(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE));
        clearCategoryCache();
        categoryRepository.delete(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void clearCategoryCache() {
        String cacheKey = "all_categories";
        cache.remove(cacheKey);
    }
}
