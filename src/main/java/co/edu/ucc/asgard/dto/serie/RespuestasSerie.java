package co.edu.ucc.asgard.dto.serie;

/** Respuestas de registrar, validar y deshacer series (RF05 - RF07). */
public final class RespuestasSerie {

    private RespuestasSerie() {
    }

    /**
     * Resultado de registrar una serie. La advertencia nunca bloquea el registro (RT-01).
     *
     * @param seriesParaDeshacer tamano de la pila de deshacer
     */
    public record RegistroSerieResponse(SerieResponse serie, String advertencia, int seriesParaDeshacer) {
    }

    /** Validacion previa de una serie: dice si habria advertencia, sin registrar nada (RF06). */
    public record ValidacionSerieResponse(
            String ejercicioId,
            String ejercicioNombre,
            double peso,
            int repeticiones,
            Double rpe,
            double e1RMEstimado,
            Double cargaSugerida,
            boolean permitido,
            boolean conAdvertencia,
            String advertencia) {
    }

    /**
     * Resultado de deshacer la ultima serie (RF07, EC-03).
     *
     * @param cargaSugeridaActual carga sugerida del ejercicio despues de deshacer
     */
    public record DeshacerResponse(SerieResponse serie, int seriesParaDeshacer, Double cargaSugeridaActual,
            String mensaje) {
    }
}
