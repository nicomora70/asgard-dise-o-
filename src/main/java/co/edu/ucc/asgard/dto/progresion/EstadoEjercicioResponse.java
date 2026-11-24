package co.edu.ucc.asgard.dto.progresion;

import co.edu.ucc.asgard.modelo.enums.DecisionCarga;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import co.edu.ucc.asgard.motor.EstadoEjercicio;

/** Semaforo, decision y carga sugerida de un ejercicio (RF08 - RF11). */
public record EstadoEjercicioResponse(
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
        Double diasSinEntrenar,
        double puntaje,
        String sustitutoId,
        String sustitutoNombre,
        String motivo) {

    public static EstadoEjercicioResponse desde(EstadoEjercicio e) {
        return new EstadoEjercicioResponse(e.ejercicioId(), e.nombre(), e.grupoMuscular(), e.tipo(), e.semaforo(),
                e.decision(), e.cargaSugerida(), e.seriesObjetivo(), e.repsObjetivo(), e.rpeObjetivo(),
                e.ultimaCarga(), e.ultimoE1RM(), e.mejorE1RM(), e.sesionesRegistradas(),
                e.diasSinEntrenar() < 0 ? null : e.diasSinEntrenar(), e.puntaje(), e.sustitutoId(),
                e.sustitutoNombre(), e.motivo());
    }
}
