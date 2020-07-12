package com.hillel.items_exchange.dto;

import com.hillel.items_exchange.util.PatternHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {

    @NotEmpty(message = "{empty.username}")
    @Size(min = 2, max = 50, message = "{invalid.username.size}")
    @Pattern(regexp = PatternHandler.USERNAME_MIN_2_MAX_50, message = "{invalid.username}")
    private String username;

    @NotEmpty(message = "{empty.email}")
    @Size(max = 129, message = "{too.big.email}")
    @Email(regexp = PatternHandler.EMAIL, message = "{invalid.email}")
    private String email;

    @NotEmpty(message = "{empty.password}")
    @Size(min = 8, max = 30, message = "{invalid.password}")
    @Pattern(regexp = PatternHandler.PASSWORD_MIN_7_MAX_30, message = "{invalid.password}")
    private String password;

    @NotEmpty(message = "{empty.confirm.password}")
    private String confirmPassword;
}
