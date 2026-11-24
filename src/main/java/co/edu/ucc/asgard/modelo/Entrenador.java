package co.edu.ucc.asgard.modelo;

import co.edu.ucc.asgard.modelo.enums.Turno;
import org.springframework.data.mongodb.core.mapping.Document;

/** Entrenador del gimnasio (coleccion "entrenadores"). Atiende a varios socios hasta su cupo maximo. */
@Document(collection = "entrenadores")
public class Entrenador extends Persona {

    private Turno turno;
    private int maximoSocios;

    /** Indica si el entrenador puede recibir un socio mas. */
    public boolean tieneCupo(int asignados) {
        return asignados < maximoSocios;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public int getMaximoSocios() {
        return maximoSocios;
    }

    public void setMaximoSocios(int maximoSocios) {
        this.maximoSocios = maximoSocios;
    }
}
