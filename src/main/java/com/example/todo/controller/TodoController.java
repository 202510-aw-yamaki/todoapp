package com.example.todo.controller;

import com.example.todo.entity.Todo;
import com.example.todo.form.TodoForm;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import java.util.List;
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

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/todos";
    }

    @GetMapping("/todos")
    public String index(
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        Model model
    ) {
        List<Todo> todos = todoService.list(keyword, sort);
        model.addAttribute("todos", todos);
        model.addAttribute("q", keyword);
        model.addAttribute("sort", sort);
        return "index";
    }

    @GetMapping("/todos/new")
    public String createForm(Model model) {
        model.addAttribute("todoForm", new TodoForm());
        model.addAttribute("mode", "create");
        return "create";
    }

    @GetMapping("/todos/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Todo todo = todoService.get(id);
        model.addAttribute("todoForm", toForm(todo));
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
            return "create";
        }
        return "confirm";
    }

    @PostMapping("/todos/back")
    public String back(@ModelAttribute("todoForm") TodoForm todoForm, @RequestParam("mode") String mode, Model model) {
        model.addAttribute("mode", mode);
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
        model.addAttribute("todo", todo);
        return "complete";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable Long id) {
        todoService.delete(id);
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/toggle")
    public String toggle(
        @PathVariable Long id,
        @RequestParam(name = "q", required = false) String keyword,
        @RequestParam(name = "sort", required = false) String sort,
        RedirectAttributes redirectAttributes
    ) {
        todoService.toggleCompleted(id);
        if (keyword != null) {
            redirectAttributes.addAttribute("q", keyword);
        }
        if (sort != null) {
            redirectAttributes.addAttribute("sort", sort);
        }
        return "redirect:/todos";
    }

    private TodoForm toForm(Todo todo) {
        TodoForm form = new TodoForm();
        form.setId(todo.getId());
        form.setAuthor(todo.getAuthor());
        form.setTitle(todo.getTitle());
        form.setDetail(todo.getDetail());
        return form;
    }

    private Todo toEntity(TodoForm form) {
        Todo todo = new Todo();
        todo.setId(form.getId());
        todo.setAuthor(form.getAuthor());
        todo.setTitle(form.getTitle());
        todo.setDetail(form.getDetail());
        return todo;
    }
}
