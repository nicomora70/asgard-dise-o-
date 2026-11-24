package co.edu.ucc.asgard.dto.socio;

import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.Nivel;
import co.edu.ucc.asgard.modelo.enums.Objetivo;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record SocioResponse(
        String id,
        String nombre,
        String documentoIdentidad,
        String correo,
        String telefono,
        Nivel nivel,
        Objetivo objetivo,
        LocalDate fechaIngreso,
        DatosFisicosDto datosFisicos,
        List<GrupoMuscular> lesiones,
        String entrenadorId,
        int seriesParaDeshacer,
        int sesionesPendientes,
        Instant fechaRegistro) {

    public static SocioResponse desde(Socio socio, LocalDate hoy) {
        return new SocioResponse(socio.getId(), socio.getNombre(), socio.getDocumentoIdentidad(), socio.getCorreo(),
                socio.getTelefono(), socio.getNivel(), socio.getObjetivo(), socio.getFechaIngreso(),
                DatosFisicosDto.desde(socio.getDatosFisicos(), hoy), List.copyOf(socio.getLesiones()),
                socio.getEntrenadorId(), socio.getPilaDeshacer().size(), socio.getColaPendientes().size(),
                socio.getFechaRegistro());
    }
}
