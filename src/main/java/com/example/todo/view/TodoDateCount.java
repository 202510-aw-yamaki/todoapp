package com.example.todo.view;

import java.time.LocalDate;

public class TodoDateCount {
    private LocalDate date;
    private int count;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
