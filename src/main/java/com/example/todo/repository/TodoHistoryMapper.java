package com.example.todo.repository;

import com.example.todo.entity.TodoHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoHistoryMapper {
    int insert(TodoHistory history);

    int deleteByTodoId(@Param("todoId") Long todoId);
}
