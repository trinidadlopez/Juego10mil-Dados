package Vista;

import Modelo.Jugador;

import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IVista {
    //inicio/menu
    void mostrarMenuPrincipal();
    void mostrarReglas();
    void iniciar() throws RemoteException;
    void limpiarPantalla();

    //lobby
    void lobbyListo();
    void mostrarLobby() throws RemoteException;
    void actualizarLobby(ArrayList<Jugador> j) throws RemoteException;
    void msjJugadorFuera() throws RemoteException;

    //inicio partida
    void iniciar_juego(String nombre, boolean esMiTurno) throws RemoteException;

    //turnos, dados y botones
    void mostrarDadosOtros(ArrayList<Integer> valores);
    void mostrarMisDados(ArrayList<Integer> valores);
    void mostrarDadosApartados(ArrayList<Integer> d);
    void habilitarBotonesPlantarseOApartar();
    void chequear_botones_y_turno(String nombre, boolean esMiTurno);
    void solo_chequear_botones(boolean esMiTurno);

    //msj´s
    void mensajeSePlanto(String nombre, int puntos) throws RemoteException;
    void mensajeEscalera(String nombre) throws RemoteException;
    void mensajeDadosSinPuntos(String nombre) throws RemoteException;
    void mostrarGanador(String nombre, int puntos);
    void mensajeMaxApartado(String nombre, int punto) throws RemoteException;
    void msjNombreRepetido() throws RemoteException;

    //puntajes
    void agregarPuntajeRondaTabla(int puntaje, String nombre, int puntajeTotal, int ronda);
    void mostrarTablaPuntaje() throws RemoteException;
    void limpiarTablaPuntaje() throws RemoteException;


}
