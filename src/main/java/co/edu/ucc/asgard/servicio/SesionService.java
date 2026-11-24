package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.config.ParametrosSesiones;
import co.edu.ucc.asgard.dto.sesion.SesionHoyResponse;
import co.edu.ucc.asgard.dto.sesion.SesionResponse;
import co.edu.ucc.asgard.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Rutina;
import co.edu.ucc.asgard.modelo.Sesion;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.motor.EstadoEjercicio;
import co.edu.ucc.asgard.motor.ResultadoProgresion;
import co.edu.ucc.asgard.motor.estructuras.ColaSesiones;
import co.edu.ucc.asgard.motor.estructuras.PlanSemanal;
import co.edu.ucc.asgard.repositorio.SerieRepository;
import co.edu.ucc.asgard.repositorio.SesionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sesiones de entrenamiento (RF04, US005). Las sesiones que el socio no realizo entran a una cola FIFO: la sesion
 * del dia es la pendiente mas antigua; si no hay pendientes, la que indica la rutina para hoy.
 */
@Service
public class SesionService {

    private static final Logger LOG = LoggerFactory.getLogger(SesionService.class);

    private final SesionRepository sesionRepository;
    private final SerieRepository serieRepository;
    private final SocioService socioService;
    private final RutinaService rutinaService;
    private final EjercicioService ejercicioService;
    private final ProgresionService progresionService;
    private final ParametrosSesiones parametros;
    private final Clock reloj;

    public SesionService(SesionRepository sesionRepository, SerieRepository serieRepository,
            SocioService socioService, RutinaService rutinaService, EjercicioService ejercicioService,
            ProgresionService progresionService, ParametrosSesiones parametros, Clock reloj) {
        this.sesionRepository = sesionRepository;
        this.serieRepository = serieRepository;
        this.socioService = socioService;
        this.rutinaService = rutinaService;
        this.ejercicioService = ejercicioService;
        this.progresionService = progresionService;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    /** Sesion que le toca hoy al socio, con la carga sugerida de cada ejercicio. */
    public SesionHoyResponse sesionDeHoy(String socioId) {
        Socio socio = socioService.obtener(socioId);
        Rutina rutina = rutinaService.obtenerActiva(socioId);
        LocalDate hoy = hoy();
        SesionActual actual = resolver(socio, rutina, hoy);
        if (actual.sesion() == null) {
            return new SesionHoyResponse(hoy, true, false, 0,
                    "Hoy es dia de descanso y no tienes sesiones pendientes", null, List.of());
        }

        Sesion sesion = actual.sesion();
        PlanSemanal plan = new PlanSemanal(rutina);
        ResultadoProgresion progresion = progresionService.evaluar(socio, sesion.getId());
        Map<String, String> nombres = ejercicioService.nombres();
        List<SesionHoyResponse.EjercicioDelDia> ejercicios = plan.delDia(sesion.getDia()).stream()
                .map(p -> ejercicioDelDia(p, progresion.estadoDe(p.getEjercicioId()), nombres, sesion.getId()))
                .toList();
        String mensaje = actual.recuperada()
                ? "Tienes pendiente la sesion del " + sesion.getFechaProgramada() + " (" + nombre(sesion)
                        + "): hazla antes de seguir con la rutina"
                : "Sesion de hoy: " + nombre(sesion);
        return new SesionHoyResponse(hoy, false, actual.recuperada(), actual.pendientes(), mensaje,
                SesionResponse.desde(sesion), ejercicios);
    }

    /**
     * Sesion a la que se asocian las series registradas sin sesion. Vacio si el socio no tiene rutina o si hoy es
     * dia de descanso (entrenamiento libre).
     */
    public Optional<Sesion> sesionActual(Socio socio) {
        return rutinaService.activa(socio.getId())
                .map(rutina -> resolver(socio, rutina, hoy()).sesion());
    }

    public Sesion obtenerDelSocio(String socioId, String sesionId) {
        return sesionRepository.findById(sesionId).filter(s -> s.getSocioId().equals(socioId))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la sesion " + sesionId
                        + " para el socio " + socioId));
    }

    /** Marca la sesion en curso al registrar su primera serie. */
    public void iniciar(Sesion sesion) {
        if (sesion.getFechaInicio() == null) {
            sesion.iniciar(Instant.now(reloj));
            sesionRepository.save(sesion);
        }
    }

    public SesionResponse completar(String socioId, String sesionId) {
        return cerrar(socioId, sesionId, true);
    }

    public SesionResponse omitir(String socioId, String sesionId) {
        return cerrar(socioId, sesionId, false);
    }

    public List<SesionResponse> historial(String socioId) {
        socioService.obtener(socioId);
        return sesionRepository.findBySocioIdOrderByFechaProgramadaDesc(socioId).stream()
                .map(SesionResponse::desde).toList();
    }

    /** Tarea del reloj del sistema: revisa a todos los socios con rutina activa (RF04). */
    public int encolarPendientesTodos() {
        LocalDate hoy = hoy();
        int revisados = 0;
        for (Rutina rutina : rutinaService.rutinasActivas()) {
            try {
                Socio socio = socioService.obtener(rutina.getSocioId());
                encolarPendientes(socio, rutina, hoy);
                revisados++;
            } catch (RuntimeException e) {
                LOG.warn("No se pudieron revisar las sesiones del socio {}", rutina.getSocioId(), e);
            }
        }
        return revisados;
    }

    /**
     * Crea las sesiones de los dias de entrenamiento ya pasados que no tienen sesion y encola las que siguen
     * abiertas. Es idempotente: se puede ejecutar varias veces sin duplicar.
     */
    ColaSesiones<String> encolarPendientes(Socio socio, Rutina rutina, LocalDate hoy) {
        PlanSemanal plan = new PlanSemanal(rutina);
        ColaSesiones<String> cola = ColaSesiones.desdeLista(socio.getColaPendientes());
        boolean cambio = false;

        LocalDate inicioRutina = rutina.getFechaInicio() != null ? rutina.getFechaInicio()
                : rutina.getFechaCreacion().atZone(zona()).toLocalDate();
        LocalDate desde = inicioRutina.isAfter(hoy.minusDays(parametros.diasRevision())) ? inicioRutina
                : hoy.minusDays(parametros.diasRevision());
        for (LocalDate dia = desde; dia.isBefore(hoy); dia = dia.plusDays(1)) {
            if (!plan.esDiaDeEntrenamiento(dia.getDayOfWeek())) {
                continue;
            }
            Optional<Sesion> existente = sesionRepository.findFirstBySocioIdAndFechaProgramada(socio.getId(), dia);
            if (existente.isEmpty()) {
                cola.encolar(crear(socio, rutina, plan, dia).getId());
                cambio = true;
            } else if (!existente.get().estaCerrada() && !cola.contiene(existente.get().getId())) {
                cola.encolar(existente.get().getId());
                cambio = true;
            }
        }

        // Saca del frente las sesiones que ya se cerraron (completadas u omitidas).
        while (cola.verFrente().isPresent()) {
            Optional<Sesion> frente = sesionRepository.findById(cola.verFrente().get());
            if (frente.isPresent() && !frente.get().estaCerrada()) {
                break;
            }
            cola.desencolar();
            cambio = true;
        }
        if (cambio) {
            socio.setColaPendientes(cola.aLista());
            socioService.guardar(socio);
        }
        return cola;
    }

    private SesionActual resolver(Socio socio, Rutina rutina, LocalDate hoy) {
        ColaSesiones<String> cola = encolarPendientes(socio, rutina, hoy);
        if (!cola.estaVacia()) {
            Sesion pendiente = sesionRepository.findById(cola.verFrente().orElseThrow()).orElseThrow();
            return new SesionActual(pendiente, true, cola.tamano());
        }
        PlanSemanal plan = new PlanSemanal(rutina);
        if (!plan.esDiaDeEntrenamiento(hoy.getDayOfWeek())) {
            return new SesionActual(null, false, 0);
        }
        Sesion deHoy = sesionRepository.findFirstBySocioIdAndFechaProgramada(socio.getId(), hoy)
                .orElseGet(() -> crear(socio, rutina, plan, hoy));
        if (deHoy.estaCerrada()) {
            return new SesionActual(null, false, 0);
        }
        return new SesionActual(deHoy, false, 0);
    }

    private SesionResponse cerrar(String socioId, String sesionId, boolean completada) {
        Socio socio = socioService.obtener(socioId);
        Sesion sesion = obtenerDelSocio(socioId, sesionId);
        if (sesion.estaCerrada()) {
            throw new ReglaNegocioException("La sesion del " + sesion.getFechaProgramada() + " ya esta "
                    + sesion.getEstado().name().toLowerCase());
        }
        ColaSesiones<String> cola = ColaSesiones.desdeLista(socio.getColaPendientes());
        if (cola.contiene(sesionId)) {
            String frente = cola.verFrente().orElseThrow();
            if (!frente.equals(sesionId)) {
                Sesion masAntigua = sesionRepository.findById(frente).orElseThrow();
                throw new ReglaNegocioException("Primero completa u omite la sesion pendiente mas antigua (del "
                        + masAntigua.getFechaProgramada() + ")");
            }
            cola.desencolar();
            socio.setColaPendientes(cola.aLista());
            socioService.guardar(socio);
        }
        if (completada) {
            sesion.completar(Instant.now(reloj));
        } else {
            sesion.omitir();
        }
        return SesionResponse.desde(sesionRepository.save(sesion));
    }

    private Sesion crear(Socio socio, Rutina rutina, PlanSemanal plan, LocalDate fecha) {
        Sesion sesion = new Sesion();
        sesion.setSocioId(socio.getId());
        sesion.setRutinaId(rutina.getId());
        sesion.setDia(fecha.getDayOfWeek());
        sesion.setNombreDia(plan.nombreDe(fecha.getDayOfWeek()));
        sesion.setFechaProgramada(fecha);
        return sesionRepository.save(sesion);
    }

    private SesionHoyResponse.EjercicioDelDia ejercicioDelDia(Prescripcion p, Optional<EstadoEjercicio> estado,
            Map<String, String> nombres, String sesionId) {
        long registradas = serieRepository.countBySesionIdAndEjercicioIdAndDeshechaFalse(sesionId, p.getEjercicioId());
        return new SesionHoyResponse.EjercicioDelDia(p.getOrden(), p.getEjercicioId(), nombres.get(p.getEjercicioId()),
                p.getSeries(), p.getRepsMin(), p.getRepsMax(), p.getRpeObjetivo(), p.getDescansoSegundos(),
                estado.map(EstadoEjercicio::cargaSugerida).orElse(p.getCargaInicial()),
                estado.map(EstadoEjercicio::repsObjetivo).orElse(p.getRepsMin()),
                estado.map(EstadoEjercicio::decision).orElse(null), estado.map(EstadoEjercicio::semaforo).orElse(null),
                estado.map(EstadoEjercicio::sustitutoId).orElse(null),
                estado.map(EstadoEjercicio::sustitutoNombre).orElse(null), registradas);
    }

    private static String nombre(Sesion sesion) {
        return sesion.getNombreDia() == null ? sesion.getDia().name() : sesion.getNombreDia();
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj.withZone(zona()));
    }

    private ZoneId zona() {
        return parametros.zona();
    }

    /** Sesion resuelta para hoy: la pendiente mas antigua o la del dia. */
    private record SesionActual(Sesion sesion, boolean recuperada, int pendientes) {
    }
}
