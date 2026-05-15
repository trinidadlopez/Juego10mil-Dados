package Excepcion;

public class PartidaIniciada extends RuntimeException {
    public PartidaIniciada(String message) {
        super(message);
    }
}
