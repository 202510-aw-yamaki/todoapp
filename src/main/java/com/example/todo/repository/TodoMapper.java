package com.example.todo.repository;

import com.example.todo.entity.Todo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {
    int count(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    List<Todo> searchPage(
        @Param("keyword") String keyword,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset,
        @Param("categoryId") Long categoryId
    );

    Todo findById(@Param("id") Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int delete(@Param("id") Long id);

    int updateCompleted(@Param("id") Long id, @Param("completed") boolean completed);

    int deleteBatch(@Param("ids") List<Long> ids);
}
