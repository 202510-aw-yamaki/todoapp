package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> list(String keyword, String sortKey) {
        String safeSort = StringUtils.hasText(sortKey) ? sortKey : "createdAtDesc";
        return todoMapper.search(StringUtils.hasText(keyword) ? keyword : null, safeSort);
    }

    public Todo get(Long id) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            throw new IllegalArgumentException("Todo not found: " + id);
        }
        return todo;
    }

    public Todo create(Todo todo) {
        if (todo.getCreatedAt() == null) {
            todo.setCreatedAt(LocalDateTime.now());
        }
        todo.setCompleted(false);
        todoMapper.insert(todo);
        return todo;
    }

    public Todo update(Long id, Todo input) {
        Todo existing = get(id);
        existing.setAuthor(input.getAuthor());
        existing.setTitle(input.getTitle());
        existing.setDetail(input.getDetail());
        todoMapper.update(existing);
        return existing;
    }

    public void delete(Long id) {
        todoMapper.delete(id);
    }

    public void toggleCompleted(Long id) {
        Todo existing = get(id);
        boolean next = !existing.isCompleted();
        todoMapper.updateCompleted(id, next);
    }
}
