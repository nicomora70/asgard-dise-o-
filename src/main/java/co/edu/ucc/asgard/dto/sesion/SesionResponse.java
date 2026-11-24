package co.edu.ucc.asgard.dto.sesion;

import co.edu.ucc.asgard.modelo.Sesion;
import co.edu.ucc.asgard.modelo.enums.EstadoSesion;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;

public record SesionResponse(
        String id,
        String socioId,
        String rutinaId,
        DayOfWeek dia,
        String nombreDia,
        LocalDate fechaProgramada,
        EstadoSesion estado,
        Instant fechaInicio,
        Instant fechaFin) {

    public static SesionResponse desde(Sesion sesion) {
        return new SesionResponse(sesion.getId(), sesion.getSocioId(), sesion.getRutinaId(), sesion.getDia(),
                sesion.getNombreDia(), sesion.getFechaProgramada(), sesion.getEstado(), sesion.getFechaInicio(),
                sesion.getFechaFin());
    }
}
