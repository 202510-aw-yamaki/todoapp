package com.example.todo.view;

import java.time.LocalDate;

public class DayView {
    private LocalDate date;
    private int dayOfMonth;
    private int todoCount;

    public DayView(LocalDate date, int dayOfMonth, int todoCount) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.todoCount = todoCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getTodoCount() {
        return todoCount;
    }
}
