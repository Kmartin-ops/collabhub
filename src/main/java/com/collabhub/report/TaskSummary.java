package com.collabhub.report;

public record TaskSummary(String assigneeName, long totalTasks, long overdueCount, long doneCount,
        long inProgressCount) {
    // Derived method — records can have methods too
    public String formatted() {
        return String.format("%-15s | Total: %2d | Overdue: %2d | Done: %2d | In Progress: %2d", assigneeName,
                totalTasks, overdueCount, doneCount, inProgressCount);
    }
}
