package Vista.grafica;

import Controlador.Controlador;
import Modelo.Jugador;
import Vista.IVista;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class VistaGrafica implements IVista {
    private Controlador controlador;
    private VMenuPrincipal menuPrincipal;
    private VNombreJugador nombreJugador;
    private VLobby lobby;
    private VJuego ventanaJuego;
    private VReglas ventana_reglas;
    private boolean juegoMostrado = false;
    private JDRanking ranking;


    public VistaGrafica(Controlador controlador){
        this.controlador  = controlador;
        controlador.setVista(this);
    }

    public void iniciar(){
        menuPrincipal = new VMenuPrincipal(this, controlador);
        nombreJugador = new VNombreJugador(this, controlador);
        lobby = new VLobby(controlador);
        ventanaJuego= new VJuego(controlador, this);
        ventana_reglas = new VReglas(this);
        ranking = new JDRanking(menuPrincipal, this);
        mostrarMenuPrincipal();
    }

    public void iniciar_juego(String nombre, boolean esMiTurno) throws RemoteException {
        lobby.setVisible(false); //estaba: lobby.setvisible(true)
        if (!juegoMostrado) { //para no tener problemas en que se me "reabra" la ventana muchas vecez y me permita minimizarla sin problemas
            ventanaJuego.setTitle("Juego 10mil - En curso. Vista de " + controlador.nombreJugadorVentana());
            ventanaJuego.setVisible(true);
            juegoMostrado = true;
        }
        chequear_botones_y_turno(nombre,esMiTurno); // habilitar/deshabilitar botones y mostrar turno
    }

    //mostrar
    @Override
    public void mostrarMenuPrincipal(){
        menuPrincipal.setVisible(true);
    }

    public void mostrarNombreJugador(){
        nombreJugador.setVisible(true);
    }

    public void mostrarMisDados(ArrayList<Integer> valores){
        ventanaJuego.mostrar_mis_dados(valores);
    }

    public void mostrarDadosOtros(ArrayList<Integer> valores){
        ventanaJuego.mostrar_dados_otros(valores);
    }

    public void mostrarDadosApartados(ArrayList<Integer> d){
        ventanaJuego.dados_apartados(d);
    }

    public void mostrarReglas(){
        ventana_reglas.setVisible(true);
    }

    public void mostrarTablaPuntaje(){
        ventanaJuego.mostrarTabla();
    }

    public void mostrarRanking() throws RemoteException {
        ranking.cargarDatos(controlador.getTablaRanking());
        ranking.setVisible(true);
    }

    @Override
    public void mostrarGanador(String nombre, int puntos){
        ventanaJuego.setVisible(false);
        VGanador ganador = new VGanador(controlador, nombre,puntos);
        ganador.setVisible(true);
        juegoMostrado = false;
    }

    //lobby
    public void mostrarLobby(){
        lobby.setVisible(true);
    }

    public void actualizarLobby(ArrayList<Jugador> jugadores) throws RemoteException{
        System.out.println("VistaGracia. actualizarLobby, hace lobby.actualizarJugadores()");
        lobby.actualizarJugadores(jugadores);
    }

     @Override
    public void lobbyListo() {
        lobby.timer.start();; // SOLO el que arranca
    }

    //mensajes
    public void mensajeSePlanto(String nombre, int puntos) throws RemoteException {
        ventanaJuego.deshabilitarBotonesTodos();
        ventanaJuego.msjPlantado(nombre, puntos);
    }

    public void msjJugadorFuera(){
        ventanaJuego.jugador_fuera();
    }

    public void msjNombreRepetido(){
        ventanaJuego.nombre_repetido();
    }

    public void mensajeEscalera(String nombre) throws RemoteException {
        ventanaJuego.deshabilitarBotonesTodos();
        ventanaJuego.msjEscalera(nombre);
    }

    public void mensajeDadosSinPuntos(String nombre) throws RemoteException {
        ventanaJuego.deshabilitarBotonesTodos();
        ventanaJuego.msjDadosSinPuntos(nombre);
    }

    public void mensajeMaxApartado(String nombre, int punto) throws RemoteException {
        ventanaJuego.msjMaxApartado(nombre, punto);
    }

    //botones y turnos
    public void chequear_botones_y_turno(String nombre, boolean esMiTurno){
        ventanaJuego.limpiar_dados_mesa();
        ventanaJuego.turno(nombre);
        if(esMiTurno){
            ventanaJuego.habilitarBotonLanzar();
        }else{
            ventanaJuego.deshabilitarBotonesTodos();
        }
    }

    public void solo_chequear_botones(boolean esMiTurno){
        if(esMiTurno){
            ventanaJuego.habilitarBotonLanzar();
        }else{
            ventanaJuego.deshabilitarBotonesTodos();
        }
    }

    public void habilitarBotonesPlantarseOApartar(){
        ventanaJuego.habilitarBotonesPlantarseYApartar();
    }

    // otros
    public void agregarPuntajeRondaTabla(int puntaje, String nombre, int puntajeTotal, int ronda){
        ventanaJuego.actualizarTablaPuntaje(puntaje, nombre, puntajeTotal, ronda);
    }

    public void limpiarPantalla() {
        ventanaJuego.limpiar_dados_mesa();
    }

    public void limpiarTablaPuntaje(){
        ventanaJuego.limpiar_tabla_puntaje();
    }

}
