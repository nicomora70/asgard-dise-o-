package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.dto.serie.SerieResponse;
import co.edu.ucc.asgard.excepcion.SolicitudInvalidaException;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.repositorio.SerieRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Historial de series del socio, sin las deshechas (RF12, US014). */
@Service
public class HistorialService {

    private final SerieRepository serieRepository;
    private final SocioService socioService;
    private final EjercicioService ejercicioService;
    private final Clock reloj;

    public HistorialService(SerieRepository serieRepository, SocioService socioService,
            EjercicioService ejercicioService, Clock reloj) {
        this.serieRepository = serieRepository;
        this.socioService = socioService;
        this.ejercicioService = ejercicioService;
        this.reloj = reloj;
    }

    /** Series del mas reciente al mas antiguo, filtrables por ejercicio y fechas. */
    public List<SerieResponse> historial(String socioId, String ejercicioId, Instant desde, Instant hasta) {
        Map<String, String> nombres = ejercicioService.nombres();
        return consultar(socioId, ejercicioId, desde, hasta).stream()
                .map(s -> SerieResponse.desde(s, nombres.get(s.getEjercicioId())))
                .toList();
    }

    public List<Serie> consultar(String socioId, String ejercicioId, Instant desde, Instant hasta) {
        socioService.obtener(socioId);
        if (ejercicioId != null && !ejercicioId.isBlank()) {
            ejercicioService.obtener(ejercicioId);
        }
        Instant inicio = desde == null ? Instant.EPOCH : desde;
        Instant fin = hasta == null ? Instant.now(reloj).plus(Duration.ofMinutes(5)) : hasta;
        if (inicio.isAfter(fin)) {
            throw new SolicitudInvalidaException("La fecha 'desde' debe ser anterior a 'hasta'");
        }
        return serieRepository.findBySocioIdAndDeshechaFalseAndFechaHoraBetweenOrderByFechaHoraDesc(socioId, inicio, fin)
                .stream()
                .filter(s -> ejercicioId == null || ejercicioId.isBlank() || s.getEjercicioId().equals(ejercicioId))
                .toList();
    }
}
