package com.example.todo.repository;

import com.example.todo.entity.Todo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {
    List<Todo> search(@Param("keyword") String keyword, @Param("sort") String sort);

    Todo findById(@Param("id") Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int delete(@Param("id") Long id);

    int updateCompleted(@Param("id") Long id, @Param("completed") boolean completed);
}
