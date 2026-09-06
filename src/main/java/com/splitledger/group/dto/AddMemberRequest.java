package com.splitledger.group.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberRequest {

    @Email
    @NotBlank(message = "Email is required")
    private String email;
}
