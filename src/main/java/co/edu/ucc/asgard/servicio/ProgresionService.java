package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.config.ParametrosSesiones;
import co.edu.ucc.asgard.dto.progresion.EstadoEjercicioResponse;
import co.edu.ucc.asgard.dto.progresion.ProgresionResponse;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import co.edu.ucc.asgard.motor.EstadoEjercicio;
import co.edu.ucc.asgard.motor.MotorProgresion;
import co.edu.ucc.asgard.motor.ResultadoProgresion;
import co.edu.ucc.asgard.repositorio.SerieRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/** Conecta el motor de progresion con los datos del socio (RF08 - RF11). */
@Service
public class ProgresionService {

    private final MotorProgresion motor;
    private final SocioService socioService;
    private final RutinaService rutinaService;
    private final EjercicioService ejercicioService;
    private final SerieRepository serieRepository;
    private final ParametrosSesiones parametrosSesiones;
    private final Clock reloj;

    public ProgresionService(MotorProgresion motor, SocioService socioService, RutinaService rutinaService,
            EjercicioService ejercicioService, SerieRepository serieRepository,
            ParametrosSesiones parametrosSesiones, Clock reloj) {
        this.motor = motor;
        this.socioService = socioService;
        this.rutinaService = rutinaService;
        this.ejercicioService = ejercicioService;
        this.serieRepository = serieRepository;
        this.parametrosSesiones = parametrosSesiones;
        this.reloj = reloj;
    }

    /** Evalua todos los ejercicios del socio con su historial activo (sin series deshechas). */
    public ResultadoProgresion evaluar(Socio socio) {
        return evaluar(socio, null);
    }

    /**
     * Evalua sin las series de una sesion: la carga sugerida para la sesion en curso se calcula con las sesiones
     * anteriores, asi no cambia a mitad del entrenamiento.
     */
    public ResultadoProgresion evaluar(Socio socio, String sesionExcluida) {
        List<Serie> historial = serieRepository.findBySocioIdAndDeshechaFalseOrderByFechaHoraAsc(socio.getId());
        if (sesionExcluida != null) {
            historial = historial.stream().filter(s -> !sesionExcluida.equals(s.getSesionId())).toList();
        }
        return motor.evaluar(socio, rutinaService.activa(socio.getId()).orElse(null), ejercicioService.catalogo(),
                historial, Instant.now(reloj), parametrosSesiones.zona());
    }

    public ProgresionResponse progresion(String socioId) {
        ResultadoProgresion resultado = evaluar(socioService.obtener(socioId));
        List<EstadoEjercicio> estados = resultado.lista();
        ProgresionResponse.ResumenSemaforo resumen = new ProgresionResponse.ResumenSemaforo(
                contar(estados, Semaforo.VERDE), contar(estados, Semaforo.AMARILLO), contar(estados, Semaforo.ROJO));
        return new ProgresionResponse(socioId, resultado.calculadoEn(), resultado.tiempoCalculoMs(), resumen,
                estados.stream().map(EstadoEjercicioResponse::desde).toList());
    }

    public ProgresionResponse.PrioridadesResponse prioridades(String socioId, int k) {
        ResultadoProgresion resultado = evaluar(socioService.obtener(socioId));
        List<EstadoEjercicioResponse> prioridades = motor.priorizar(resultado, k).stream()
                .map(EstadoEjercicioResponse::desde).toList();
        return new ProgresionResponse.PrioridadesResponse(socioId, k, resultado.tiempoCalculoMs(), prioridades);
    }

    private static long contar(List<EstadoEjercicio> estados, Semaforo semaforo) {
        return estados.stream().filter(e -> e.semaforo() == semaforo).count();
    }
}
