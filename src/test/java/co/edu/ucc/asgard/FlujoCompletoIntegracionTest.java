package co.edu.ucc.asgard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Recorre el flujo completo de la API contra un MongoDB en memoria: entrenador, socio, catalogo, rutina, sesion del
 * dia, series con advertencia, deshacer, cola de sesiones pendientes, progresion, prioridades, lesiones, historial,
 * reporte PDF y eliminacion en cascada.
 */
@SpringBootTest(properties = {"asgard.sesiones.planificador-habilitado=false", 
        // El servidor en memoria no implementa el comando "hello" del indicador de salud de MongoDB
        "management.health.mongodb.enabled=false"})
@AutoConfigureMockMvc
class FlujoCompletoIntegracionTest {

    private static final MongoServer MONGO = new MongoServer(new MemoryBackend());
    private static final InetSocketAddress DIRECCION = MONGO.bind();
    /** Lunes 23 de noviembre de 2026, 3:00 p. m. en Bogota. */
    private static final RelojAjustable RELOJ = new RelojAjustable(Instant.parse("2026-11-23T20:00:00Z"));

    @DynamicPropertySource
    static void mongo(DynamicPropertyRegistry registro) {
        String uri = "mongodb://" + DIRECCION.getHostString() + ":" + DIRECCION.getPort() + "/asgard";
        registro.add("spring.mongodb.uri", () -> uri);
        registro.add("spring.data.mongodb.uri", () -> uri);
    }

    @TestBean(name = "reloj")
    private Clock reloj;

    static Clock reloj() {
        return RELOJ;
    }

    @AfterAll
    static void apagar() {
        MONGO.shutdown();
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void flujoCompletoDelSocio() throws Exception {
        // ---------- Entrenador y socio (RF01) ----------
        String entrenadorId = id(enviar(post("/api/entrenadores"), """
                {"nombre":"Carlos Ruiz","documentoIdentidad":"E-100","turno":"TARDE","maximoSocios":20}""")
                .andExpect(status().isCreated()));

        String socio = """
                {"nombre":"Laura Gomez","documentoIdentidad":"S-200","correo":"laura@example.com",
                 "nivel":"INTERMEDIO","objetivo":"HIPERTROFIA",
                 "datosFisicos":{"pesoCorporal":62,"estatura":165,"fechaNacimiento":"1998-04-10","sexo":"FEMENINO"},
                 "entrenadorId":"%s"}""".formatted(entrenadorId);
        String socioId = id(enviar(post("/api/socios"), socio)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datosFisicos.imc").value(22.8)));
        enviar(post("/api/socios"), socio).andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un socio con el documento S-200"));

        // ---------- Catalogo y grafo de sustitutos (RF02) ----------
        String sentadilla = id(enviar(post("/api/ejercicios"), ejercicio("Sentadilla", "CUADRICEPS", "BARRA", ""))
                .andExpect(status().isCreated()));
        String prensa = id(enviar(post("/api/ejercicios"), ejercicio("Prensa", "CUADRICEPS", "MAQUINA",
                "{\"ejercicioId\":\"" + sentadilla + "\",\"similitud\":0.8}")));
        String zancada = id(enviar(post("/api/ejercicios"), ejercicio("Zancada", "GLUTEOS", "MANCUERNA",
                "{\"ejercicioId\":\"" + sentadilla + "\",\"similitud\":0.6}")));
        String press = id(enviar(post("/api/ejercicios"), ejercicio("Press de banca", "PECHO", "BARRA", "")));

        mvc.perform(get("/api/ejercicios/" + sentadilla + "/sustitutos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ejercicioId").value(prensa))
                .andExpect(jsonPath("$[1].ejercicioId").value(zancada));

        // ---------- Rutina semanal (RF03) ----------
        enviar(post("/api/socios/" + socioId + "/rutinas"), """
                {"nombre":"Fuerza base","fechaInicio":"2026-11-23","semanas":8,"dias":[
                  {"dia":"MONDAY","nombre":"Pierna","ejercicios":[
                    {"ejercicioId":"%s","series":3,"repsMin":8,"repsMax":12,"rpeObjetivo":8,"cargaInicial":60,"descansoSegundos":150}]},
                  {"dia":"THURSDAY","nombre":"Torso","ejercicios":[
                    {"ejercicioId":"%s","series":3,"repsMin":8,"repsMax":12,"rpeObjetivo":8,"cargaInicial":40,"descansoSegundos":120}]}]}
                """.formatted(sentadilla, press))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activa").value(true));

        // ---------- Sesion del lunes (RF04) ----------
        String sesionLunes = JsonPath.read(mvc.perform(get("/api/socios/" + socioId + "/sesiones/hoy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descanso").value(false))
                .andExpect(jsonPath("$.ejercicios[0].cargaSugerida").value(60.0))
                .andReturn().getResponse().getContentAsString(), "$.sesion.id");

        // ---------- Series (RF05), advertencia (RF06) y deshacer (RF07) ----------
        for (int i = 0; i < 3; i++) {
            enviar(post("/api/socios/" + socioId + "/series"),
                    "{\"ejercicioId\":\"" + sentadilla + "\",\"peso\":60,\"repeticiones\":12,\"rpe\":8}")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.serie.numero").value(i + 1))
                    .andExpect(jsonPath("$.serie.sesionId").value(sesionLunes));
        }
        mvc.perform(get("/api/socios/" + socioId + "/series/validar").param("ejercicioId", sentadilla)
                        .param("peso", "80").param("repeticiones", "5"))
                .andExpect(jsonPath("$.conAdvertencia").value(true))
                .andExpect(jsonPath("$.permitido").value(true));
        enviar(post("/api/socios/" + socioId + "/series"),
                "{\"ejercicioId\":\"" + sentadilla + "\",\"peso\":80,\"repeticiones\":5,\"rpe\":10}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serie.superaSugerida").value(true))
                .andExpect(jsonPath("$.advertencia").isNotEmpty());
        mvc.perform(post("/api/socios/" + socioId + "/series/deshacer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serie.deshecha").value(true))
                .andExpect(jsonPath("$.serie.peso").value(80.0))
                .andExpect(jsonPath("$.seriesParaDeshacer").value(3));
        mvc.perform(post("/api/socios/" + socioId + "/sesiones/" + sesionLunes + "/completar"))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));

        // ---------- El jueves no entrena: el lunes siguiente la sesion del jueves va primero (cola FIFO) ----------
        RELOJ.fijar(Instant.parse("2026-11-30T20:00:00Z"));
        String sesionJueves = JsonPath.read(mvc.perform(get("/api/socios/" + socioId + "/sesiones/hoy"))
                .andExpect(jsonPath("$.recuperada").value(true))
                .andExpect(jsonPath("$.sesion.fechaProgramada").value("2026-11-26"))
                .andExpect(jsonPath("$.sesionesPendientes").value(1))
                .andReturn().getResponse().getContentAsString(), "$.sesion.id");
        mvc.perform(post("/api/socios/" + socioId + "/sesiones/" + sesionJueves + "/omitir"))
                .andExpect(jsonPath("$.estado").value("OMITIDA"));
        mvc.perform(get("/api/socios/" + socioId + "/sesiones/hoy"))
                .andExpect(jsonPath("$.recuperada").value(false))
                .andExpect(jsonPath("$.sesion.fechaProgramada").value("2026-11-30"))
                .andExpect(jsonPath("$.ejercicios[0].decision").value("SUBIR"))
                .andExpect(jsonPath("$.ejercicios[0].cargaSugerida").value(65.0));

        // ---------- Progresion (RF08, RF09) y prioridades (RF11) ----------
        mvc.perform(get("/api/socios/" + socioId + "/progresion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ejercicios[0].ejercicioId").value(sentadilla))
                .andExpect(jsonPath("$.ejercicios[0].semaforo").value("VERDE"))
                .andExpect(jsonPath("$.ejercicios[0].decision").value("SUBIR"))
                .andExpect(jsonPath("$.ejercicios[1].cargaSugerida").value(40.0))
                .andExpect(jsonPath("$.tiempoCalculoMs").isNumber());
        mvc.perform(get("/api/socios/" + socioId + "/prioridades").param("k", "1"))
                .andExpect(jsonPath("$.ejercicios[0].ejercicioId").value(press));
        mvc.perform(get("/api/socios/" + socioId + "/prioridades").param("k", "0"))
                .andExpect(status().isBadRequest());

        // ---------- Lesion: el motor propone un sustituto que no la cargue (RF10) ----------
        enviar(put("/api/socios/" + socioId + "/lesiones"), "{\"lesiones\":[\"CUADRICEPS\"]}")
                .andExpect(status().isOk());
        mvc.perform(get("/api/socios/" + socioId + "/progresion"))
                .andExpect(jsonPath("$.ejercicios[0].decision").value("SUSTITUIR"))
                .andExpect(jsonPath("$.ejercicios[0].sustitutoId").value(zancada));

        // ---------- Historial y reporte PDF (RF12) ----------
        mvc.perform(get("/api/socios/" + socioId + "/historial"))
                .andExpect(jsonPath("$.length()").value(3));
        byte[] pdf = mvc.perform(get("/api/socios/" + socioId + "/reporte/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        // ---------- Eliminacion en cascada (RNF-006) ----------
        mvc.perform(delete("/api/socios/" + socioId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/socios/" + socioId)).andExpect(status().isNotFound());
    }

    @Test
    void documentacionYSaludDelServicio() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Asgard API"));
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Ruta no encontrada"));
    }

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion,
            String cuerpo) throws Exception {
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private static String id(ResultActions resultado) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private static String ejercicio(String nombre, String grupo, String equipamiento, String sustituto) {
        return """
                {"nombre":"%s","grupoMuscular":"%s","tipo":"COMPUESTO","equipamiento":"%s","sustitutos":[%s]}"""
                .formatted(nombre, grupo, equipamiento, sustituto);
    }

    /** Reloj que la prueba puede adelantar para simular el paso de los dias. */
    static final class RelojAjustable extends Clock {

        private volatile Instant ahora;

        RelojAjustable(Instant inicial) {
            this.ahora = inicial;
        }

        void fijar(Instant instante) {
            this.ahora = instante;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            RelojAjustable base = this;
            return new Clock() {
                @Override
                public ZoneId getZone() {
                    return zona;
                }

                @Override
                public Clock withZone(ZoneId otra) {
                    return base.withZone(otra);
                }

                @Override
                public Instant instant() {
                    return base.instant();
                }
            };
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }
}
