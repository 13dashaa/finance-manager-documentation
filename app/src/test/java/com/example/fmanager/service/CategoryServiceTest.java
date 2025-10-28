package com.example.fmanager.service;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.example.fmanager.dto.CategoryCreateDto;
import com.example.fmanager.dto.CategoryGetDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Category;
import com.example.fmanager.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1);
        category1.setName("Food");

        category2 = new Category();
        category2.setId(2);
        category2.setName("Transport");
    }

    @Test
    void findAll_Success() {
        String cacheKey = "all_categories";
        when(cache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        List<CategoryGetDto> result = categoryService.findAll();

        assertEquals(2, result.size());
        assertEquals("Food", result.get(0).getName());
        verify(cache, times(1)).put(eq(cacheKey), any());
    }

    @Test
    void findById_Success() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category1));

        Optional<CategoryGetDto> result = categoryService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Food", result.get().getName());
    }

    @Test
    void findById_NotFound() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.findById(1));
    }

    @Test
    void createCategory_Success() {
        CategoryCreateDto createDto = new CategoryCreateDto("New Category");
        Category newCategory = new Category();
        newCategory.setId(3);
        newCategory.setName("New Category");

        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

        Category result = categoryService.createCategory(createDto);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
        verify(cache, times(1)).remove("all_categories");
    }

    @Test
    void updateCategory_Success() {
        CategoryCreateDto updateDto = new CategoryCreateDto("Updated Category");
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category1));
        when(categoryRepository.save(any(Category.class))).thenReturn(category1);

        CategoryGetDto result = categoryService.updateCategory(1, updateDto);

        assertEquals("Updated Category", result.getName());
        verify(cache, times(1)).remove("all_categories");
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category1));
        doNothing().when(categoryRepository).delete(category1);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1));
        verify(cache, times(1)).remove("all_categories");
        verify(categoryRepository, times(1)).delete(category1);
    }

    @Test
    void deleteCategory_NotFound() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(1));
    }
}
