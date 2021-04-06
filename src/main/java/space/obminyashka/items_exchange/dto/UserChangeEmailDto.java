package space.obminyashka.items_exchange.dto;

import space.obminyashka.items_exchange.annotation.FieldMatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldMatch(first = "newEmail", second = "newEmailConfirmation", message = "{invalid.confirm.email}")
public class UserChangeEmailDto {

    @NotEmpty(message = "{invalid.not-empty}")
    @Email(message = "{invalid.email}")
    private String newEmail;

    @NotEmpty(message = "{invalid.not-empty}")
    private String newEmailConfirmation;
}