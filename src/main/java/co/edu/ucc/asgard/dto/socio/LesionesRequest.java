package co.edu.ucc.asgard.dto.socio;

import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Lesiones actuales del socio. Una lista vacia indica que ya no tiene lesiones. */
public record LesionesRequest(@NotNull(message = "es obligatoria (puede ser vacia)") List<GrupoMuscular> lesiones) {
}
