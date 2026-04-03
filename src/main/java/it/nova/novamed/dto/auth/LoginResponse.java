package it.nova.novamed.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import it.nova.novamed.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Risposta restituita dopo un login riuscito")
public class LoginResponse {

    @Schema(
            description = "ID dell'utente autenticato",
            example = "42"
    )
    private Long userId;

    @Schema(
            description = "Ruolo dell'utente autenticato",
            example = "PATIENT"
    )
    private Role role;
    private Long doctorId;

}

