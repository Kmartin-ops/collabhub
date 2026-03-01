package com.collabhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(

        @NotBlank(message = "User email is required")
        @Email(message = "Must be a valid email address")
        String userEmail
) {}