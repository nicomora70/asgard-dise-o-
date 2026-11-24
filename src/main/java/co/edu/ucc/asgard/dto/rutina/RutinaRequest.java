package co.edu.ucc.asgard.dto.rutina;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/** Rutina semanal que el entrenador le asigna al socio (RF03, US004). */
public record RutinaRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 80) String nombre,
        String entrenadorId,
        LocalDate fechaInicio,
        @Min(value = 1, message = "debe ser al menos 1") @Max(value = 52, message = "debe ser maximo 52") int semanas,
        @NotEmpty(message = "debe tener al menos un dia") @Size(max = 7) @Valid List<DiaRequest> dias) {

    /** Un dia de entrenamiento con sus ejercicios en orden. */
    public record DiaRequest(
            @NotNull(message = "es obligatorio") DayOfWeek dia,
            @Size(max = 40) String nombre,
            @NotEmpty(message = "debe tener al menos un ejercicio") @Valid List<PrescripcionRequest> ejercicios) {
    }

    /** Lo que se indica para un ejercicio: series, rango de repeticiones, RPE objetivo y carga inicial. */
    public record PrescripcionRequest(
            @NotBlank(message = "es obligatorio") String ejercicioId,
            @Min(value = 1, message = "debe ser al menos 1") @Max(value = 10, message = "debe ser maximo 10") int series,
            @Min(value = 1, message = "debe ser al menos 1") @Max(value = 50, message = "debe ser maximo 50") int repsMin,
            @Min(value = 1, message = "debe ser al menos 1") @Max(value = 50, message = "debe ser maximo 50") int repsMax,
            @DecimalMin(value = "5.0", message = "debe estar entre 5 y 10") @DecimalMax(value = "10.0", message = "debe estar entre 5 y 10") double rpeObjetivo,
            @PositiveOrZero(message = "no puede ser negativa") double cargaInicial,
            @PositiveOrZero(message = "no puede ser negativo") int descansoSegundos) {
    }
}
