package co.edu.ucc.asgard.modelo;

import co.edu.ucc.asgard.modelo.enums.Sexo;
import java.time.LocalDate;
import java.time.Period;

/** Datos fisicos del socio. Se guardan embebidos en su documento (composicion 1 a 1). */
public class DatosFisicos {

    /** Peso corporal en kilogramos. */
    private double pesoCorporal;
    /** Estatura en centimetros. */
    private double estatura;
    private LocalDate fechaNacimiento;
    private Sexo sexo;

    public DatosFisicos() {
    }

    public DatosFisicos(double pesoCorporal, double estatura, LocalDate fechaNacimiento, Sexo sexo) {
        this.pesoCorporal = pesoCorporal;
        this.estatura = estatura;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
    }

    /** Edad cumplida a la fecha actual. */
    public int edad() {
        return edad(LocalDate.now());
    }

    public int edad(LocalDate hoy) {
        return fechaNacimiento == null ? 0 : Period.between(fechaNacimiento, hoy).getYears();
    }

    /** Indice de masa corporal: peso / estatura(m)^2. */
    public double imc() {
        if (estatura <= 0) {
            return 0;
        }
        double metros = estatura / 100.0;
        return pesoCorporal / (metros * metros);
    }

    public double getPesoCorporal() {
        return pesoCorporal;
    }

    public void setPesoCorporal(double pesoCorporal) {
        this.pesoCorporal = pesoCorporal;
    }

    public double getEstatura() {
        return estatura;
    }

    public void setEstatura(double estatura) {
        this.estatura = estatura;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
}
