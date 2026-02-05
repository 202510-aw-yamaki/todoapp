package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public Page<Todo> list(String keyword, String sortKey, Long categoryId, int page, int size) {
        String safeSort = normalizeSort(sortKey);
        int safePage = Math.max(page, 0);
        int safeSize = resolveSize(size);
        int offset = safePage * safeSize;
        String safeKeyword = StringUtils.hasText(keyword) ? keyword : null;
        int total = todoMapper.count(safeKeyword, categoryId);
        List<Todo> rows = todoMapper.searchPage(safeKeyword, safeSort, safeSize, offset, categoryId);
        return new PageImpl<>(rows, PageRequest.of(safePage, safeSize), total);
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
        if (todo.getCategoryId() == null) {
            todo.setCategoryId(1L);
        }
        todoMapper.insert(todo);
        return todo;
    }

    public Todo update(Long id, Todo input) {
        Todo existing = get(id);
        existing.setAuthor(input.getAuthor());
        existing.setTitle(input.getTitle());
        existing.setDetail(input.getDetail());
        existing.setCategoryId(input.getCategoryId());
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

    public String normalizeSort(String sortKey) {
        if (!StringUtils.hasText(sortKey)) {
            return "createdAtDesc";
        }
        return switch (sortKey) {
            case "createdAtAsc",
                 "createdAtDesc",
                 "titleAsc",
                 "titleDesc",
                 "completedAsc",
                 "completedDesc",
                 "deadlineAsc",
                 "deadlineDesc" -> sortKey;
            default -> "createdAtDesc";
        };
    }

    public int resolveSize(int size) {
        return switch (size) {
            case 10, 20, 30, 50, 100 -> size;
            default -> 10;
        };
    }
}
