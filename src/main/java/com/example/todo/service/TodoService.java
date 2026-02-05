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
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.todo.entity.TodoHistory;
import com.example.todo.repository.TodoHistoryMapper;

@Service
public class TodoService {

    private final TodoMapper todoMapper;
    private final TodoHistoryMapper todoHistoryMapper;
    private final UserService userService;

    public TodoService(TodoMapper todoMapper, TodoHistoryMapper todoHistoryMapper, UserService userService) {
        this.todoMapper = todoMapper;
        this.todoHistoryMapper = todoHistoryMapper;
        this.userService = userService;
    }

    public Page<Todo> list(
        String keyword,
        String sortKey,
        Long categoryId,
        List<String> authors,
        Boolean completed,
        Long userId,
        int page,
        int size
    ) {
        String safeSort = normalizeSort(sortKey);
        int safePage = Math.max(page, 0);
        int safeSize = resolveSize(size);
        int offset = safePage * safeSize;
        String safeKeyword = StringUtils.hasText(keyword) ? keyword : null;
        List<String> safeAuthors = (authors == null || authors.isEmpty()) ? null : authors;
        int total = todoMapper.count(safeKeyword, categoryId, safeAuthors, completed, userId);
        List<Todo> rows = todoMapper.searchPage(safeKeyword, safeSort, safeSize, offset, categoryId, safeAuthors, completed, userId);
        return new PageImpl<>(rows, PageRequest.of(safePage, safeSize), total);
    }

    public List<Todo> listAll(
        String keyword,
        String sortKey,
        Long categoryId,
        List<String> authors,
        Boolean completed,
        Long userId
    ) {
        String safeSort = normalizeSort(sortKey);
        String safeKeyword = StringUtils.hasText(keyword) ? keyword : null;
        List<String> safeAuthors = (authors == null || authors.isEmpty()) ? null : authors;
        return todoMapper.searchAll(safeKeyword, safeSort, categoryId, safeAuthors, completed, userId);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@todoSecurityService.isOwnerOrAdmin(#id, principal)")
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

    @org.springframework.security.access.prepost.PreAuthorize("@todoSecurityService.isOwnerOrAdmin(#id, principal)")
    public Todo update(Long id, Todo input) {
        Todo existing = get(id);
        recordAdminEditHistory(existing);
        existing.setAuthor(input.getAuthor());
        existing.setTitle(input.getTitle());
        existing.setDetail(input.getDetail());
        existing.setCategoryId(input.getCategoryId());
        existing.setDeadline(input.getDeadline());
        todoMapper.update(existing);
        return existing;
    }

    @org.springframework.security.access.prepost.PreAuthorize("@todoSecurityService.isOwnerOrAdmin(#id, principal)")
    public void delete(Long id) {
        todoMapper.delete(id);
    }

    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            delete(id);
        }
    }

    @org.springframework.security.access.prepost.PreAuthorize("@todoSecurityService.isOwnerOrAdmin(#id, principal)")
    public void toggleCompleted(Long id) {
        Todo existing = get(id);
        boolean next = !existing.isCompleted();
        todoMapper.updateCompleted(id, next);
    }

    private void recordAdminEditHistory(Todo existing) {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
            ? null
            : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
            return;
        }
        boolean isAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return;
        }
        Long editorId = userService.findUserId(userDetails.getUsername());
        if (editorId == null || editorId.equals(existing.getUserId())) {
            return;
        }
        TodoHistory history = new TodoHistory();
        history.setTodoId(existing.getId());
        history.setEditorUserId(editorId);
        history.setEditedAt(LocalDateTime.now());
        history.setNote("ADMIN edited other user's todo");
        todoHistoryMapper.insert(history);
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

    public List<String> listAuthors(Long userId, boolean isAdmin) {
        return todoMapper.findAuthors(isAdmin ? null : userId);
    }
}
