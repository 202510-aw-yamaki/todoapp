package com.example.todo.repository;

import com.example.todo.entity.Todo;
import com.example.todo.view.TodoDateCount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {
    int count(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("authors") List<String> authors,
        @Param("completed") Boolean completed,
        @Param("userId") Long userId
    );

    List<Todo> searchPage(
        @Param("keyword") String keyword,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset,
        @Param("categoryId") Long categoryId,
        @Param("authors") List<String> authors,
        @Param("completed") Boolean completed,
        @Param("userId") Long userId
    );

    List<Todo> searchAll(
        @Param("keyword") String keyword,
        @Param("sort") String sort,
        @Param("categoryId") Long categoryId,
        @Param("authors") List<String> authors,
        @Param("completed") Boolean completed,
        @Param("userId") Long userId
    );

    List<String> findAuthors(@Param("userId") Long userId);

    Long findOwnerId(@Param("id") Long id);

    Todo findById(@Param("id") Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int delete(@Param("id") Long id);

    int updateCompleted(@Param("id") Long id, @Param("completed") boolean completed);

    int deleteBatch(@Param("ids") List<Long> ids);

    List<TodoDateCount> countByCreatedDateRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("userId") Long userId
    );

    List<Todo> findByCreatedDate(
        @Param("date") LocalDate date,
        @Param("userId") Long userId
    );
}
