package com.example.todo.service;

import com.example.todo.entity.Category;
import com.example.todo.repository.CategoryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> list() {
        return categoryMapper.findAll();
    }

    public Category get(Long id) {
        return categoryMapper.findById(id);
    }
}
