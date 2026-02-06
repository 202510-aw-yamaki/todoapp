package com.example.todo.service;

import com.example.todo.entity.TodoHistory;
import com.example.todo.repository.TodoHistoryMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final TodoHistoryMapper todoHistoryMapper;

    public AuditLogService(TodoHistoryMapper todoHistoryMapper) {
        this.todoHistoryMapper = todoHistoryMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void logAdminEdit(Long todoId, Long editorUserId) {
        TodoHistory history = new TodoHistory();
        history.setTodoId(todoId);
        history.setEditorUserId(editorUserId);
        history.setEditedAt(LocalDateTime.now());
        history.setNote("ADMIN_EDIT");
        todoHistoryMapper.insert(history);
    }
}
