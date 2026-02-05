package com.example.todo.entity;

import java.time.LocalDateTime;

public class TodoHistory {

    private Long id;
    private Long todoId;
    private Long editorUserId;
    private LocalDateTime editedAt;
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTodoId() {
        return todoId;
    }

    public void setTodoId(Long todoId) {
        this.todoId = todoId;
    }

    public Long getEditorUserId() {
        return editorUserId;
    }

    public void setEditorUserId(Long editorUserId) {
        this.editorUserId = editorUserId;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
