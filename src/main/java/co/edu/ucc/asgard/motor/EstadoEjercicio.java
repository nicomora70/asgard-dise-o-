package co.edu.ucc.asgard.motor;

import co.edu.ucc.asgard.modelo.enums.DecisionCarga;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;

/**
 * Resultado del motor para un ejercicio: semaforo, decision, carga sugerida y prioridad.
 *
 * @param diasSinEntrenar dias desde la ultima sesion del ejercicio; -1 si nunca se ha entrenado
 * @param sustitutoId     ejercicio sugerido cuando la decision es SUSTITUIR (puede ser nulo)
 * @param motivo          explicacion en espanol de la decision
 */
public record EstadoEjercicio(
        String ejercicioId,
        String nombre,
        GrupoMuscular grupoMuscular,
        TipoEjercicio tipo,
        Semaforo semaforo,
        DecisionCarga decision,
        double cargaSugerida,
        int seriesObjetivo,
        int repsObjetivo,
        double rpeObjetivo,
        double ultimaCarga,
        double ultimoE1RM,
        double mejorE1RM,
        int sesionesRegistradas,
        double diasSinEntrenar,
        double puntaje,
        String sustitutoId,
        String sustitutoNombre,
        String motivo) {

    /** Copia con otra decision, carga, sustituto y motivo (segunda pasada del motor). */
    public EstadoEjercicio conDecision(DecisionCarga nuevaDecision, double nuevaCarga, String nuevoSustitutoId,
            String nuevoSustitutoNombre, String nuevoMotivo) {
        return new EstadoEjercicio(ejercicioId, nombre, grupoMuscular, tipo, semaforo, nuevaDecision, nuevaCarga,
                seriesObjetivo, repsObjetivo, rpeObjetivo, ultimaCarga, ultimoE1RM, mejorE1RM, sesionesRegistradas,
                diasSinEntrenar, puntaje, nuevoSustitutoId, nuevoSustitutoNombre, nuevoMotivo);
    }

    public boolean estancado() {
        return semaforo != Semaforo.VERDE;
    }
}
