package oop.tegevusteplaneerija.common;

import java.time.ZonedDateTime;

public class CalendarEvent {
    private String title;
    private String description;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    public CalendarEvent(String title, String description, ZonedDateTime startTime, ZonedDateTime endTime) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}

