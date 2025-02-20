package com.example.shiftplanet.utils;

import java.util.*;

public class WorkSchedule {
    private String week;
    private Map<String, Map<String, List<Map<String, String>>>> schedule;

    public WorkSchedule(String startDate) {
        this.week = startDate;
        this.schedule = new LinkedHashMap<>();

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] shifts = {"morning", "evening"};

        for (String day : days) {
            Map<String, List<Map<String, String>>> shiftMap = new LinkedHashMap<>();
            for (String shift : shifts) {
                shiftMap.put(shift, new ArrayList<>()); // Empty list for shifts
            }
            schedule.put(day, shiftMap);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("week", this.week);
        result.put("schedule", this.schedule);
        return result;
    }
}