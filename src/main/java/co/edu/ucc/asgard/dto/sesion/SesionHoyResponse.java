package co.edu.ucc.asgard.dto.sesion;

import co.edu.ucc.asgard.modelo.enums.DecisionCarga;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import java.time.LocalDate;
import java.util.List;

/**
 * Sesion que le toca al socio hoy (RF04, US005).
 *
 * @param descanso           true si hoy no hay que entrenar y no hay sesiones pendientes
 * @param recuperada         true si la sesion es una pendiente de un dia anterior (frente de la cola FIFO)
 * @param sesionesPendientes sesiones que siguen en la cola
 */
public record SesionHoyResponse(
        LocalDate fecha,
        boolean descanso,
        boolean recuperada,
        int sesionesPendientes,
        String mensaje,
        SesionResponse sesion,
        List<EjercicioDelDia> ejercicios) {

    /** Un ejercicio de la sesion con su carga sugerida por el motor. */
    public record EjercicioDelDia(
            int orden,
            String ejercicioId,
            String nombre,
            int series,
            int repsMin,
            int repsMax,
            double rpeObjetivo,
            int descansoSegundos,
            double cargaSugerida,
            int repsObjetivo,
            DecisionCarga decision,
            Semaforo semaforo,
            String sustitutoId,
            String sustitutoNombre,
            long seriesRegistradas) {
    }
}
