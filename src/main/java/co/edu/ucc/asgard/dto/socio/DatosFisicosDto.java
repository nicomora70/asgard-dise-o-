package co.edu.ucc.asgard.dto.socio;

import co.edu.ucc.asgard.modelo.DatosFisicos;
import co.edu.ucc.asgard.modelo.enums.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/** Datos fisicos del socio: peso en kg y estatura en cm. */
public record DatosFisicosDto(
        @DecimalMin(value = "20", message = "debe ser al menos 20 kg") @DecimalMax(value = "350", message = "debe ser maximo 350 kg") double pesoCorporal,
        @DecimalMin(value = "100", message = "debe ser al menos 100 cm") @DecimalMax(value = "250", message = "debe ser maximo 250 cm") double estatura,
        @Past(message = "debe ser una fecha pasada") LocalDate fechaNacimiento,
        Sexo sexo,
        Integer edad,
        Double imc) {

    public DatosFisicos aModelo() {
        return new DatosFisicos(pesoCorporal, estatura, fechaNacimiento, sexo);
    }

    public static DatosFisicosDto desde(DatosFisicos datos, LocalDate hoy) {
        if (datos == null) {
            return null;
        }
        return new DatosFisicosDto(datos.getPesoCorporal(), datos.getEstatura(), datos.getFechaNacimiento(),
                datos.getSexo(), datos.getFechaNacimiento() == null ? null : datos.edad(hoy),
                Math.round(datos.imc() * 10.0) / 10.0);
    }
}
