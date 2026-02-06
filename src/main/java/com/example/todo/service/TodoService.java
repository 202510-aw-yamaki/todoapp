package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoHistoryMapper;
import com.example.todo.repository.TodoMapper;
import com.example.todo.entity.TodoHistory;
import com.example.todo.view.DayView;
import com.example.todo.view.TodoDateCount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TodoService {

    private final TodoMapper todoMapper;
    private final AuditLogService auditLogService;
    private final TodoHistoryMapper todoHistoryMapper;
    private final UserService userService;

    public TodoService(TodoMapper todoMapper, AuditLogService auditLogService, TodoHistoryMapper todoHistoryMapper, UserService userService) {
        this.todoMapper = todoMapper;
        this.auditLogService = auditLogService;
        this.todoHistoryMapper = todoHistoryMapper;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
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

    @org.springframework.security.access.prepost.PreAuthorize("@todoSecurityService.isOwnerOrAdmin(#id, principal)")
    @Transactional(readOnly = true)
    public Todo get(Long id) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            throw new com.example.todo.exception.TodoNotFoundException("Todo not found: " + id);
        }
        return todo;
    }

    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public Todo update(Long id, Todo input) {
        Todo existing = get(id);
        recordAdminEditHistory(existing);
        existing.setAuthor(input.getAuthor());
        existing.setTitle(input.getTitle());
        existing.setDetail(input.getDetail());
        existing.setCategoryId(input.getCategoryId());
        existing.setDeadline(input.getDeadline());
        todoMapper.update(existing);
        recordHistory(existing.getId(), "UPDATE");
        return existing;
    }

    @org.springframework.security.access.prepost.PreAuthorize("@todoSecurityService.isOwnerOrAdmin(#id, principal)")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        todoMapper.delete(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            delete(id);
        }
    }

    @org.springframework.security.access.prepost.PreAuthorize("@todoSecurityService.isOwnerOrAdmin(#id, principal)")
    @Transactional(rollbackFor = Exception.class)
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
        auditLogService.logAdminEdit(existing.getId(), editorId);
    }

    private void recordHistory(Long todoId, String note) {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
            ? null
            : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
            return;
        }
        Long editorId = userService.findUserId(userDetails.getUsername());
        if (editorId == null) {
            return;
        }
        TodoHistory history = new TodoHistory();
        history.setTodoId(todoId);
        history.setEditorUserId(editorId);
        history.setEditedAt(LocalDateTime.now());
        history.setNote(note);
        todoHistoryMapper.insert(history);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public int resolveSize(int size) {
        return switch (size) {
            case 10, 20, 30, 50, 100 -> size;
            default -> 10;
        };
    }

    @Transactional(readOnly = true)
    public List<String> listAuthors(Long userId, boolean isAdmin) {
        return todoMapper.findAuthors(isAdmin ? null : userId);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<DayView> buildMonthDays(YearMonth month, Long userId) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<TodoDateCount> counts = todoMapper.countByCreatedDateRange(
            start.atStartOfDay(),
            end.plusDays(1).atStartOfDay(),
            userId
        );
        Map<LocalDate, Integer> countMap = counts.stream()
            .collect(Collectors.toMap(TodoDateCount::getDate, TodoDateCount::getCount));
        return start.datesUntil(end.plusDays(1))
            .map(d -> new DayView(d, d.getDayOfMonth(), countMap.getOrDefault(d, 0)))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Todo> listByCreatedDate(LocalDate date, Long userId) {
        if (date == null) {
            return List.of();
        }
        return todoMapper.findByCreatedDate(date, userId);
    }
}
