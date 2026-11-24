package co.edu.ucc.asgard.motor;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.ucc.asgard.config.ParametrosMotor;
import co.edu.ucc.asgard.modelo.DiaRutina;
import co.edu.ucc.asgard.modelo.Ejercicio;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Rutina;
import co.edu.ucc.asgard.modelo.Serie;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.modelo.Sustituto;
import co.edu.ucc.asgard.modelo.enums.DecisionCarga;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.Objetivo;
import co.edu.ucc.asgard.modelo.enums.Semaforo;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MotorProgresionTest {

    private static final Instant AHORA = Instant.parse("2026-11-24T12:00:00Z");
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final ParametrosMotor parametros = ParametrosMotor.porDefecto();
    private final MotorProgresion motor = new MotorProgresion(parametros, new CalculadoraCarga(parametros));

    private Socio socio;
    private Rutina rutina;
    private List<Ejercicio> catalogo;
    private List<Serie> historial;

    @BeforeEach
    void preparar() {
        socio = new Socio();
        socio.setId("socio1");
        socio.setObjetivo(Objetivo.HIPERTROFIA);

        Ejercicio sentadilla = ejercicio("sentadilla", GrupoMuscular.CUADRICEPS, TipoEjercicio.COMPUESTO);
        Ejercicio prensa = ejercicio("prensa", GrupoMuscular.CUADRICEPS, TipoEjercicio.COMPUESTO);
        Ejercicio zancada = ejercicio("zancada", GrupoMuscular.GLUTEOS, TipoEjercicio.COMPUESTO);
        Ejercicio press = ejercicio("press", GrupoMuscular.PECHO, TipoEjercicio.COMPUESTO);
        sentadilla.setSustitutos(List.of(new Sustituto("prensa", 0.8), new Sustituto("zancada", 0.6)));
        catalogo = List.of(sentadilla, prensa, zancada, press);

        rutina = new Rutina();
        rutina.setDias(List.of(
                new DiaRutina(DayOfWeek.MONDAY, "Pierna", List.of(new Prescripcion(1, "sentadilla", 3, 8, 12, 8, 60, 180))),
                new DiaRutina(DayOfWeek.THURSDAY, "Torso", List.of(new Prescripcion(1, "press", 3, 8, 12, 8, 40, 120)))));
        historial = new ArrayList<>();
    }

    @Test
    void sinHistorialSugiereLaCargaInicialDeLaRutina() {
        EstadoEjercicio estado = evaluar().estadoDe("press").orElseThrow();

        assertThat(estado.decision()).isEqualTo(DecisionCarga.MANTENER);
        assertThat(estado.cargaSugerida()).isEqualTo(40);
        assertThat(estado.repsObjetivo()).isEqualTo(8);
        assertThat(estado.diasSinEntrenar()).isEqualTo(-1);
    }

    @Test
    void subeLaCargaCuandoSeCompletanTodasLasRepeticiones() {
        sesion("s1", "sentadilla", 60, 3, 12, 12, 12);

        EstadoEjercicio estado = evaluar().estadoDe("sentadilla").orElseThrow();

        assertThat(estado.semaforo()).isEqualTo(Semaforo.VERDE);
        assertThat(estado.decision()).isEqualTo(DecisionCarga.SUBIR);
        assertThat(estado.cargaSugerida()).isEqualTo(65); // tren inferior: +5 kg
        assertThat(estado.repsObjetivo()).isEqualTo(8);
    }

    @Test
    void estancadoSugiereDeload() {
        sesion("s1", "press", 50, 13, 10, 10, 10);
        sesion("s2", "press", 50, 10, 10, 10, 10);
        sesion("s3", "press", 50, 7, 10, 10, 9);
        sesion("s4", "press", 50, 4, 10, 10, 10);

        EstadoEjercicio estado = evaluar().estadoDe("press").orElseThrow();

        assertThat(estado.semaforo()).isEqualTo(Semaforo.AMARILLO);
        assertThat(estado.decision()).isEqualTo(DecisionCarga.DELOAD);
        assertThat(estado.cargaSugerida()).isEqualTo(45);
    }

    @Test
    void retrocesoSugiereElSustitutoMasParecido() {
        sesion("s1", "sentadilla", 80, 7, 10, 10, 10);
        sesion("s2", "sentadilla", 70, 3, 8, 8, 8);

        EstadoEjercicio estado = evaluar().estadoDe("sentadilla").orElseThrow();

        assertThat(estado.semaforo()).isEqualTo(Semaforo.ROJO);
        assertThat(estado.decision()).isEqualTo(DecisionCarga.SUSTITUIR);
        assertThat(estado.sustitutoId()).isEqualTo("prensa");
    }

    @Test
    void nuncaSugiereUnSustitutoQueCargueUnGrupoLesionado() {
        socio.setLesiones(List.of(GrupoMuscular.CUADRICEPS));

        EstadoEjercicio estado = evaluar().estadoDe("sentadilla").orElseThrow();

        assertThat(estado.decision()).isEqualTo(DecisionCarga.SUSTITUIR);
        assertThat(estado.sustitutoId()).isEqualTo("zancada");
    }

    @Test
    void lasSeriesDeshechasNoCuentan() {
        sesion("s1", "sentadilla", 60, 3, 12, 12, 12);
        historial.forEach(s -> s.setDeshecha(true));

        assertThat(evaluar().estadoDe("sentadilla").orElseThrow().sesionesRegistradas()).isZero();
    }

    @Test
    void priorizaLosEjerciciosQueMasAtencionNecesitan() {
        sesion("s1", "sentadilla", 60, 1, 10, 10, 10);
        sesion("s2", "press", 40, 10, 10, 10, 10);

        List<EstadoEjercicio> prioridades = motor.priorizar(evaluar(), 1);

        assertThat(prioridades).extracting(EstadoEjercicio::ejercicioId).containsExactly("press");
    }

    @Test
    void rendimiento() {
        List<Ejercicio> grande = new ArrayList<>();
        List<Prescripcion> prescripciones = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Ejercicio e = ejercicio("e" + i, GrupoMuscular.values()[i % GrupoMuscular.values().length],
                    i % 2 == 0 ? TipoEjercicio.COMPUESTO : TipoEjercicio.AISLAMIENTO);
            e.setSustitutos(List.of(new Sustituto("e" + ((i + 1) % 200), 0.7), new Sustituto("e" + ((i + 7) % 200), 0.5)));
            grande.add(e);
            if (i < 40) {
                prescripciones.add(new Prescripcion(i, "e" + i, 3, 8, 12, 8, 20, 90));
            }
        }
        catalogo = grande;
        rutina.setDias(List.of(new DiaRutina(DayOfWeek.MONDAY, "Todo", prescripciones)));
        Random azar = new Random(7);
        for (int i = 0; i < 5000; i++) {
            Serie serie = serie("sesion" + (i / 15), "e" + azar.nextInt(200), 20 + azar.nextInt(40),
                    6 + azar.nextInt(8), AHORA.minus(Duration.ofHours(5000 - i)));
            historial.add(serie);
        }

        evaluar(); // calentamiento de la JVM
        long inicio = System.nanoTime();
        ResultadoProgresion resultado = evaluar();
        motor.priorizar(resultado, 5);
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;

        assertThat(resultado.lista()).hasSize(200);
        assertThat(milisegundos).isLessThan(200);
        assertThat(resultado.tiempoCalculoMs()).isLessThan(200);
    }

    private ResultadoProgresion evaluar() {
        return motor.evaluar(socio, rutina, catalogo, historial, AHORA, ZONA);
    }

    /** Agrega una sesion del ejercicio hace "diasAtras" dias con las repeticiones dadas. */
    private void sesion(String sesionId, String ejercicioId, double peso, int diasAtras, int... repeticiones) {
        Instant fecha = AHORA.minus(Duration.ofDays(diasAtras));
        for (int reps : repeticiones) {
            historial.add(serie(sesionId, ejercicioId, peso, reps, fecha));
            fecha = fecha.plusSeconds(120);
        }
    }

    private Serie serie(String sesionId, String ejercicioId, double peso, int reps, Instant fecha) {
        Serie serie = new Serie();
        serie.setSocioId("socio1");
        serie.setSesionId(sesionId);
        serie.setEjercicioId(ejercicioId);
        serie.setPeso(peso);
        serie.setRepeticiones(reps);
        serie.setRpe(8.0);
        serie.setE1RM(new CalculadoraCarga(parametros).e1RM(peso, reps));
        serie.setFechaHora(fecha);
        return serie;
    }

    private static Ejercicio ejercicio(String id, GrupoMuscular grupo, TipoEjercicio tipo) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setId(id);
        ejercicio.setNombre(id);
        ejercicio.setGrupoMuscular(grupo);
        ejercicio.setTipo(tipo);
        return ejercicio;
    }
}
