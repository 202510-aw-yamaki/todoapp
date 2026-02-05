package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> list(String keyword, String sortKey) {
        Sort sort = resolveSort(sortKey);
        if (StringUtils.hasText(keyword)) {
            return todoRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, sort);
        }
        return todoRepository.findAll(sort);
    }

    public Todo get(Long id) {
        return todoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Todo not found: " + id));
    }

    public Todo create(Todo todo) {
        if (todo.getCreatedAt() == null) {
            todo.setCreatedAt(LocalDateTime.now());
        }
        return todoRepository.save(todo);
    }

    public Todo update(Long id, Todo input) {
        Todo existing = get(id);
        existing.setAuthor(input.getAuthor());
        existing.setTitle(input.getTitle());
        existing.setDetail(input.getDetail());
        return todoRepository.save(existing);
    }

    public void delete(Long id) {
        todoRepository.deleteById(id);
    }

    private Sort resolveSort(String sortKey) {
        if (!StringUtils.hasText(sortKey)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sortKey) {
            case "createdAtAsc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "titleAsc" -> Sort.by(Sort.Direction.ASC, "title");
            case "authorAsc" -> Sort.by(Sort.Direction.ASC, "author");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
