package com.collabhub.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") String name, // optional — null
                                                                                                      // means don't
                                                                                                      // update

        @Size(max = 500, message = "Description cannot exceed 500 characters") String description, // optional

        @Pattern(regexp = "ACTIVE|COMPLETED|ARCHIVED", message = "Status must be ACTIVE, COMPLETED, or ARCHIVED") String status // optional
) {
}
