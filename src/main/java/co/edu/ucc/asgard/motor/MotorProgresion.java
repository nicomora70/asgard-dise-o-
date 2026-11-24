package co.edu.ucc.asgard.motor;

import co.edu.ucc.asgard.config.ParametrosMotor;
import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Rutina;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.modelo.Sustituto;
import co.edu.ucc.asgard.modelo.enums.DecisionCarga;
import co.edu.ucc.asgard.modelo.enums.Objetivo;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import co.edu.ucc.asgard.motor.estructuras.GrafoEjercicios;
import co.edu.ucc.asgard.motor.estructuras.MonticuloMaximo;
import co.edu.ucc.asgard.motor.estructuras.PlanSemanal;
import co.edu.ucc.asgard.motor.estructuras.TablaHistorial;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

/**
 * Motor de progresion de cargas (RF08 - RF11). No depende de la base de datos ni de Spring Web (RNF-004): recibe el
 * socio, su rutina, el catalogo y el historial de series, y calcula el estado de cada ejercicio.
 */
@Component
public class MotorProgresion {

    private static final double RPE_POR_DEFECTO = 8.0;
    private static final int SERIES_POR_DEFECTO = 3;

    private final ParametrosMotor parametros;
    private final CalculadoraCarga calculadora;

    public MotorProgresion(ParametrosMotor parametros, CalculadoraCarga calculadora) {
        this.parametros = parametros;
        this.calculadora = calculadora;
    }

    /**
     * Evalua todos los ejercicios del socio: los de su rutina activa y los que tienen historial.
     *
     * @param historial series no deshechas del socio, en cualquier orden
     * @param zona      zona horaria para agrupar por dia las series que no tienen sesion
     */
    public ResultadoProgresion evaluar(Socio socio, Rutina rutina, List<Ejercicio> catalogo, List<Serie> historial,
            Instant ahora, ZoneId zona) {
        long inicio = System.nanoTime();
        PlanSemanal plan = new PlanSemanal(rutina);

        // 1. Catalogo en tabla hash y grafo de sustitutos.
        TablaHistorial<String, Ejercicio> ejercicios = new TablaHistorial<>();
        GrafoEjercicios grafo = construirGrafo(catalogo, ejercicios);

        // 2. Historial agrupado por ejercicio y por sesion (tabla hash: ejercicioId -> sesiones).
        TablaHistorial<String, List<SesionEjercicio>> historialPorEjercicio = agrupar(historial, zona);

        // 3. Ejercicios a evaluar: primero los de la rutina, luego los que solo tienen historial.
        List<String> orden = new ArrayList<>();
        for (String id : plan.ejercicios()) {
            if (ejercicios.contiene(id)) {
                orden.add(id);
            }
        }
        for (String id : historialPorEjercicio.claves()) {
            if (!orden.contains(id) && ejercicios.contiene(id)) {
                orden.add(id);
            }
        }

        // 4. Primera pasada: semaforo y doble progresion de cada ejercicio.
        TablaHistorial<String, EstadoEjercicio> estados = new TablaHistorial<>();
        for (String id : orden) {
            Ejercicio ejercicio = ejercicios.obtener(id).orElseThrow();
            Prescripcion prescripcion = plan.buscarEjercicio(id).orElseGet(() -> prescripcionPorDefecto(socio, id));
            List<SesionEjercicio> sesiones = historialPorEjercicio.obtener(id).orElse(List.of());
            estados.poner(id, evaluarEjercicio(ejercicio, prescripcion, sesiones, ahora));
        }

        // 5. Segunda pasada: estancamientos y lesiones buscan sustituto en el grafo (RF10).
        for (String id : orden) {
            EstadoEjercicio estado = estados.obtener(id).orElseThrow();
            Ejercicio ejercicio = ejercicios.obtener(id).orElseThrow();
            boolean lesionado = socio.getLesiones().stream().anyMatch(ejercicio::cargaGrupo);
            if (lesionado || estado.decision() == DecisionCarga.SUSTITUIR) {
                estados.poner(id, resolverSustitucion(estado, ejercicio, lesionado, socio, grafo, ejercicios, estados));
            }
        }

        long milisegundos = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
        return new ResultadoProgresion(estados, List.copyOf(orden), ahora, milisegundos);
    }

    /**
     * Busca un sustituto en el grafo: activo, que no cargue un grupo lesionado y que no este estancado.
     */
    public Optional<String> sugerirSustituto(String ejercicioId, Socio socio, GrafoEjercicios grafo,
            TablaHistorial<String, Ejercicio> ejercicios, TablaHistorial<String, EstadoEjercicio> estados) {
        Predicate<String> admitido = candidatoId -> ejercicios.obtener(candidatoId)
                .filter(Ejercicio::isActivo)
                .filter(c -> socio.getLesiones().stream().noneMatch(c::cargaGrupo))
                .filter(c -> estados.obtener(candidatoId).map(e -> !e.estancado()).orElse(true))
                .isPresent();
        return grafo.buscarSustituto(ejercicioId, admitido);
    }

    /**
     * Los k ejercicios que mas atencion necesitan (RF11): todos entran a un Max-Heap ordenado por puntaje y se
     * extrae la raiz k veces.
     */
    public List<EstadoEjercicio> priorizar(ResultadoProgresion resultado, int k) {
        Comparator<EstadoEjercicio> porPuntaje = Comparator.comparingDouble(EstadoEjercicio::puntaje)
                .thenComparing(EstadoEjercicio::nombre, Comparator.reverseOrder());
        MonticuloMaximo<EstadoEjercicio> monticulo = new MonticuloMaximo<>(porPuntaje);
        resultado.lista().forEach(monticulo::insertar);
        List<EstadoEjercicio> prioridades = new ArrayList<>();
        while (prioridades.size() < k && !monticulo.estaVacio()) {
            monticulo.extraerMaximo().ifPresent(prioridades::add);
        }
        return prioridades;
    }

    /** Grafo no dirigido del catalogo: cada sustituto es una arista con su similitud. */
    public GrafoEjercicios construirGrafo(List<Ejercicio> catalogo, TablaHistorial<String, Ejercicio> ejercicios) {
        GrafoEjercicios grafo = new GrafoEjercicios();
        for (Ejercicio ejercicio : catalogo) {
            ejercicios.poner(ejercicio.getId(), ejercicio);
            grafo.agregarVertice(ejercicio.getId());
        }
        for (Ejercicio ejercicio : catalogo) {
            for (Sustituto sustituto : ejercicio.getSustitutos()) {
                if (ejercicios.contiene(sustituto.getEjercicioId())) {
                    grafo.agregarArista(ejercicio.getId(), sustituto.getEjercicioId(), sustituto.getSimilitud());
                }
            }
        }
        return grafo;
    }

    private EstadoEjercicio evaluarEjercicio(Ejercicio ejercicio, Prescripcion prescripcion,
            List<SesionEjercicio> sesiones, Instant ahora) {
        List<Double> e1rms = sesiones.stream().map(SesionEjercicio::mejorE1RM).toList();
        Semaforo semaforo = calculadora.semaforo(e1rms);
        double diasSinEntrenar = sesiones.isEmpty() ? -1
                : Duration.between(sesiones.get(sesiones.size() - 1).fecha(), ahora).toMinutes() / 1440.0;
        double puntaje = (diasSinEntrenar < 0 ? parametros.diasSinHistorial() : Math.max(0, diasSinEntrenar))
                * calculadora.factorSemaforo(semaforo)
                * (ejercicio.getTipo() == TipoEjercicio.COMPUESTO ? parametros.factorCompuesto() : 1.0);
        puntaje = Math.round(puntaje * 100.0) / 100.0;

        if (sesiones.isEmpty()) {
            return new EstadoEjercicio(ejercicio.getId(), ejercicio.getNombre(), ejercicio.getGrupoMuscular(),
                    ejercicio.getTipo(), semaforo, DecisionCarga.MANTENER,
                    calculadora.redondear(prescripcion.getCargaInicial()), prescripcion.getSeries(),
                    prescripcion.getRepsMin(), prescripcion.getRpeObjetivo(), 0, 0, 0, 0, -1, puntaje, null, null,
                    "Sin historial: se empieza con la carga inicial de la rutina");
        }

        SesionEjercicio ultima = sesiones.get(sesiones.size() - 1);
        int fallosPrevios = 0;
        for (int i = sesiones.size() - 2; i >= 0; i--) {
            boolean fallo = sesiones.get(i).series().stream()
                    .anyMatch(s -> s.getRepeticiones() < prescripcion.getRepsMin());
            if (!fallo) {
                break;
            }
            fallosPrevios++;
        }

        DecisionCarga decision;
        String motivo;
        switch (semaforo) {
            case ROJO -> {
                decision = DecisionCarga.SUSTITUIR;
                motivo = "Retroceso: el 1RM estimado cayo mas de "
                        + porcentaje(parametros.umbralRetroceso()) + " frente al mejor registro";
            }
            case AMARILLO -> {
                decision = DecisionCarga.DELOAD;
                motivo = "Estancado: sin mejora en " + parametros.sesionesEstancamiento()
                        + " sesiones; semana de descarga al " + porcentaje(1 - parametros.porcentajeDeload());
            }
            default -> {
                decision = calculadora.decidir(ultima.series(), prescripcion, fallosPrevios);
                motivo = switch (decision) {
                    case SUBIR -> "Todas las series llegaron a " + prescripcion.getRepsMax()
                            + " repeticiones dentro del RPE objetivo";
                    case BAJAR -> "Series por debajo de " + prescripcion.getRepsMin() + " repeticiones en "
                            + (fallosPrevios + 1) + " sesiones seguidas";
                    default -> "Mantener la carga y buscar mas repeticiones dentro del rango";
                };
            }
        }

        double carga = calculadora.siguienteCarga(ultima.cargaTrabajo(), decision, calculadora.incremento(ejercicio));
        int minimoUltima = ultima.series().stream().mapToInt(Serie::getRepeticiones).min().orElse(0);
        int reps = switch (decision) {
            case MANTENER -> Math.max(prescripcion.getRepsMin(), Math.min(prescripcion.getRepsMax(), minimoUltima + 1));
            default -> prescripcion.getRepsMin();
        };
        double mejor = e1rms.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        return new EstadoEjercicio(ejercicio.getId(), ejercicio.getNombre(), ejercicio.getGrupoMuscular(),
                ejercicio.getTipo(), semaforo, decision, carga, prescripcion.getSeries(), reps,
                prescripcion.getRpeObjetivo(), ultima.cargaTrabajo(), ultima.mejorE1RM(), mejor, sesiones.size(),
                Math.round(diasSinEntrenar * 10.0) / 10.0, puntaje, null, null, motivo);
    }

    private EstadoEjercicio resolverSustitucion(EstadoEjercicio estado, Ejercicio ejercicio, boolean lesionado,
            Socio socio, GrafoEjercicios grafo, TablaHistorial<String, Ejercicio> ejercicios,
            TablaHistorial<String, EstadoEjercicio> estados) {
        Optional<Ejercicio> sustituto = sugerirSustituto(ejercicio.getId(), socio, grafo, ejercicios, estados)
                .flatMap(ejercicios::obtener);
        if (sustituto.isPresent()) {
            String motivo = lesionado
                    ? "El ejercicio carga un grupo muscular lesionado; se sugiere " + sustituto.get().getNombre()
                    : estado.motivo() + "; se sugiere cambiar a " + sustituto.get().getNombre();
            return estado.conDecision(DecisionCarga.SUSTITUIR, estado.cargaSugerida(), sustituto.get().getId(),
                    sustituto.get().getNombre(), motivo);
        }
        if (lesionado) {
            return estado.conDecision(DecisionCarga.SUSTITUIR, estado.cargaSugerida(), null, null,
                    "El ejercicio carga un grupo muscular lesionado y no hay un sustituto disponible en el catalogo");
        }
        double cargaDeload = calculadora.siguienteCarga(estado.ultimaCarga(), DecisionCarga.DELOAD, 0);
        return estado.conDecision(DecisionCarga.DELOAD, cargaDeload, null, null,
                estado.motivo() + "; sin sustituto disponible, se sugiere una semana de descarga");
    }

    private static Prescripcion prescripcionPorDefecto(Socio socio, String ejercicioId) {
        Objetivo objetivo = socio.getObjetivo() == null ? Objetivo.HIPERTROFIA : socio.getObjetivo();
        return new Prescripcion(0, ejercicioId, SERIES_POR_DEFECTO, objetivo.getRepsMin(), objetivo.getRepsMax(),
                RPE_POR_DEFECTO, 0, 90);
    }

    /** Agrupa las series por ejercicio y, dentro de cada ejercicio, por sesion (o por dia si no tienen sesion). */
    private TablaHistorial<String, List<SesionEjercicio>> agrupar(List<Serie> historial, ZoneId zona) {
        List<Serie> ordenadas = new ArrayList<>(historial);
        ordenadas.removeIf(Serie::isDeshecha);
        ordenadas.sort(Comparator.comparing(Serie::getFechaHora));

        TablaHistorial<String, TablaHistorial<String, List<Serie>>> porSesion = new TablaHistorial<>();
        TablaHistorial<String, List<String>> ordenSesiones = new TablaHistorial<>();
        for (Serie serie : ordenadas) {
            String clave = serie.getSesionId() != null ? serie.getSesionId()
                    : "dia-" + serie.getFechaHora().atZone(zona).toLocalDate();
            TablaHistorial<String, List<Serie>> sesiones = porSesion.obtener(serie.getEjercicioId()).orElse(null);
            if (sesiones == null) {
                sesiones = new TablaHistorial<>();
                porSesion.poner(serie.getEjercicioId(), sesiones);
                ordenSesiones.poner(serie.getEjercicioId(), new ArrayList<>());
            }
            List<Serie> series = sesiones.obtener(clave).orElse(null);
            if (series == null) {
                series = new ArrayList<>();
                sesiones.poner(clave, series);
                ordenSesiones.obtener(serie.getEjercicioId()).orElseThrow().add(clave);
            }
            series.add(serie);
        }

        TablaHistorial<String, List<SesionEjercicio>> resultado = new TablaHistorial<>();
        for (String ejercicioId : porSesion.claves()) {
            TablaHistorial<String, List<Serie>> sesiones = porSesion.obtener(ejercicioId).orElseThrow();
            List<SesionEjercicio> lista = new ArrayList<>();
            for (String clave : ordenSesiones.obtener(ejercicioId).orElseThrow()) {
                lista.add(SesionEjercicio.de(sesiones.obtener(clave).orElseThrow()));
            }
            lista.sort(Comparator.comparing(SesionEjercicio::fecha));
            resultado.poner(ejercicioId, lista);
        }
        return resultado;
    }

    private static String porcentaje(double fraccion) {
        return String.format(Locale.ROOT, "%.0f %%", fraccion * 100);
    }

    /** Series de un ejercicio en una misma sesion. */
    record SesionEjercicio(List<Serie> series, Instant fecha, double cargaTrabajo, double mejorE1RM) {

        static SesionEjercicio de(List<Serie> series) {
            Instant fecha = series.stream().map(Serie::getFechaHora).max(Comparator.naturalOrder()).orElseThrow();
            double carga = series.stream().mapToDouble(Serie::getPeso).max().orElse(0);
            double mejor = series.stream().mapToDouble(Serie::getE1RM).max().orElse(0);
            return new SesionEjercicio(List.copyOf(series), fecha, carga, mejor);
        }
    }
}
