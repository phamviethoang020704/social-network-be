package com.example.mangxahoi.DTO.Request;

import com.example.mangxahoi.Enums.GenderUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "required fields")
    @Size(min = 6,message = "username must be at least 6 characters long")
    private String fullName;

    @Email(message = "Username must be a valid email")
    @NotBlank(message = "required fields")
    @Size(min = 8,message = "username must be at least 8 characters long")
    private String username;

    @NotBlank(message = "required fields")
    @Size(min = 8,message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "required fields")
    private String confirmPassword;

    private GenderUser gender;
}
