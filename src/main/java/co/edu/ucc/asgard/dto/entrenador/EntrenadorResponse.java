package co.edu.ucc.asgard.dto.entrenador;

import co.edu.ucc.asgard.modelo.Entrenador;
import co.edu.ucc.asgard.modelo.enums.Turno;
import java.time.Instant;

public record EntrenadorResponse(
        String id,
        String nombre,
        String documentoIdentidad,
        String correo,
        String telefono,
        Turno turno,
        int maximoSocios,
        long sociosAsignados,
        boolean tieneCupo,
        Instant fechaRegistro) {

    public static EntrenadorResponse desde(Entrenador entrenador, long sociosAsignados) {
        return new EntrenadorResponse(entrenador.getId(), entrenador.getNombre(), entrenador.getDocumentoIdentidad(),
                entrenador.getCorreo(), entrenador.getTelefono(), entrenador.getTurno(), entrenador.getMaximoSocios(),
                sociosAsignados, entrenador.tieneCupo((int) sociosAsignados), entrenador.getFechaRegistro());
    }
}
