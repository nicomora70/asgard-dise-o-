package co.edu.ucc.asgard.dto.progresion;

import java.time.Instant;
import java.util.List;

/**
 * Progresion del socio: estado de cada ejercicio y resumen del semaforo (RF08 - RF10).
 *
 * @param tiempoCalculoMs tiempo que tardo el motor (RNF-002)
 */
public record ProgresionResponse(
        String socioId,
        Instant calculadoEn,
        long tiempoCalculoMs,
        ResumenSemaforo resumen,
        List<EstadoEjercicioResponse> ejercicios) {

    public record ResumenSemaforo(long verde, long amarillo, long rojo) {
    }

    /** Los k ejercicios que mas atencion necesitan, extraidos del Max-Heap (RF11). */
    public record PrioridadesResponse(String socioId, int k, long tiempoCalculoMs,
            List<EstadoEjercicioResponse> ejercicios) {
    }
}
