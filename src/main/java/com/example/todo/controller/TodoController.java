package com.example.todo.controller;

import com.example.todo.entity.Category;
import com.example.todo.entity.Todo;
import com.example.todo.form.TodoForm;
import com.example.todo.service.CategoryService;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.chrono.JapaneseDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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

@Controller
public class TodoController {

    private final TodoService todoService;
    private final CategoryService categoryService;

    public TodoController(TodoService todoService, CategoryService categoryService) {
        this.todoService = todoService;
        this.categoryService = categoryService;
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
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @RequestParam(name = "size", required = false, defaultValue = "10") int size,
        Model model
    ) {
        String safeSort = todoService.normalizeSort(sort);
        int safeSize = todoService.resolveSize(size);
        Page<Todo> todoPage = todoService.list(keyword, safeSort, categoryId, page, safeSize);
        List<Todo> todos = todoPage.getContent();
        model.addAttribute("todos", todos);
        model.addAttribute("q", keyword);
        model.addAttribute("sort", safeSort);
        model.addAttribute("categoryId", categoryId);
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
        RedirectAttributes redirectAttributes
    ) {
        Todo saved;
        if ("edit".equals(mode)) {
            saved = todoService.update(todoForm.getId(), toEntity(todoForm));
        } else {
            saved = todoService.create(toEntity(todoForm));
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
        if (page != null) {
            redirectAttributes.addAttribute("page", page);
        }
        if (size != null) {
            redirectAttributes.addAttribute("size", size);
        }
        return "redirect:/todos";
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

    private Todo toEntity(TodoForm form) {
        Todo todo = new Todo();
        todo.setId(form.getId());
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
