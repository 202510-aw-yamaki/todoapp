package com.example.todo.repository;

import com.example.todo.entity.TodoAttachment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoAttachmentMapper {

    int insert(TodoAttachment attachment);

    List<TodoAttachment> findByTodoId(@Param("todoId") Long todoId);

    TodoAttachment findById(@Param("id") Long id);

    int delete(@Param("id") Long id);

    Long findTodoId(@Param("id") Long id);
}
