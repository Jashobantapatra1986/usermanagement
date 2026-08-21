package com.dppm.usmt.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        long createdBy,
        long updatedBy
) {
}
