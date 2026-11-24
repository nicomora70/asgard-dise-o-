package co.edu.ucc.asgard.dto.serie;

import co.edu.ucc.asgard.modelo.enums.RegistradoPor;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Registro de una serie en un solo paso (RF05, RNF-001): basta con ejercicioId, peso y repeticiones. Si no se envia
 * la sesion, la serie se asocia a la sesion de hoy.
 */
public record RegistrarSerieRequest(
        @NotBlank(message = "es obligatorio") String ejercicioId,
        @NotNull(message = "es obligatorio") @DecimalMin(value = "0.0", message = "no puede ser negativo") @DecimalMax(value = "1000.0", message = "debe ser maximo 1000 kg") Double peso,
        @NotNull(message = "es obligatorio") @Min(value = 1, message = "debe ser al menos 1") @Max(value = 100, message = "debe ser maximo 100") Integer repeticiones,
        @DecimalMin(value = "1.0", message = "debe estar entre 1 y 10") @DecimalMax(value = "10.0", message = "debe estar entre 1 y 10") Double rpe,
        String sesionId,
        RegistradoPor registradoPor,
        Instant fechaHora,
        @Size(max = 300) String notas) {
}
