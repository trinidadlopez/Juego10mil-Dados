package Modelo;

import Excepcion.MismoNombre;
import Excepcion.PartidaIniciada;
import Serializador.AdministradorRanking;
import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

import static Modelo.Eventos.*;

public class Juego extends ObservableRemoto implements Serializable, IJuego {
    private ArrayList<Jugador> jugadores;
    private Jugador jugadorActual;
    private int siguiente_nroJ = 0;
    private Reglas reglas;
    private int nroRonda = 1;
    private EstadoJugada estadoJugada;
    private Cubilete cubilete;
    private AdministradorRanking admRanking = new AdministradorRanking();
    private ArrayList<Jugador> ranking;
    private ArrayList<Integer> jugadoresConfirmados;

    public Juego() {
        estadoJugada = EstadoJugada.ESPERANDO_JUGADORES; //arranca en este estado, estamos en el lobby
        jugadores = new ArrayList<>();
        jugadoresConfirmados = new ArrayList<>();
        reglas = Reglas.getInstance();
        cubilete = new Cubilete();
        this.ranking = this.admRanking.cargarRanking(); //le pide al administrador de ranking que cargue el ranking(desde el archivo) y guarda el resultado en el atributo ranking
        //admRanking.vaciarRanking();
    }

    // CONFIG INICIAL=================
    @Override
    public int siguienteNroJ() { //jugador.id
        siguiente_nroJ = siguiente_nroJ + 1;
        return siguiente_nroJ;
    }

    @Override
    public int iniciar_jugador(String nombre) throws RemoteException {
        if(estadoJugada != EstadoJugada.ESPERANDO_JUGADORES){ //si no esta en este estado significa que la partida ya comenzo
            throw new PartidaIniciada("mensaje de partida iniciada-error"); //excepcion
        }
        for(Jugador j: jugadores){
            if(j.getNombreJugador().equals(nombre)){
                throw new MismoNombre("ese nombre ya existe-error"); //excepcion
            }
        }
        Jugador jugador = new Jugador(nombre, new ArrayList<Dado>());
        jugador.setNroJugador(siguiente_nroJ+1);
        jugadores.add(jugador);
        if (jugadores.size() == 1) {
            jugadorActual = jugador;
        }
        notificarObservadores(JUGADOR_AGREGADO);

        if(sePuedeIniciarLobby()){
            notificarObservadores(INICIAR_LOBBY);
        }
        return siguienteNroJ();
    }

    @Override
    public void comenzarJuego() throws RemoteException {
        estadoJugada = EstadoJugada.JUEGO_ACTIVO; //cambia el estado a JUEGO_ACTIVO para que no se puedan unir mas jugadores porque la partida ya comenzo
        notificarObservadores(COMENZAR_JUEGO);
    }

    // CONFIG EN JUEGO=================
    public void lanzar() throws RemoteException {
        ArrayList<Dado> resultados = cubilete.tirarse();
        jugadorActual.setDadosParciales(resultados);
        notificarObservadores(DADOS_LANZADOS);
        chequear_estado_tirada(); //cheqeuo en que estado estan mis dados. METODO PRIV-----------------
        analizar_estado_tirada(); //me fijo que hago con ese estado. METODO PRIV-----------------
    }

    public void apartar_dados() throws RemoteException {
        reglas = Reglas.getInstance();
        ArrayList<Dado> dadosConPuntos = reglas.obtenerDadosConPuntos(jugadorActual.getDadosParciales());
        for (Dado d : dadosConPuntos) {
            jugadorActual.setDadosApartados(d);
        }
        jugadorActual.getDadosParciales().clear();

        cubilete.actualizar_cubilete(jugadorActual.getDadosApartados());

        if (jugadorActual.getDadosApartados().size() == 5) {
            jugadorActual.setPuntajeParcial(
                    reglas.calcularPuntaje(jugadorActual.getDadosApartados())
            );
            jugadorActual.setPuntajeTotal();
            notificarObservadores(MAX_APARTADOS);
            return;
        }
        notificarObservadores(DADOS_APARTADOS);
    }

    public void jugador_plantado() throws RemoteException {
        estadoJugada = EstadoJugada.SE_PLANTO; //el jugador decidio plantarse, se debe guardar los puntos hasta ese momento (a dif. de DADOS_SIN_PUNOS)
        actualizar_parciales(); //METODO PRIV-----------------
        jugadorActual.setPuntajeParcial(reglas.calcularPuntaje(jugadorActual.getDadosParciales()));
        jugadorActual.setPuntajeTotal();
        notificarObservadores(PLANTADO);
    }

    // CONTROLES=================
    public void actualizar_turno_jugador() throws RemoteException {
        cubilete.reestablecer_cubilete();
        jugadorActual.getDadosParciales().clear();
        jugadorActual.getDadosApartados().clear();

        if (jugadorActual.getPuntajeTotal() >= 10000) { //son 10.000 puntos, pongo menos para chequear cosas
            finalizar_partida(jugadorActual);
            return;
        }

        int i = jugadores.indexOf(jugadorActual);
        i++;
        i = i % jugadores.size();
        jugadorActual = jugadores.get(i);
        if (jugadores.getFirst() == jugadorActual) {
            nroRonda++; //solo incremento el nro de ronda cuando paso por de nuevo por el primer jugador.
        }
        notificarObservadores(ACTUALIZACION_TURNO);
    }

    // METODOS PRIV=================
    private void chequear_estado_tirada() {
        estadoJugada = reglas.decime_el_estado(jugadorActual.getDadosParciales());
    }

    private void analizar_estado_tirada() throws RemoteException { //analizo que hacer segun el estado en el que se encuentra. Lo llamo desde lanzar()
        switch (estadoJugada) {
            case TIENE_ESCALERA:
                jugadorActual.setPuntajeParcial(500);
                jugadorActual.setPuntajeTotal();
                notificarObservadores(Eventos.ESCALERA_OBTENIDA); //notifico escalera, sumo puntos y cambio el turno
                break;
            case TIENE_DADOS_CON_PUNTOS:
                notificarObservadores(Eventos.DADOS_CON_PUNTOS); //habilito los botones de apartar o plantarse
                break;
            case TIENE_DADOS_SIN_PUNTOS:
                jugadorActual.setPuntajeParcial(0);
                notificarObservadores(DADOS_SIN_PUNTOS); //msj perdio los puntos y actualizo turno
                break;
        }
    }

    private void actualizar_parciales() {
        for (Dado d : jugadorActual.getDadosApartados()) {
            jugadorActual.getDadosParciales().add(d);
        }
    }

    // GETTERS=================
    public int getNroRonda() {
        return nroRonda;
    }

    @Override
    public Object[][] getTablaRanking() throws RemoteException {
        Object[][] datos = new Object[this.ranking.size()][3]; //3 columnas: nombre, fecha, puntajeGanador
        int i = 0;
        for (Jugador j : this.ranking) {
            datos[i][0] = j.getNombreJugador();
            datos[i][1] = j.getPuntajeTotal();
            datos[i][2] = j.getFechaJugado();
            i++;
        }
        return datos;
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    @Override
    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public EstadoJugada getEstadoJugada() {
        return estadoJugada;
    }

    // resetear y finalizar
    public void resetearJuego() throws RemoteException{
        jugadores.clear();
        jugadorActual = null;
        siguiente_nroJ=0;
        nroRonda=1;
        estadoJugada=null;
        cubilete.reestablecer_cubilete();
        estadoJugada=EstadoJugada.ESPERANDO_JUGADORES; //para volver al estado inicial
    }

    private void finalizar_partida(Jugador jugador) throws RemoteException {
        ranking.add(jugador);
        admRanking.guardarRanking(ranking);
        notificarObservadores(JUGADOR_GANADOR);
        resetearJuego();
    }

    // cosas nuevas
    private boolean sePuedeIniciarLobby(){
        return jugadores.size() == 2;
    }

    public boolean jugadorEsta(String nombre) throws RemoteException{
        for(Jugador j: jugadores){
            if(j.getNombreJugador().equals(nombre)){
                return true;
            }
        }
        return false;
    }


    public void confirmacion_del_puntaje(String nombre) throws RemoteException{ //sirve para controlar que todos hayan apretado ok o se les haya expirado el timer para poder continuar con el juego
        int nro = -1;
        for(Jugador j : jugadores){
            if(j.getNombreJugador().equals(nombre)){
                nro = j.getNroJugador();
                break;
            }
        }
        if(nro != -1){
            jugadoresConfirmados.add(nro);
        }
        if(jugadoresConfirmados.size() == jugadores.size()){ //cuando todos los jugadores confirmaron
            jugadoresConfirmados.clear();
            actualizar_turno_jugador(); //actualiza el turno y continuael juego
        }
    }
}
