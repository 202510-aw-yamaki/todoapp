package com.example.todo.view;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;

public class DayView {
    private LocalDate date;
    private int dayOfMonth;
    private int todoCount;
    private List<String> topTitles;
    private int otherCount;
    private String rokuyo;
    private boolean holiday;
    private DayOfWeek dayOfWeek;

    public DayView(
        LocalDate date,
        int dayOfMonth,
        int todoCount,
        List<String> topTitles,
        int otherCount,
        String rokuyo,
        boolean holiday,
        DayOfWeek dayOfWeek
    ) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.todoCount = todoCount;
        this.topTitles = topTitles;
        this.otherCount = otherCount;
        this.rokuyo = rokuyo;
        this.holiday = holiday;
        this.dayOfWeek = dayOfWeek;
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

    public List<String> getTopTitles() {
        return topTitles;
    }

    public int getOtherCount() {
        return otherCount;
    }

    public String getRokuyo() {
        return rokuyo;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
}
