package com.example.be_qltv.service;

import com.example.be_qltv.dto.CategoryDTO;
import com.example.be_qltv.entity.Category;
import com.example.be_qltv.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<CategoryDTO> getAllCategoriesWithPagination(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        List<CategoryDTO> categoryDTOs = categories.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(categoryDTOs, pageable, categories.getTotalElements());
    }
    
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public Optional<CategoryDTO> getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .map(this::convertToDTO);
    }
    
    public List<CategoryDTO> searchCategories(String query) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<String> getCategorySuggestions(String query) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        return categories.stream()
                .map(Category::getName)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }
    
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }
    
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private CategoryDTO convertToDTO(Category category) {
    CategoryDTO dto = new CategoryDTO();
    dto.setId(category.getId());
    dto.setName(category.getName());
    dto.setDescription(category.getDescription());
    dto.setBookCount(category.getBooks() != null ? category.getBooks().size() : 0);
    dto.setCreatedDate(category.getCreatedDate());
    return dto;
    }
    
    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        if (dto.getId() != null) {
            category.setId(dto.getId());
        }
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}
