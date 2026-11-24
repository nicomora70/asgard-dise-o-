package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.dto.ejercicio.EjercicioRequest;
import co.edu.ucc.asgard.dto.ejercicio.EjercicioResponse;
import co.edu.ucc.asgard.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.excepcion.SolicitudInvalidaException;
import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.Sustituto;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.motor.estructuras.GrafoEjercicios;
import co.edu.ucc.asgard.repositorio.EjercicioRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Catalogo de ejercicios y su grafo de sustitutos (RF02, US003). */
@Service
public class EjercicioService {

    private final EjercicioRepository ejercicioRepository;
    private final EntrenadorService entrenadorService;

    public EjercicioService(EjercicioRepository ejercicioRepository, EntrenadorService entrenadorService) {
        this.ejercicioRepository = ejercicioRepository;
        this.entrenadorService = entrenadorService;
    }

    /**
     * Crea un ejercicio. La sustitucion es bidireccional (el grafo es no dirigido): cada sustituto tambien guarda
     * al nuevo ejercicio en su lista.
     */
    public EjercicioResponse crear(EjercicioRequest solicitud) {
        String nombre = solicitud.nombre().trim();
        if (ejercicioRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe un ejercicio llamado " + nombre);
        }
        if (solicitud.creadoPor() != null && !solicitud.creadoPor().isBlank()) {
            entrenadorService.obtener(solicitud.creadoPor());
        }
        List<EjercicioRequest.SustitutoRequest> pedidos = solicitud.sustitutos() == null ? List.of()
                : solicitud.sustitutos();
        Set<String> vistos = new HashSet<>();
        List<Ejercicio> sustitutos = new ArrayList<>();
        for (EjercicioRequest.SustitutoRequest s : pedidos) {
            if (!vistos.add(s.ejercicioId())) {
                throw new SolicitudInvalidaException("El sustituto " + s.ejercicioId() + " esta repetido");
            }
            sustitutos.add(obtener(s.ejercicioId()));
        }

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setNombre(nombre);
        ejercicio.setGrupoMuscular(solicitud.grupoMuscular());
        ejercicio.setGruposSecundarios(solicitud.gruposSecundarios() == null ? List.of()
                : solicitud.gruposSecundarios().stream().filter(Objects::nonNull)
                        .filter(g -> g != solicitud.grupoMuscular()).distinct().toList());
        ejercicio.setTipo(solicitud.tipo());
        ejercicio.setEquipamiento(solicitud.equipamiento());
        ejercicio.setIncrementoKg(solicitud.incrementoKg() == null ? 0 : solicitud.incrementoKg());
        ejercicio.setCreadoPor(solicitud.creadoPor());
        ejercicio.setSustitutos(pedidos.stream().map(s -> new Sustituto(s.ejercicioId(), s.similitud())).toList());
        Ejercicio guardado = ejercicioRepository.save(ejercicio);

        for (int i = 0; i < sustitutos.size(); i++) {
            Ejercicio otro = sustitutos.get(i);
            List<Sustituto> lista = new ArrayList<>(otro.getSustitutos());
            lista.removeIf(s -> s.getEjercicioId().equals(guardado.getId()));
            lista.add(new Sustituto(guardado.getId(), pedidos.get(i).similitud()));
            otro.setSustitutos(lista);
            ejercicioRepository.save(otro);
        }
        return EjercicioResponse.desde(guardado, nombres());
    }

    public Ejercicio obtener(String id) {
        return ejercicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el ejercicio " + id));
    }

    public EjercicioResponse consultar(String id) {
        return EjercicioResponse.desde(obtener(id), nombres());
    }

    public List<EjercicioResponse> listar(GrupoMuscular grupo) {
        List<Ejercicio> ejercicios = grupo == null ? ejercicioRepository.findAllByOrderByNombreAsc()
                : ejercicioRepository.findByGrupoMuscularOrderByNombreAsc(grupo);
        Map<String, String> nombres = nombres();
        return ejercicios.stream().map(e -> EjercicioResponse.desde(e, nombres)).toList();
    }

    /** Sustitutos directos del ejercicio en el grafo, del mas parecido al menos parecido. */
    public List<EjercicioResponse.SustitutoResponse> sustitutos(String id) {
        obtener(id);
        List<Ejercicio> catalogo = catalogo();
        GrafoEjercicios grafo = new GrafoEjercicios();
        Map<String, String> nombres = new HashMap<>();
        for (Ejercicio e : catalogo) {
            nombres.put(e.getId(), e.getNombre());
            grafo.agregarVertice(e.getId());
        }
        for (Ejercicio e : catalogo) {
            e.getSustitutos().stream().filter(s -> nombres.containsKey(s.getEjercicioId()))
                    .forEach(s -> grafo.agregarArista(e.getId(), s.getEjercicioId(), s.getSimilitud()));
        }
        return grafo.vecinos(id).stream()
                .sorted(Comparator.comparingDouble(GrafoEjercicios.Arista::similitud).reversed())
                .map(a -> new EjercicioResponse.SustitutoResponse(a.destino(), nombres.get(a.destino()), a.similitud()))
                .toList();
    }

    public List<Ejercicio> catalogo() {
        return ejercicioRepository.findAll();
    }

    /** Nombre de cada ejercicio por id. */
    public Map<String, String> nombres() {
        Map<String, String> nombres = new HashMap<>();
        ejercicioRepository.findAll().forEach(e -> nombres.put(e.getId(), e.getNombre()));
        return nombres;
    }
}
