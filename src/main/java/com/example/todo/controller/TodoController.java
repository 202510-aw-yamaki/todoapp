package com.example.todo.controller;

import com.example.todo.entity.Category;
import com.example.todo.entity.Todo;
import com.example.todo.form.TodoForm;
import com.example.todo.service.CategoryService;
import com.example.todo.service.TodoService;
import com.example.todo.service.UserService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.chrono.JapaneseDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
public class TodoController {

    private final TodoService todoService;
    private final CategoryService categoryService;
    private final UserService userService;

    public TodoController(TodoService todoService, CategoryService categoryService, UserService userService) {
        this.todoService = todoService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/todos";
    }

    @GetMapping("/todos")
    public String index(
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "categoryId", required = false) Long categoryId,
        @RequestParam(name = "author", required = false) List<String> authors,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @RequestParam(name = "size", required = false, defaultValue = "10") int size,
        @AuthenticationPrincipal UserDetails principal,
        Model model
    ) {
        if (principal == null) {
            return "redirect:/login";
        }
        String safeSort = todoService.normalizeSort(sort);
        int safeSize = todoService.resolveSize(size);
        Boolean completed = resolveCompleted(status);
        Long userId = userService.findUserId(principal.getUsername());
        Page<Todo> todoPage = todoService.list(keyword, safeSort, categoryId, authors, completed, userId, page, safeSize);
        List<Todo> todos = todoPage.getContent();
        model.addAttribute("todos", todos);
        model.addAttribute("q", keyword);
        model.addAttribute("sort", safeSort);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("author", authors);
        model.addAttribute("status", status);
        model.addAttribute("authors", todoService.listAuthors(userId));
        model.addAttribute("categories", categoryService.list());
        model.addAttribute("count", todoPage.getTotalElements());
        model.addAttribute("page", todoPage.getNumber());
        model.addAttribute("size", todoPage.getSize());
        model.addAttribute("totalPages", todoPage.getTotalPages());
        model.addAttribute("start", todos.isEmpty() ? 0 : (todoPage.getNumber() * todoPage.getSize() + 1));
        model.addAttribute("end", todos.isEmpty() ? 0 : (todoPage.getNumber() * todoPage.getSize() + todos.size()));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("nearLimit", LocalDate.now().plusDays(3));
        return "index";
    }

    @GetMapping("/todos/new")
    public String createForm(Model model) {
        model.addAttribute("todoForm", new TodoForm());
        model.addAttribute("categories", categoryService.list());
        model.addAttribute("mode", "create");
        return "create";
    }

    @GetMapping("/todos/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Todo todo = todoService.get(id);
        model.addAttribute("todoForm", toForm(todo));
        model.addAttribute("categories", categoryService.list());
        model.addAttribute("mode", "edit");
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirm(
        @Valid @ModelAttribute("todoForm") TodoForm todoForm,
        BindingResult bindingResult,
        Model model
    ) {
        String mode = todoForm.getId() == null ? "create" : "edit";
        model.addAttribute("mode", mode);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.list());
            return "create";
        }
        Category category = categoryService.get(todoForm.getCategoryId());
        model.addAttribute("category", category);
        model.addAttribute("deadlineLabel", formatDeadline(todoForm.getDeadline()));
        return "confirm";
    }

    @PostMapping("/todos/back")
    public String back(@ModelAttribute("todoForm") TodoForm todoForm, @RequestParam("mode") String mode, Model model) {
        model.addAttribute("mode", mode);
        model.addAttribute("categories", categoryService.list());
        return "create";
    }

    @PostMapping("/todos/complete")
    public String complete(
        @ModelAttribute("todoForm") TodoForm todoForm,
        @RequestParam("mode") String mode,
        @AuthenticationPrincipal UserDetails principal,
        RedirectAttributes redirectAttributes
    ) {
        Todo saved;
        if ("edit".equals(mode)) {
            saved = todoService.update(todoForm.getId(), toEntity(todoForm, userService.findUserId(principal.getUsername())));
        } else {
            saved = todoService.create(toEntity(todoForm, userService.findUserId(principal.getUsername())));
        }
        redirectAttributes.addFlashAttribute("todo", saved);
        return "redirect:/todos/complete";
    }

    @GetMapping("/todos/complete")
    public String completeView(@ModelAttribute("todo") Todo todo, Model model) {
        if (todo.getId() == null) {
            return "redirect:/todos";
        }
        Todo latest = todoService.get(todo.getId());
        model.addAttribute("todo", latest);
        return "complete";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(
        @PathVariable Long id,
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "categoryId", required = false) Long categoryId,
        @RequestParam(name = "author", required = false) List<String> authors,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "size", required = false) Integer size,
        RedirectAttributes redirectAttributes
    ) {
        todoService.delete(id);
        if (keyword != null) {
            redirectAttributes.addAttribute("q", keyword);
        }
        if (sort != null) {
            redirectAttributes.addAttribute("sort", sort);
        }
        if (categoryId != null) {
            redirectAttributes.addAttribute("categoryId", categoryId);
        }
        if (authors != null) {
            redirectAttributes.addAttribute("author", authors);
        }
        if (status != null) {
            redirectAttributes.addAttribute("status", status);
        }
        if (page != null) {
            redirectAttributes.addAttribute("page", page);
        }
        if (size != null) {
            redirectAttributes.addAttribute("size", size);
        }
        return "redirect:/todos";
    }

    @PostMapping("/todos/bulk-delete")
    public String bulkDelete(
        @RequestParam(name = "ids", required = false) List<Long> ids,
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "categoryId", required = false) Long categoryId,
        @RequestParam(name = "author", required = false) List<String> authors,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "size", required = false) Integer size,
        RedirectAttributes redirectAttributes
    ) {
        todoService.deleteBatch(ids);
        if (keyword != null) {
            redirectAttributes.addAttribute("q", keyword);
        }
        if (sort != null) {
            redirectAttributes.addAttribute("sort", sort);
        }
        if (categoryId != null) {
            redirectAttributes.addAttribute("categoryId", categoryId);
        }
        if (authors != null) {
            redirectAttributes.addAttribute("author", authors);
        }
        if (status != null) {
            redirectAttributes.addAttribute("status", status);
        }
        if (page != null) {
            redirectAttributes.addAttribute("page", page);
        }
        if (size != null) {
            redirectAttributes.addAttribute("size", size);
        }
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/toggle")
    public String toggle(
        @PathVariable Long id,
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "categoryId", required = false) Long categoryId,
        @RequestParam(name = "author", required = false) List<String> authors,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "size", required = false) Integer size,
        RedirectAttributes redirectAttributes
    ) {
        todoService.toggleCompleted(id);
        if (keyword != null) {
            redirectAttributes.addAttribute("q", keyword);
        }
        if (sort != null) {
            redirectAttributes.addAttribute("sort", sort);
        }
        if (categoryId != null) {
            redirectAttributes.addAttribute("categoryId", categoryId);
        }
        if (authors != null) {
            redirectAttributes.addAttribute("author", authors);
        }
        if (status != null) {
            redirectAttributes.addAttribute("status", status);
        }
        if (page != null) {
            redirectAttributes.addAttribute("page", page);
        }
        if (size != null) {
            redirectAttributes.addAttribute("size", size);
        }
        return "redirect:/todos";
    }

    @GetMapping("/todos/export")
    public ResponseEntity<byte[]> exportCsv(
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "categoryId", required = false) Long categoryId,
        @RequestParam(name = "author", required = false) List<String> authors,
        @RequestParam(name = "status", required = false) String status,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Boolean completed = resolveCompleted(status);
        Long userId = userService.findUserId(principal.getUsername());
        List<Todo> todos = todoService.listAll(keyword, sort, categoryId, authors, completed, userId);
        String csv = buildCsv(todos);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        String fileName = "todo_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        return ResponseEntity.ok().headers(headers).body(bytes);
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

    private String buildCsv(List<Todo> todos) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("ID,タイトル,登録者,ステータス,作成日,期限日\n");
        for (Todo todo : todos) {
            sb.append(escapeCsv(String.valueOf(todo.getId()))).append(',');
            sb.append(escapeCsv(todo.getTitle())).append(',');
            sb.append(escapeCsv(todo.getAuthor())).append(',');
            sb.append(escapeCsv(todo.isCompleted() ? "完了" : "未完了")).append(',');
            sb.append(escapeCsv(todo.getCreatedAtLabel())).append(',');
            sb.append(escapeCsv(todo.getDeadlineLabel())).append('\n');
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuote ? "\"" + escaped + "\"" : escaped;
    }

    private TodoForm toForm(Todo todo) {
        TodoForm form = new TodoForm();
        form.setId(todo.getId());
        form.setAuthor(todo.getAuthor());
        form.setTitle(todo.getTitle());
        form.setDetail(todo.getDetail());
        form.setCategoryId(todo.getCategoryId());
        form.setDeadline(todo.getDeadline());
        return form;
    }

    private Todo toEntity(TodoForm form, Long userId) {
        Todo todo = new Todo();
        todo.setId(form.getId());
        todo.setUserId(userId);
        todo.setAuthor(form.getAuthor());
        todo.setTitle(form.getTitle());
        todo.setDetail(form.getDetail());
        todo.setCategoryId(form.getCategoryId());
        todo.setDeadline(form.getDeadline());
        return todo;
    }

    private String formatDeadline(LocalDate deadline) {
        if (deadline == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("Gy年MM月dd日", Locale.JAPAN);
        return formatter.format(JapaneseDate.from(deadline));
    }
}
