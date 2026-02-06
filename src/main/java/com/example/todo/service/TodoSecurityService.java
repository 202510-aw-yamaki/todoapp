package com.example.todo.service;

import com.example.todo.repository.TodoAttachmentMapper;
import com.example.todo.repository.TodoMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class TodoSecurityService {

    private final TodoMapper todoMapper;
    private final TodoAttachmentMapper attachmentMapper;
    private final UserService userService;

    public TodoSecurityService(TodoMapper todoMapper, TodoAttachmentMapper attachmentMapper, UserService userService) {
        this.todoMapper = todoMapper;
        this.attachmentMapper = attachmentMapper;
        this.userService = userService;
    }

    public boolean isOwner(Long id, Object principal) {
        if (id == null || principal == null) {
            return false;
        }
        if (!(principal instanceof UserDetails userDetails)) {
            return false;
        }
        Long ownerId = todoMapper.findOwnerId(id);
        if (ownerId == null) {
            return false;
        }
        Long userId = userService.findUserId(userDetails.getUsername());
        return ownerId.equals(userId);
    }

    public boolean isOwnerOrAdmin(Long id, Object principal) {
        if (principal instanceof UserDetails userDetails) {
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return true;
            }
        }
        return isOwner(id, principal);
    }

    public boolean isAttachmentOwnerOrAdmin(Long attachmentId, Object principal) {
        if (attachmentId == null) {
            return false;
        }
        Long todoId = attachmentMapper.findTodoId(attachmentId);
        if (todoId == null) {
            return false;
        }
        return isOwnerOrAdmin(todoId, principal);
    }
}
