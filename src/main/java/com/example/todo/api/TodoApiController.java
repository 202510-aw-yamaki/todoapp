package com.example.todo.api;

import com.example.todo.entity.Todo;
import com.example.todo.service.TodoService;
import com.example.todo.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoApiController {

    private final TodoService todoService;
    private final UserService userService;

    public TodoApiController(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Todo>>> list(
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "categoryId", required = false) Long categoryId,
        @RequestParam(name = "author", required = false) List<String> authors,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "date", required = false) LocalDate date,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        boolean isAdmin = principal.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long userId = userService.findUserId(principal.getUsername());
        List<Todo> todos;
        if (date != null) {
            todos = todoService.listByCreatedDate(date, isAdmin ? null : userId);
        } else {
            todos = todoService.listAll(
                keyword,
                sort,
                categoryId,
                authors,
                resolveCompleted(status),
                isAdmin ? null : userId
            );
        }
        return ResponseEntity.ok(ApiResponse.ok("ok", todos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> get(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        Todo todo = todoService.get(id);
        return ResponseEntity.ok(ApiResponse.ok("ok", todo));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Todo>> create(
        @Valid @RequestBody TodoApiRequest request,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        Long userId = userService.findUserId(principal.getUsername());
        Todo created = todoService.create(toEntity(request, userId));
        return ResponseEntity.created(URI.create("/api/todos/" + created.getId()))
            .body(ApiResponse.ok("created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> update(
        @PathVariable Long id,
        @Valid @RequestBody TodoApiRequest request,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        boolean isAdmin = principal.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long userId = userService.findUserId(principal.getUsername());
        Todo entity = toEntity(request, isAdmin ? null : userId);
        if (isAdmin) {
            entity.setUserId(todoService.get(id).getUserId());
        }
        Todo updated = todoService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.ok("updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        todoService.delete(id);
        return ResponseEntity.status(204).body(ApiResponse.ok("deleted", null));
    }

    private Boolean resolveCompleted(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status) {
            case "completed" -> true;
            case "active" -> false;
            default -> null;
        };
    }

    private Todo toEntity(TodoApiRequest request, Long userId) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setAuthor(request.getAuthor());
        todo.setTitle(request.getTitle());
        todo.setDetail(request.getDetail());
        todo.setCategoryId(request.getCategoryId());
        todo.setDeadline(request.getDeadline());
        return todo;
    }
}
