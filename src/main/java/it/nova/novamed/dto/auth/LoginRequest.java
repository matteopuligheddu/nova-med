package it.nova.novamed.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Richiesta di login contenente email e password")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(
            description = "Email dell'utente",
            example = "mario.rossi@example.com",
            required = true
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "Password dell'utente",
            example = "password123",
            required = true
    )
    private String password;
}

