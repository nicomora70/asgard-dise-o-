package co.edu.ucc.asgard.dto.ejercicio;

import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.enums.Equipamiento;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import java.util.List;
import java.util.Map;

public record EjercicioResponse(
        String id,
        String nombre,
        GrupoMuscular grupoMuscular,
        List<GrupoMuscular> gruposSecundarios,
        TipoEjercicio tipo,
        Equipamiento equipamiento,
        double incrementoKg,
        String creadoPor,
        boolean activo,
        List<SustitutoResponse> sustitutos) {

    /**
     * @param nombres nombre de cada ejercicio por id, para mostrar el nombre de los sustitutos
     */
    public static EjercicioResponse desde(Ejercicio ejercicio, Map<String, String> nombres) {
        List<SustitutoResponse> sustitutos = ejercicio.getSustitutos().stream()
                .map(s -> new SustitutoResponse(s.getEjercicioId(), nombres.get(s.getEjercicioId()), s.getSimilitud()))
                .toList();
        return new EjercicioResponse(ejercicio.getId(), ejercicio.getNombre(), ejercicio.getGrupoMuscular(),
                List.copyOf(ejercicio.getGruposSecundarios()), ejercicio.getTipo(), ejercicio.getEquipamiento(),
                ejercicio.getIncrementoKg(), ejercicio.getCreadoPor(), ejercicio.isActivo(), sustitutos);
    }

    public record SustitutoResponse(String ejercicioId, String nombre, double similitud) {
    }
}
