package com.example.todo.repository;

import com.example.todo.entity.Category;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {
    List<Category> findAll();

    Category findById(Long id);
}
