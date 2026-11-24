package co.edu.ucc.asgard.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.ucc.asgard.dto.serie.RespuestasSerie.RegistroSerieResponse;
import co.edu.ucc.asgard.dto.serie.SerieResponse;
import co.edu.ucc.asgard.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.modelo.enums.RegistradoPor;
import co.edu.ucc.asgard.servicio.SerieService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SerieController.class)
class SerieControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SerieService serieService;

    @Test
    void registrarConSobrecargaDevuelve201ConAdvertenciaNoBloqueante() throws Exception {
        SerieResponse serie = new SerieResponse("serie1", "socio1", "sesion1", "press", "Press de banca", 1, 72, 8,
                8.0, 91.2, 60.0, true, Instant.parse("2026-11-24T12:00:00Z"), RegistradoPor.SOCIO, null, false, null);
        when(serieService.registrar(eq("socio1"), any())).thenReturn(new RegistroSerieResponse(serie,
                "La carga (72.0 kg) supera en 20 % la sugerida (60.0 kg)", 1));

        mvc.perform(post("/api/socios/socio1/series").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ejercicioId\":\"press\",\"peso\":72,\"repeticiones\":8,\"rpe\":8}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serie.superaSugerida").value(true))
                .andExpect(jsonPath("$.advertencia").value("La carga (72.0 kg) supera en 20 % la sugerida (60.0 kg)"))
                .andExpect(jsonPath("$.seriesParaDeshacer").value(1));
    }

    @Test
    void registrarValidaElCuerpoYRespondeEnEspanol() throws Exception {
        mvc.perform(post("/api/socios/socio1/series").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ejercicioId\":\"\",\"peso\":-5,\"repeticiones\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La peticion tiene campos invalidos"))
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void ejercicioInexistenteDevuelve404() throws Exception {
        when(serieService.registrar(eq("socio1"), any()))
                .thenThrow(new RecursoNoEncontradoException("No existe el ejercicio xyz"));

        mvc.perform(post("/api/socios/socio1/series").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ejercicioId\":\"xyz\",\"peso\":50,\"repeticiones\":10}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el ejercicio xyz"));
    }

    @Test
    void deshacerSinSeriesDevuelve409() throws Exception {
        when(serieService.deshacerUltima("socio1")).thenThrow(new ReglaNegocioException("No hay series para deshacer"));

        mvc.perform(post("/api/socios/socio1/series/deshacer"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No hay series para deshacer"));
    }

    @Test
    void validarSinParametroObligatorioDevuelve400() throws Exception {
        mvc.perform(get("/api/socios/socio1/series/validar").param("ejercicioId", "press").param("peso", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parametro obligatorio 'repeticiones'"));
    }
}
