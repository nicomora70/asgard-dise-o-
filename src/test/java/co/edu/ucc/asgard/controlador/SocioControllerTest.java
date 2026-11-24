package co.edu.ucc.asgard.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.ucc.asgard.dto.socio.SocioResponse;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.modelo.enums.Nivel;
import co.edu.ucc.asgard.modelo.enums.Objetivo;
import co.edu.ucc.asgard.servicio.SocioService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SocioController.class)
class SocioControllerTest {

    private static final String CUERPO = "{\"nombre\":\"Laura Gomez\",\"documentoIdentidad\":\"1001\","
            + "\"nivel\":\"INTERMEDIO\",\"objetivo\":\"HIPERTROFIA\"}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SocioService socioService;

    @Test
    void registrarDevuelve201ConElId() throws Exception {
        when(socioService.registrar(any())).thenReturn(new SocioResponse("socio1", "Laura Gomez", "1001", null, null,
                Nivel.INTERMEDIO, Objetivo.HIPERTROFIA, LocalDate.parse("2026-11-24"), null, List.of(), null, 0, 0,
                Instant.parse("2026-11-24T12:00:00Z")));

        mvc.perform(post("/api/socios").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/socios/socio1"))
                .andExpect(jsonPath("$.id").value("socio1"));
    }

    @Test
    void documentoRepetidoDevuelve409() throws Exception {
        when(socioService.registrar(any())).thenThrow(new ReglaNegocioException("Ya existe un socio con el documento 1001"));

        mvc.perform(post("/api/socios").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un socio con el documento 1001"));
    }

    @Test
    void enumInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/socios").contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO.replace("INTERMEDIO", "EXPERTO")))
                .andExpect(status().isBadRequest());
    }
}
