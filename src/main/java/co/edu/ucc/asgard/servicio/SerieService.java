package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.config.ParametrosMotor;
import co.edu.ucc.asgard.dto.serie.RegistrarSerieRequest;
import co.edu.ucc.asgard.dto.serie.RespuestasSerie.DeshacerResponse;
import co.edu.ucc.asgard.dto.serie.RespuestasSerie.RegistroSerieResponse;
import co.edu.ucc.asgard.dto.serie.RespuestasSerie.ValidacionSerieResponse;
import co.edu.ucc.asgard.dto.serie.SerieResponse;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.excepcion.SolicitudInvalidaException;
import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.Sesion;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.modelo.enums.RegistradoPor;
import co.edu.ucc.asgard.motor.CalculadoraCarga;
import co.edu.ucc.asgard.motor.EstadoEjercicio;
import co.edu.ucc.asgard.motor.estructuras.PilaDeshacer;
import co.edu.ucc.asgard.repositorio.SerieRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Registro, validacion y deshacer de series (RF05 - RF07). */
@Service
public class SerieService {

    /** Tolerancia para fechas levemente adelantadas por diferencias de reloj del dispositivo. */
    private static final Duration TOLERANCIA_FUTURO = Duration.ofMinutes(5);

    private final SerieRepository serieRepository;
    private final SocioService socioService;
    private final EjercicioService ejercicioService;
    private final SesionService sesionService;
    private final ProgresionService progresionService;
    private final CalculadoraCarga calculadora;
    private final ParametrosMotor parametros;
    private final Clock reloj;

    public SerieService(SerieRepository serieRepository, SocioService socioService, EjercicioService ejercicioService,
            SesionService sesionService, ProgresionService progresionService, CalculadoraCarga calculadora,
            ParametrosMotor parametros, Clock reloj) {
        this.serieRepository = serieRepository;
        this.socioService = socioService;
        this.ejercicioService = ejercicioService;
        this.sesionService = sesionService;
        this.progresionService = progresionService;
        this.calculadora = calculadora;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    /** Dice si la serie generaria advertencia, sin registrarla. Nunca bloquea (RF06). */
    public ValidacionSerieResponse validar(String socioId, String ejercicioId, double peso, int repeticiones,
            Double rpe) {
        validarValores(peso, repeticiones, rpe);
        Socio socio = socioService.obtener(socioId);
        Ejercicio ejercicio = ejercicioService.obtener(ejercicioId);
        String sesionId = sesionService.sesionActual(socio).map(Sesion::getId).orElse(null);
        Double sugerida = cargaSugerida(socio, ejercicioId, sesionId).orElse(null);
        String advertencia = advertencia(peso, rpe, sugerida);
        return new ValidacionSerieResponse(ejercicioId, ejercicio.getNombre(), peso, repeticiones, rpe,
                calculadora.e1RM(peso, repeticiones), sugerida, true, advertencia != null, advertencia);
    }

    /** Registra una serie en un solo paso y la apila para poder deshacerla (RF05, RF07). */
    public RegistroSerieResponse registrar(String socioId, RegistrarSerieRequest solicitud) {
        Socio socio = socioService.obtener(socioId);
        Ejercicio ejercicio = ejercicioService.obtener(solicitud.ejercicioId());
        Instant ahora = Instant.now(reloj);
        Instant fechaHora = solicitud.fechaHora() == null ? ahora : solicitud.fechaHora();
        if (fechaHora.isAfter(ahora.plus(TOLERANCIA_FUTURO))) {
            throw new SolicitudInvalidaException("La fecha de la serie no puede estar en el futuro");
        }

        Optional<Sesion> sesion;
        if (solicitud.sesionId() != null && !solicitud.sesionId().isBlank()) {
            Sesion pedida = sesionService.obtenerDelSocio(socioId, solicitud.sesionId());
            if (pedida.estaCerrada()) {
                throw new ReglaNegocioException("La sesion del " + pedida.getFechaProgramada()
                        + " ya esta cerrada; no se le pueden agregar series");
            }
            sesion = Optional.of(pedida);
        } else {
            sesion = sesionService.sesionActual(socio);
        }

        Double sugerida = cargaSugerida(socio, ejercicio.getId(), sesion.map(Sesion::getId).orElse(null)).orElse(null);
        String advertencia = advertencia(solicitud.peso(), solicitud.rpe(), sugerida);

        Serie serie = new Serie();
        serie.setSocioId(socio.getId());
        serie.setSesionId(sesion.map(Sesion::getId).orElse(null));
        serie.setEjercicioId(ejercicio.getId());
        serie.setNumero(sesion.map(s -> (int) serieRepository.countBySesionIdAndEjercicioIdAndDeshechaFalse(s.getId(),
                ejercicio.getId()) + 1).orElse(1));
        serie.setPeso(solicitud.peso());
        serie.setRepeticiones(solicitud.repeticiones());
        serie.setRpe(solicitud.rpe());
        serie.setE1RM(calculadora.e1RM(solicitud.peso(), solicitud.repeticiones()));
        serie.setCargaSugerida(sugerida);
        serie.setSuperaSugerida(superaSugerida(solicitud.peso(), sugerida));
        serie.setFechaHora(fechaHora);
        serie.setRegistradoPor(solicitud.registradoPor() == null ? RegistradoPor.SOCIO : solicitud.registradoPor());
        serie.setNotas(solicitud.notas());
        Serie guardada = serieRepository.save(serie);

        PilaDeshacer pila = PilaDeshacer.desdeLista(socio.getPilaDeshacer(), parametros.capacidadDeshacer());
        pila.apilar(guardada.getId());
        socio.setPilaDeshacer(pila.aLista());
        socioService.guardar(socio);
        sesion.ifPresent(sesionService::iniciar);

        return new RegistroSerieResponse(SerieResponse.desde(guardada, ejercicio.getNombre()), advertencia,
                pila.tamano());
    }

    /** Deshace la ultima serie: la desapila y la marca como deshecha, sin borrarla (RF07, RT-02). */
    public DeshacerResponse deshacerUltima(String socioId) {
        Socio socio = socioService.obtener(socioId);
        PilaDeshacer pila = PilaDeshacer.desdeLista(socio.getPilaDeshacer(), parametros.capacidadDeshacer());
        Serie aDeshacer = null;
        while (aDeshacer == null) {
            Optional<String> cima = pila.desapilar();
            if (cima.isEmpty()) {
                socio.setPilaDeshacer(pila.aLista());
                socioService.guardar(socio);
                throw new ReglaNegocioException("No hay series para deshacer");
            }
            aDeshacer = serieRepository.findById(cima.get()).filter(s -> !s.isDeshecha()).orElse(null);
        }
        aDeshacer.deshacer(Instant.now(reloj));
        Serie guardada = serieRepository.save(aDeshacer);
        socio.setPilaDeshacer(pila.aLista());
        socioService.guardar(socio);

        Ejercicio ejercicio = ejercicioService.obtener(guardada.getEjercicioId());
        Double sugeridaAhora = cargaSugerida(socio, guardada.getEjercicioId(), guardada.getSesionId()).orElse(null);
        return new DeshacerResponse(SerieResponse.desde(guardada, ejercicio.getNombre()), pila.tamano(),
                sugeridaAhora, "Se deshizo la serie " + guardada.getNumero() + " de " + ejercicio.getNombre()
                        + "; queda en el historial marcada como deshecha");
    }

    private Optional<Double> cargaSugerida(Socio socio, String ejercicioId, String sesionEnCurso) {
        return progresionService.evaluar(socio, sesionEnCurso).estadoDe(ejercicioId)
                .map(EstadoEjercicio::cargaSugerida)
                .filter(c -> c > 0);
    }

    private boolean superaSugerida(double peso, Double sugerida) {
        return sugerida != null && peso > sugerida * (1 + parametros.umbralSobrecarga());
    }

    /** Advertencia no bloqueante en espanol (RF06); nulo si no hay nada que advertir. */
    private String advertencia(double peso, Double rpe, Double sugerida) {
        List<String> avisos = new ArrayList<>();
        if (superaSugerida(peso, sugerida)) {
            double exceso = (peso / sugerida - 1) * 100;
            avisos.add(String.format(Locale.ROOT, "La carga (%.1f kg) supera en %.0f %% la sugerida (%.1f kg): "
                    + "sube de forma gradual para evitar lesiones.", peso, exceso, sugerida));
        }
        if (rpe != null && rpe >= parametros.rpeAlerta()) {
            avisos.add(String.format(Locale.ROOT, "RPE de %.1f: esfuerzo casi maximo, cuida la tecnica y el "
                    + "descanso.", rpe));
        }
        return avisos.isEmpty() ? null : String.join(" ", avisos);
    }

    private static void validarValores(double peso, int repeticiones, Double rpe) {
        if (peso < 0) {
            throw new SolicitudInvalidaException("El peso no puede ser negativo");
        }
        if (repeticiones < 1 || repeticiones > 100) {
            throw new SolicitudInvalidaException("Las repeticiones deben estar entre 1 y 100");
        }
        if (rpe != null && (rpe < 1 || rpe > 10)) {
            throw new SolicitudInvalidaException("El RPE debe estar entre 1 y 10");
        }
    }
}
