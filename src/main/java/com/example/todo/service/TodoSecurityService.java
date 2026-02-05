package com.example.todo.service;

import com.example.todo.repository.TodoMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class TodoSecurityService {

    private final TodoMapper todoMapper;
    private final UserService userService;

    public TodoSecurityService(TodoMapper todoMapper, UserService userService) {
        this.todoMapper = todoMapper;
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
}
