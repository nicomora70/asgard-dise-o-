package co.edu.ucc.asgard.dto.serie;

import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.enums.RegistradoPor;
import java.time.Instant;

public record SerieResponse(
        String id,
        String socioId,
        String sesionId,
        String ejercicioId,
        String ejercicioNombre,
        int numero,
        double peso,
        int repeticiones,
        Double rpe,
        double e1RM,
        Double cargaSugerida,
        boolean superaSugerida,
        Instant fechaHora,
        RegistradoPor registradoPor,
        String notas,
        boolean deshecha,
        Instant fechaDeshecha) {

    public static SerieResponse desde(Serie serie, String ejercicioNombre) {
        return new SerieResponse(serie.getId(), serie.getSocioId(), serie.getSesionId(), serie.getEjercicioId(),
                ejercicioNombre, serie.getNumero(), serie.getPeso(), serie.getRepeticiones(), serie.getRpe(),
                serie.getE1RM(), serie.getCargaSugerida(), serie.isSuperaSugerida(), serie.getFechaHora(),
                serie.getRegistradoPor(), serie.getNotas(), serie.isDeshecha(), serie.getFechaDeshecha());
    }
}
