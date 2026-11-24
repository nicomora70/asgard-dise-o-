package co.edu.ucc.asgard.controlador;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Pagina de inicio de la API con los enlaces principales. */
@RestController
@Hidden
public class InicioController {

    @GetMapping("/")
    public Map<String, String> inicio() {
        Map<String, String> enlaces = new LinkedHashMap<>();
        enlaces.put("servicio", "Asgard API - progresion de cargas en el gimnasio");
        enlaces.put("documentacion", "/swagger-ui.html");
        enlaces.put("openapi", "/v3/api-docs");
        enlaces.put("salud", "/actuator/health");
        return enlaces;
    }
}
