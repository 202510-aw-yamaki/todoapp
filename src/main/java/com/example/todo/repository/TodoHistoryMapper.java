package com.example.todo.repository;

import com.example.todo.entity.TodoHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoHistoryMapper {
    int insert(TodoHistory history);
}
