package co.edu.ucc.asgard.modelo.enums;

/** Decision del motor sobre la carga de la siguiente sesion (RF08, RF10). Lo calcula el motor; no se guarda. */
public enum DecisionCarga {
    SUBIR,
    MANTENER,
    BAJAR,
    DELOAD,
    SUSTITUIR
}
