package co.edu.ucc.asgard.dto.socio;

import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.Nivel;
import co.edu.ucc.asgard.modelo.enums.Objetivo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** Datos para registrar un socio (RF01, US001). */
public record SocioRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 100) String nombre,
        @NotBlank(message = "es obligatorio") @Size(max = 30) String documentoIdentidad,
        @Email(message = "no es un correo valido") String correo,
        @Size(max = 20) String telefono,
        @NotNull(message = "es obligatorio") Nivel nivel,
        @NotNull(message = "es obligatorio") Objetivo objetivo,
        LocalDate fechaIngreso,
        @Valid DatosFisicosDto datosFisicos,
        List<GrupoMuscular> lesiones,
        String entrenadorId) {
}
