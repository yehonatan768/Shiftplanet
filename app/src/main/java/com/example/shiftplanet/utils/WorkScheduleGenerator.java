package com.example.shiftplanet.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class WorkScheduleGenerator {
    public static String generateSchedule(String startDate) {
        try {
            WorkSchedule workSchedule = new WorkSchedule(startDate);

            // Convert to JSON format
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON

            return objectMapper.writeValueAsString(workSchedule.toMap()); // Return JSON string
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // Return empty JSON in case of error
        }
    }
}
