package co.edu.ucc.asgard.motor;

import co.edu.ucc.asgard.motor.estructuras.TablaHistorial;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Resultado de evaluar a un socio: el estado de cada ejercicio, guardado en una tabla hash por su id.
 *
 * @param estados         estado de cada ejercicio por id (consulta O(1))
 * @param orden           ids en el orden de la rutina (y luego los ejercicios fuera de la rutina)
 * @param tiempoCalculoMs tiempo que tardo el motor (RNF-002)
 */
public record ResultadoProgresion(
        TablaHistorial<String, EstadoEjercicio> estados,
        List<String> orden,
        Instant calculadoEn,
        long tiempoCalculoMs) {

    public Optional<EstadoEjercicio> estadoDe(String ejercicioId) {
        return estados.obtener(ejercicioId);
    }

    /** Estados en el orden de la rutina. */
    public List<EstadoEjercicio> lista() {
        return orden.stream().map(estados::obtener).flatMap(Optional::stream).toList();
    }
}
