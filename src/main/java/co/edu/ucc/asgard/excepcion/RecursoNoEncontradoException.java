package co.edu.ucc.asgard.excepcion;

/** El recurso pedido no existe: la API responde 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
