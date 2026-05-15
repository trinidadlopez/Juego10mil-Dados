package Modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IJuego extends IObservableRemoto {
    //config inicial
    int siguienteNroJ() throws RemoteException;
    int iniciar_jugador(String nombre) throws RemoteException;
    void comenzarJuego() throws RemoteException;

    //acciones del juego
    void lanzar()throws RemoteException;
    void apartar_dados() throws RemoteException;
    void actualizar_turno_jugador() throws RemoteException;
    void jugador_plantado() throws RemoteException;

    //consultas de estados
    Object[][] getTablaRanking() throws RemoteException;
    ArrayList<Jugador> getJugadores()throws RemoteException;
    int getNroRonda() throws RemoteException;
    Jugador getJugadorActual() throws RemoteException;
    EstadoJugada getEstadoJugada() throws RemoteException;
    //volver a empezar
    void resetearJuego() throws RemoteException;

    //agregados
    boolean jugadorEsta(String nombre) throws RemoteException;
    void confirmacion_del_puntaje(String nombre) throws RemoteException;
}
