package co.edu.ucc.asgard.repositorio;

import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EjercicioRepository extends MongoRepository<Ejercicio, String> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Ejercicio> findByGrupoMuscularOrderByNombreAsc(GrupoMuscular grupoMuscular);

    List<Ejercicio> findAllByOrderByNombreAsc();
}
