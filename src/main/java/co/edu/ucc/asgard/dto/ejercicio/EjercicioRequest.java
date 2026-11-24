package co.edu.ucc.asgard.dto.ejercicio;

import co.edu.ucc.asgard.modelo.enums.Equipamiento;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Datos para crear un ejercicio del catalogo con sus sustitutos (RF02, US003). */
public record EjercicioRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 80) String nombre,
        @NotNull(message = "es obligatorio") GrupoMuscular grupoMuscular,
        List<GrupoMuscular> gruposSecundarios,
        @NotNull(message = "es obligatorio") TipoEjercicio tipo,
        @NotNull(message = "es obligatorio") Equipamiento equipamiento,
        @PositiveOrZero(message = "no puede ser negativo") Double incrementoKg,
        String creadoPor,
        @Valid List<SustitutoRequest> sustitutos) {

    /** Sustituto del ejercicio con su grado de similitud. */
    public record SustitutoRequest(
            @NotBlank(message = "es obligatorio") String ejercicioId,
            @DecimalMin(value = "0.0", message = "debe estar entre 0 y 1") @DecimalMax(value = "1.0", message = "debe estar entre 0 y 1") double similitud) {
    }
}
