package co.edu.ucc.asgard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parametros configurables del motor de progresion (prefijo "asgard.motor", RNF-007).
 * Ver application.properties para el significado de cada valor.
 */
@ConfigurationProperties(prefix = "asgard.motor")
public record ParametrosMotor(
        double incrementoSuperior,
        double incrementoInferior,
        double incrementoAislamiento,
        double redondeoKg,
        int sesionesEstancamiento,
        double umbralRetroceso,
        double porcentajeDeload,
        double porcentajeBajada,
        int fallosParaBajar,
        double umbralSobrecarga,
        double rpeAlerta,
        double factorRojo,
        double factorAmarillo,
        double factorCompuesto,
        double diasSinHistorial,
        int capacidadDeshacer) {

    /** Valores por defecto (los mismos de application.properties), utiles en pruebas. */
    public static ParametrosMotor porDefecto() {
        return new ParametrosMotor(2.5, 5.0, 1.0, 0.5, 3, 0.05, 0.10, 0.05, 2, 0.10, 9.5, 2.0, 1.5, 1.2, 30, 50);
    }
}
