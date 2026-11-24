package co.edu.ucc.asgard.modelo;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/** Un dia de entrenamiento de la rutina, con sus prescripciones en orden. Se guarda embebido en {@link Rutina}. */
public class DiaRutina {

    private DayOfWeek dia;
    /** Nombre del dia, por ejemplo "Torso A" o "Pierna". */
    private String nombre;
    private List<Prescripcion> prescripciones = new ArrayList<>();

    public DiaRutina() {
    }

    public DiaRutina(DayOfWeek dia, String nombre, List<Prescripcion> prescripciones) {
        this.dia = dia;
        this.nombre = nombre;
        setPrescripciones(prescripciones);
    }

    public DayOfWeek getDia() {
        return dia;
    }

    public void setDia(DayOfWeek dia) {
        this.dia = dia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Prescripcion> getPrescripciones() {
        return prescripciones;
    }

    public void setPrescripciones(List<Prescripcion> prescripciones) {
        this.prescripciones = prescripciones == null ? new ArrayList<>() : new ArrayList<>(prescripciones);
    }
}
