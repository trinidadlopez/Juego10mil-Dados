package Controlador;

import Excepcion.MismoNombre;
import Excepcion.PartidaIniciada;
import Modelo.Dado;
import Modelo.Eventos;
import Modelo.IJuego;
import Modelo.Jugador;
import Vista.IVista;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Controlador implements IControladorRemoto {
    private IJuego juego;
    private IVista vista;
    //private int nroJugador;
    private String nombreJugador; //se inicializa en null


    public Controlador() {
    }

    // ACCIONES QUE VIENEN DE LA VISTA =================
    public void iniciarJugador(String nombre) throws RemoteException, PartidaIniciada{
        this.nombreJugador = nombre;
        try{
            juego.iniciar_jugador(nombre); //para que agregue el jugador al array de jugadores
        }
        catch(PartidaIniciada a){ //si la partida ya comenzo
            vista.msjJugadorFuera(); //le avisa
            juego.removerObservador(this); //desconecta al modelo
            return;
        }
        catch (MismoNombre a){ //si puso un nombre repetido
            vista.msjNombreRepetido();
            return;
        }
        vista.mostrarLobby(); //si todo esta ok, muestra el loby
    }

    public void comenzarJuego() throws RemoteException{ //es llamado por el lobby cuando el timer expira o hay 6 jugadores
        juego.comenzarJuego();
    }

    public void plantarse() throws RemoteException { //el jugador presiono o eligio la opcion de plantarse
        juego.jugador_plantado();
    }

    public void lanzar_dados() throws RemoteException { //el jugador presiono o eligio la opcion de lanzar
        juego.lanzar();
    }

    public void apartarDados() throws RemoteException { //el jugador presiono o eligio la opcion de apartar
        juego.apartar_dados();
    }

    // OBSERVER =================
    @Override
    public void actualizar(IObservableRemoto iObservableRemoto, Object o) throws RemoteException {
        if(!juego.jugadorEsta(nombreJugador) || nombreJugador==null){ //si el jugador no esta en la partida actual o todavia no ingreso su nombre ignora todas los eventos(notificaciones). Sin esto los observadores que estaban en el menu principal recibia las notificaciones de las partidas igualmente.
            return;
        }
        try{
            Eventos evento = (Eventos) o;
            switch (evento){
                case JUGADOR_AGREGADO:
                    vista.actualizarLobby(juego.getJugadores());
                    break;
                case INICIAR_LOBBY:
                    if(juego.getJugadores().get(0).getNombreJugador().equals(nombreJugador)){ //solo el primer jugador va a activar el timer del lobby
                        vista.lobbyListo();
                    }
                    break;
                case COMENZAR_JUEGO:
                    if(!juego.jugadorEsta(nombreJugador)){ //controlo nuevamente, por las dudas
                        terminarJuego();
                    }else {
                        boolean esMiTurno = juego.getJugadorActual().getNombreJugador().equals(nombreJugador);
                        vista.iniciar_juego(juego.getJugadorActual().getNombreJugador(), esMiTurno); //para que la vista sepa si tiene que habilitar/deshabilitar botones
                    }
                    break;
                case DADOS_LANZADOS:
                    ArrayList<Integer> valores = new ArrayList<>();
                    for (Dado d : juego.getJugadorActual().getDadosParciales()){
                        valores.add(d.getValorCaraSuperior());
                    }
                    if (juego.getJugadorActual().getNombreJugador().equals(nombreJugador)) { //me fijo si son mis dados o del otro para cambiar el msj
                        vista.mostrarMisDados(valores);
                    } else {
                        vista.mostrarDadosOtros(valores);
                    }
                    break;
                case DADOS_CON_PUNTOS: //es solo para el jugador en turno
                    if(juego.getJugadorActual().getNombreJugador().equals(nombreJugador)){
                        vista.habilitarBotonesPlantarseOApartar();
                    }
                    break;
                case ACTUALIZACION_TURNO: //verifico en cada turno, para saber si habilitar o no los botones
                    boolean es_mi_turno = juego.getJugadorActual().getNombreJugador().equals(nombreJugador);
                    vista.chequear_botones_y_turno(juego.getJugadorActual().getNombreJugador(), es_mi_turno);
                    break;
                case DADOS_APARTADOS:
                    ArrayList<Integer> dadosA = new ArrayList<>();
                    for (Dado d : juego.getJugadorActual().getDadosApartados()) {
                        dadosA.add(d.getValorCaraSuperior());
                    }
                    vista.mostrarDadosApartados(dadosA);
                    boolean es_miturno = juego.getJugadorActual().getNombreJugador().equals(nombreJugador); //si es mi turno, luego de apartar se me tiene que habilitar el boton de lanzar de nuevo
                    vista.solo_chequear_botones(es_miturno);
                    break;
                case MAX_APARTADOS: //a partir de aca para abajo, notifico a todos por igual lo que esta sucediendo
                    vista.mensajeMaxApartado(juego.getJugadorActual().getNombreJugador(), juego.getJugadorActual().getPuntajeParcial());
                    break;
                case ESCALERA_OBTENIDA:
                    vista.mensajeEscalera(juego.getJugadorActual().getNombreJugador());
                    break;
                case DADOS_SIN_PUNTOS:
                    vista.mensajeDadosSinPuntos(juego.getJugadorActual().getNombreJugador());
                    break;
                case PLANTADO:
                    vista.mensajeSePlanto(juego.getJugadorActual().getNombreJugador() ,juego.getJugadorActual().getPuntajeParcial() );
                    break;
                case JUGADOR_GANADOR:
                    vista.mostrarGanador(juego.getJugadorActual().getNombreJugador() , juego.getJugadorActual().getPuntajeTotal());
                    break;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void confirmar_mensaje() throws RemoteException { //lo llama la vista cuando :se confirma que el jugador vio algunos de los mensajes de JDMensajes y puso OK, hace lo sigunte:
        vista.agregarPuntajeRondaTabla(juego.getJugadorActual().getPuntajeParcial(), juego.getJugadorActual().getNombreJugador(), juego.getJugadorActual().getPuntajeTotal(), juego.getNroRonda());
        vista.mostrarTablaPuntaje();
    }

    public void respuesta_puntaje() throws RemoteException { //lo llama la vista cuando: ya vio la tabla de puntajes y apreto OK, hace esto:
        juego.confirmacion_del_puntaje(nombreJugador);
    }

    //METODOS AUX. =================
    public Object[][] getTablaRanking() throws RemoteException { //lo usa la vistapara mostrar la tabla
        return juego.getTablaRanking();
    }

    public ArrayList<Jugador> jugadoresLobby() throws RemoteException { //lo usa la vista consola para ver los juadores q se encuentran en el lobby
        return juego.getJugadores();
    }

    public String nombreJugadorVentana() throws RemoteException{
        if(nombreJugador != null){
            return nombreJugador;
        }
        else{
            return "";
        }
    }

    //CONFIG. =================
    public void setVista(IVista vista){
        this.vista = vista;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T t) throws RemoteException {
        this.juego= (IJuego) t;
    }

    //RESETEO y FINALIZACION =================
    public void volverAJugar() throws RemoteException{
        vista.limpiarTablaPuntaje();
        vista.mostrarMenuPrincipal();
    }

    public void terminarJuego() throws RemoteException{
        juego.removerObservador(this);
        System.exit(0);
    }
}
