package co.edu.ucc.asgard.dto.entrenador;

import co.edu.ucc.asgard.modelo.enums.Turno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para registrar un entrenador (RF01, US002). */
public record EntrenadorRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 100) String nombre,
        @NotBlank(message = "es obligatorio") @Size(max = 30) String documentoIdentidad,
        @Email(message = "no es un correo valido") String correo,
        @Size(max = 20) String telefono,
        @NotNull(message = "es obligatorio") Turno turno,
        @Min(value = 1, message = "debe ser al menos 1") @Max(value = 100, message = "debe ser maximo 100") int maximoSocios) {
}
