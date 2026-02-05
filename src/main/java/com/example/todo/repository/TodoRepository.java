package com.example.todo.repository;

import com.example.todo.entity.Todo;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author, Sort sort);
}
