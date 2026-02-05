package com.example.todo.controller;

import com.example.todo.entity.Todo;
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
        model.addAttribute("todo", new Todo());
        model.addAttribute("mode", "create");
        return "create";
    }

    @GetMapping("/todos/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Todo todo = todoService.get(id);
        model.addAttribute("todo", todo);
        model.addAttribute("mode", "edit");
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirm(
        @Valid @ModelAttribute("todo") Todo todo,
        BindingResult bindingResult,
        Model model
    ) {
        String mode = todo.getId() == null ? "create" : "edit";
        model.addAttribute("mode", mode);
        if (bindingResult.hasErrors()) {
            return "create";
        }
        return "confirm";
    }

    @PostMapping("/todos/back")
    public String back(@ModelAttribute("todo") Todo todo, @RequestParam("mode") String mode, Model model) {
        model.addAttribute("mode", mode);
        return "create";
    }

    @PostMapping("/todos/complete")
    public String complete(
        @ModelAttribute("todo") Todo todo,
        @RequestParam("mode") String mode,
        RedirectAttributes redirectAttributes
    ) {
        Todo saved;
        if ("edit".equals(mode)) {
            saved = todoService.update(todo.getId(), todo);
        } else {
            saved = todoService.create(todo);
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
}
