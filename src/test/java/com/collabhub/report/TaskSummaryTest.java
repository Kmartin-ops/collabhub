package com.collabhub.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaskSummary")
class TaskSummaryTest {

    @Test
    @DisplayName("formatted should include assignee and counts")
    void shouldFormatSummary() {
        TaskSummary summary = new TaskSummary("Alice", 10, 2, 4, 4);

        String formatted = summary.formatted();

        assertThat(formatted)
                .contains("Alice", "Total: 10", "Overdue:  2", "Done:  4", "In Progress:  4");
    }
}