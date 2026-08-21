package com.dppm.usmt.dto;

public record UserRequest(
        String firstName,
        String lastName,
        String email
) {
}
