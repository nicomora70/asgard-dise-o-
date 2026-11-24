package co.edu.ucc.asgard.modelo;

import co.edu.ucc.asgard.modelo.enums.Equipamiento;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.modelo.enums.TipoEjercicio;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Ejercicio del catalogo (coleccion "ejercicios"). Embebe la lista de sus sustitutos (RF02). */
@Document(collection = "ejercicios")
public class Ejercicio {

    @Id
    private String id;
    @Indexed(unique = true)
    private String nombre;
    @Indexed
    private GrupoMuscular grupoMuscular;
    private List<GrupoMuscular> gruposSecundarios = new ArrayList<>();
    private TipoEjercicio tipo;
    private Equipamiento equipamiento;
    /** Incremento de carga propio (kg). Si es 0 se usa el incremento por defecto segun el tipo. */
    private double incrementoKg;
    /** Id del entrenador que lo creo. */
    private String creadoPor;
    private boolean activo = true;
    private List<Sustituto> sustitutos = new ArrayList<>();

    /** Indica si el ejercicio trabaja el grupo dado, como grupo principal o secundario. */
    public boolean cargaGrupo(GrupoMuscular grupo) {
        return grupoMuscular == grupo || (gruposSecundarios != null && gruposSecundarios.contains(grupo));
    }

    public boolean esCompuesto() {
        return tipo == TipoEjercicio.COMPUESTO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public GrupoMuscular getGrupoMuscular() {
        return grupoMuscular;
    }

    public void setGrupoMuscular(GrupoMuscular grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    public List<GrupoMuscular> getGruposSecundarios() {
        return gruposSecundarios;
    }

    public void setGruposSecundarios(List<GrupoMuscular> gruposSecundarios) {
        this.gruposSecundarios = gruposSecundarios == null ? new ArrayList<>() : new ArrayList<>(gruposSecundarios);
    }

    public TipoEjercicio getTipo() {
        return tipo;
    }

    public void setTipo(TipoEjercicio tipo) {
        this.tipo = tipo;
    }

    public Equipamiento getEquipamiento() {
        return equipamiento;
    }

    public void setEquipamiento(Equipamiento equipamiento) {
        this.equipamiento = equipamiento;
    }

    public double getIncrementoKg() {
        return incrementoKg;
    }

    public void setIncrementoKg(double incrementoKg) {
        this.incrementoKg = incrementoKg;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Sustituto> getSustitutos() {
        return sustitutos;
    }

    public void setSustitutos(List<Sustituto> sustitutos) {
        this.sustitutos = sustitutos == null ? new ArrayList<>() : new ArrayList<>(sustitutos);
    }
}
