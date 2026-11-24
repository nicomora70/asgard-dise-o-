package co.edu.ucc.asgard.dto.rutina;

import co.edu.ucc.asgard.modelo.DiaRutina;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Rutina;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record RutinaResponse(
        String id,
        String socioId,
        String entrenadorId,
        String nombre,
        LocalDate fechaInicio,
        int semanas,
        boolean activa,
        Instant fechaCreacion,
        List<DiaResponse> dias) {

    public static RutinaResponse desde(Rutina rutina, Map<String, String> nombres) {
        List<DiaResponse> dias = rutina.getDias().stream()
                .sorted(Comparator.comparing(DiaRutina::getDia))
                .map(d -> new DiaResponse(d.getDia(), d.getNombre(), d.getPrescripciones().stream()
                        .sorted(Comparator.comparingInt(Prescripcion::getOrden))
                        .map(p -> PrescripcionResponse.desde(p, nombres.get(p.getEjercicioId())))
                        .toList()))
                .toList();
        return new RutinaResponse(rutina.getId(), rutina.getSocioId(), rutina.getEntrenadorId(), rutina.getNombre(),
                rutina.getFechaInicio(), rutina.getSemanas(), rutina.isActiva(), rutina.getFechaCreacion(), dias);
    }

    public record DiaResponse(DayOfWeek dia, String nombre, List<PrescripcionResponse> ejercicios) {
    }

    public record PrescripcionResponse(
            int orden,
            String ejercicioId,
            String ejercicioNombre,
            int series,
            int repsMin,
            int repsMax,
            double rpeObjetivo,
            double cargaInicial,
            int descansoSegundos) {

        static PrescripcionResponse desde(Prescripcion p, String nombre) {
            return new PrescripcionResponse(p.getOrden(), p.getEjercicioId(), nombre, p.getSeries(), p.getRepsMin(),
                    p.getRepsMax(), p.getRpeObjetivo(), p.getCargaInicial(), p.getDescansoSegundos());
        }
    }
}
