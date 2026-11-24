package co.edu.ucc.asgard.dto;

import java.time.Instant;
import java.util.List;

/** Formato uniforme de error que devuelve la API (RNF-005). */
public record ErrorResponse(
        Instant timestamp,
        int estado,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles) {
}
