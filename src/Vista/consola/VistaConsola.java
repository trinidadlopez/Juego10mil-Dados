package Vista.consola;

import Controlador.Controlador;
import Modelo.Jugador;
import Vista.IVista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

public class VistaConsola extends JFrame implements IVista{
    private Controlador controlador;
    private JTextArea txtSalida; //es la "pantalla" de la consola. Despues en vez de usar System.ut.print se usa txtSalida.append(..)
    private JPanel panel;
    private JTextField txtEntradaJugador; //simula la entrada por teclado, en vez de usar scanner
    private EstadoVistaConsola estado; //estado actual
    private Timer timer;
    private ArrayList<String[]> tablaPuntajes = new ArrayList<>();
    private JPanel panelAbajo;
    private JLabel indicacion;

    public VistaConsola(Controlador controlador) throws RemoteException { //armo toda la ventana
        this.controlador = controlador;
        txtSalida = new JTextArea();
        txtEntradaJugador = new JTextField();
        panel = new JPanel(new BorderLayout());
        panel.add(txtSalida, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(txtSalida);
        panel.add(scrollPane, BorderLayout.CENTER);

        panelAbajo = new JPanel();
        indicacion = new JLabel("Escriba aqui: ");
        panelAbajo.setLayout(new BoxLayout(panelAbajo, BoxLayout.X_AXIS));
        panelAbajo.add(indicacion);
        panelAbajo.add(txtEntradaJugador);
        panel.add(panelAbajo, BorderLayout.SOUTH);

        setSize(500, 700);
        setLocationRelativeTo(null);
        setContentPane(panel);
        txtSalida.setEditable(false); //el usuario no puede escribir en el area de salida

        iniciar();

        txtEntradaJugador.addActionListener(new ActionListener() { //presiona "enter" en su teclado
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    procesarEntradaJugador(txtEntradaJugador.getText());
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
                txtEntradaJugador.setText(""); //limpia el campo de entrada
            }
        });
    }

    // Utilidades=================
    private void print(String string) { //simula System.out.print
        txtSalida.append(string);
        txtSalida.setCaretPosition(txtSalida.getDocument().getLength());
    }

    private void println(String string) { //simula System.out.println
        print(string + "\n");
        txtSalida.setCaretPosition(txtSalida.getDocument().getLength());
    }

    @Override
    public void limpiarPantalla() { //para vaciar el texto que se ve cada vez que cambia de vista. asi no se ve todo acumulado
        txtSalida.setText("");
    }

    // LOBBY=================
    @Override
    public void lobbyListo(){ //lo llama el controlador cuando hay al menos 2 jugadores en el lobby, para que empiece a correr el timer
        if(timer == null || !timer.isRunning()){
            iniciarTimer();
            timer.start();
        }
    }

    @Override
    public void actualizarLobby(ArrayList<Jugador> jugadores) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            limpiarPantalla();
            mostrarJugadores(jugadores); //muestro la cantidad de jugadores actuales en el lobby, cada vez q uno ingresa
            if(jugadores.size() == 1 ){
                println("Esperando al menos 2 jugadores (" + jugadores.size() + "/6)");
            }else{
                println("Esperando a mas jugadores... Jugadores conectados: " + jugadores.size() + "/6");
            }
            if(jugadores.size() == 6 && timer != null && timer.isRunning()){ //si ya hay 6 jugadores para el timer, y comienza el juego
                timer.stop();
                try {
                    controlador.comenzarJuego();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void mostrarJugadores(ArrayList<Jugador> jugadores){
        txtEntradaJugador.setEditable(false);
        println("Jugadores en lobby:");
        for(Jugador j: jugadores){
            println("ID: " + j.getNroJugador() + " - Nombre: " + j.getNombreJugador() );
        }
    }


    // INICIOS=================
    @Override
    public void iniciar(){ //conecta la vista con el controlador y muestra el menu principal
        controlador.setVista(this);
        setVisible(true);
        mostrarMenuPrincipal();
    }

    @Override
    public void iniciar_juego(String nombre, boolean esMiTurno){ //limpio la pantalla y muestro el turno del jugador actual
        limpiarPantalla();
        println("EL JUEGO COMENZÓ");
        println("-----------------------");
        println("Es el turno del jugador/a: " + nombre);
        if(esMiTurno){
            txtEntradaJugador.setEditable(true);
            println("");
            println("Es tu turno!");
            println("1. LANZAR los dados");
            estado = EstadoVistaConsola.LANZAR_DADOS;
        }else{
            println("");
            println("Esperando que " + nombre + " juegue...");
            txtEntradaJugador.setEditable(false);
        }
    }

    // MENU PRINCIPAL=================
    @Override
    public void mostrarMenuPrincipal(){
        estado= EstadoVistaConsola.MENU_PRINCIPAL;
        limpiarPantalla();
        println("MENÚ PRINCIPAL:");
        println("1. Jugar");
        println("2. Reglas y Cómo Jugar");
        println("3. Ranking");
        println("4. Salir");

        print("Seleccione una opcion ");
    }

    public void mostrarNombreJugador(){
        estado = EstadoVistaConsola.INGRESO_NOMBRE_JUGADOR;
        limpiarPantalla();
        print("Ingrese su nombre: ");
    }

    public void procesarEntradaMP(String entrada) throws RemoteException { //procesa la opcion que eligio el jugador del menu principal. solo permite 1,2,3 o 4, otra opcion da error
        println(entrada);
        switch(entrada){
            case "1": //presiono. jugar
                mostrarNombreJugador();
                break;
            case "2": //presiono. reglas
                mostrarReglas();
                break;
            case "3":
                mostrarRanking();
                break;
            case "4": //presiono. salir
                println("¡Hasta luego!");
                controlador.terminarJuego();
                break;
            default: //puse algo invalido
                println("");
                println("Opcion no valida. Por favor elija una opcion valida.");
                mostrarMenuPrincipal();
                break;
        }
    }

    // PROCESAMIENTO DE ENTRADAS=================
    private void procesarEntradaJugador(String entrada) throws RemoteException { //se ejecuta cada vez que un jugador presiona enter, y se delega al metodo correspondiente segun el estado
        switch (estado){
            case MENU_PRINCIPAL:
                procesarEntradaMP(entrada);
                break;
            case REGLAS:
                procesarVolverReglas(entrada);
                break;
            case RANKING:
                procesarVolverRanking(entrada);
                break;
            case INGRESO_NOMBRE_JUGADOR:
                estado = EstadoVistaConsola.ESPERANDO_JUGADORES;
                controlador.iniciarJugador(entrada);
                setTitle("Juego 10mil - En curso. Vista de " + controlador.nombreJugadorVentana());
                break;
            case LANZAR_DADOS:
                if (entrada.equals("1")) {
                    controlador.lanzar_dados();
                } else {
                    println("");
                    println("Opcion no valida. Por favor elija una opcion valida.");
                    println("1. LANZAR los dados");
                }
                break;
            case LANZAR_DADOS_Y_LIMPIAR:
                limpiarPantalla();
                procesarLanzamientoYLimpieza(entrada);
                break;
            case ESPERANDO_DECISION:
                procesarDecision(entrada);
                break;
            case VOLVER_AL_MP:
                mostrarMenuPrincipal();
            case ULTIMA_DECISION:
                procesarUltimaDecision(entrada);
                break;
        }
    }

    public void procesarDecision(String entrada) throws RemoteException { //se ejecuta cuando el jugador elige una opcion de plantarse, apartar o lanzar dados
        switch(entrada){
            case "1":
                controlador.apartarDados();
                break;
            case "2":
                controlador.plantarse();
                break;
            default: //puso algo invalido
                println("");
                println("Opcion no valida. Por favor elija una opcion valida.");
                habilitarBotonesPlantarseOApartar();
                break;
        }
    }

    private void procesarUltimaDecision(String entrada) throws RemoteException { //se ejecuta cuando se termino la partida, para saber que opcion quiere:
        switch(entrada){
            case "1": //presiono. volver a jugar
                limpiarTablaPuntaje();
                controlador.volverAJugar();
                break;
            case "2": //presiono. salir
                dispose();
                break;
            default: //puse algo invalido
                println("");
                println("Opcion no valida. Por favor elija una opcion valida.");
                mostrarMenuPrincipal();
                break;
        }
    }

    public void procesarLanzamientoYLimpieza(String entrada) throws RemoteException {
        switch (entrada){
            case "1":
                controlador.lanzar_dados();
                break;
            default:
                println("");
                println("Opcion no valida. Por favor elija una opcion valida.");
                println("1. LANZAR los dados");
                break;
        }
    }

    // TURNO, DADOS Y BOTONES=================
    @Override
    public void mostrarMisDados(ArrayList<Integer> valores){ //imprime los dados lanzados si soy el jugador actual
        println("");
        println("Tus dados lanzados son: " + valores);
    }

    @Override
    public void mostrarDadosOtros(ArrayList<Integer> valores){ //imprime los dados lanzados si es otro el jugador actual:
        println("");
        println("Dados lanzados por: " + valores);
    }
    @Override
    public void mostrarDadosApartados(ArrayList<Integer> d){
        println("");
        println("Dados apartados: " + d);
    }

    @Override
    public void habilitarBotonesPlantarseOApartar() {
        estado = EstadoVistaConsola.ESPERANDO_DECISION;
        println("");
        println("Tus dados tienen puntos!");
        println("Que deseas hacer? Elija una opcion: ");
        println("1. Apartar dados con puntos");
        println("2. Plantarse");
    }

    @Override
    public void chequear_botones_y_turno(String nombre, boolean es_mi_turno){ //se llama cuando cambia el turno para saber que botones habilitar segun si es juegador actual o no, ademas se muestran mensajes del turno
        if(es_mi_turno){
            txtEntradaJugador.setEditable(true);
            println("");
            println("Es tu turno!");
            println("1. LANZAR los dados");
            estado = EstadoVistaConsola.LANZAR_DADOS_Y_LIMPIAR;
        }else{
            println("");
            println("Esperando que " + nombre + " juegue...");
            txtEntradaJugador.setEditable(false);
        }

    }

    @Override
    public void solo_chequear_botones(boolean esMiTurno){ //se usa dsp de apartar cuando el jugador es el mismo y el turno no se actualizo, solo habilita y deshabilita
        if(esMiTurno) {
            println("Es tu turno!");
            println("Presione 1 para volver a lanzar!");
            estado = EstadoVistaConsola.LANZAR_DADOS;
        }
    }

    // MENSAJES=================
    @Override
    public void mensajeDadosSinPuntos(String nombre) throws RemoteException {
        println("¡Dados sin puntos! En esta ronda " + nombre + " no suma puntos");
        controlador.confirmar_mensaje();
        controlador.respuesta_puntaje();
    }

    @Override
    public void mensajeEscalera(String nombre) throws RemoteException {
        println(nombre + " obtuvo escalera! +500 puntos. Turno finalizado");
        controlador.confirmar_mensaje();
        controlador.respuesta_puntaje();
    }
    @Override
    public void mensajeSePlanto(String nombre, int puntos) throws RemoteException {
        println("");
        println(nombre + " se plantó, suma " + puntos + " puntos.");
        controlador.confirmar_mensaje();
        controlador.respuesta_puntaje();
    }

    @Override
    public void mensajeMaxApartado(String nombre, int punto) throws RemoteException {
        println("");
        println(nombre + " apartó la cantidad maxima de dados. Sus puntos en esta ronda son: " + punto);
        controlador.confirmar_mensaje();
        controlador.respuesta_puntaje();
    }

    @Override
    public void msjJugadorFuera() throws RemoteException {
        limpiarPantalla();
        println("Usted no pudo ser agregado porque la partida ya está en curso! Intentelo más tarde.");
        print("(Presione cualquier tecla para volver al menu principal)");
        estado = EstadoVistaConsola.VOLVER_AL_MP;
    }

    @Override
    public void mostrarGanador(String nombre, int puntos){
        txtEntradaJugador.setEditable(true);
        println(" ¡JUGADOR GANADOR! " + nombre + " llegó/pasó el puntaje ganador(10.000). \n La puntuacion obtenida fueron: " + puntos + " .Juego finalizado.");
        println("");
        println("¿Qué desea hacer ahora?");
        println("1. Volver a jugar");
        println("2. Salir");
        estado= EstadoVistaConsola.ULTIMA_DECISION;
    }

    // TABLA DE PUNTAJES=================
    @Override
    public void agregarPuntajeRondaTabla(int puntaje, String nombre, int puntajeTotal, int ronda){ //SIMULO TABLA DE PUNTAJES
        String[] fila = new String[4];
        fila[0] = String.valueOf(ronda);
        fila[1] = nombre;
        fila[2] = String.valueOf(puntaje);
        fila[3] = String.valueOf(puntajeTotal);
        tablaPuntajes.add(fila);
    }

    @Override
    public void limpiarTablaPuntaje(){
        tablaPuntajes.clear();
    }

    @Override
    public void mostrarTablaPuntaje() throws RemoteException {
        println("");
        println("-----------------------------------------------------------------------------------------------------------------");
        println(" --- TABLA DE PUNTAJES ---");
        println("-----------------------------------------------------------------------------------------------------------------");
        String encabezado = String.format("%-10s | %-20s | %-15s | %-15s%n", "NRO RONDA", "NOMBRE JUGADOR" , "PUNTAJE RONDA", "PUNTAJE TOTAL");
        println(encabezado);
        for(String[] fila : tablaPuntajes){
            String linea = String.format("        %-10s        |        %-20s        |        %-15s        |        %-15s%n        " ,fila[0] ,fila[1] ,fila[2] , fila[3]);
            println(linea);
        }
        println("-----------------------------------------------------------------------------------------------------------------");
        println("");
    }

    // RANKING=================
    public void mostrarRanking() throws RemoteException {
        limpiarPantalla();
        estado = EstadoVistaConsola.RANKING;
        println("RANKING");
        println("----------------------------------------------");
        println(String.format("%-15s %-15s %-15s", "NOMBRE", "PUNTAJE", "FECHA"));
        println("----------------------------------------------");
        Object[][] tabla = controlador.getTablaRanking();

        //ORDENA LA TABLA DE MAYOR A MENOR ANTES DE MOSTRARLA:
        Arrays.sort(tabla, (a, b) -> {
            int puntajeA = Integer.parseInt(a[1].toString());
            int puntajeB = Integer.parseInt(b[1].toString());
            return Integer.compare(puntajeB, puntajeA);
        });

        for (Object[] fila : tabla) {
            println(String.format("%-15s %-15s %-15s",
                    fila[0],  // nombre
                    fila[1],  // puntaje
                    fila[2]   // fecha
            ));
        }

        println("----------------------------------------------");
        println("");
        println("1. Volver");
    }

    private void procesarVolverRanking(String entrada) throws RemoteException {
        switch (entrada){
            case "1":
                mostrarMenuPrincipal();
                break;
            default: //puso algo invalido
                println("");
                println("Opcion no valida. Por favor elija una opcion valida.");
                mostrarRanking();
                break;
        }
    }

    // REGLAS=================
    private void procesarVolverReglas(String entrada){
        switch (entrada){
            case "1":
                mostrarMenuPrincipal();
                break;
            default: //puso algo invalido
                println("");
                println("Opcion no valida. Por favor elija una opcion valida.");
                mostrarReglas();
                break;
        }
    }

    @Override
    public void mostrarReglas(){
        limpiarPantalla();
        estado = EstadoVistaConsola.REGLAS;
        limpiarPantalla();
        println("REGLAS y COMO JUGAR - Juego 10mil - Dados");
        println("------------------------------------------------------");
        println("El juego de dados 10Mil se juega con 5 dados comunes de seis caras.");
        println("Pueden participar entre 2 y 6 jugadores, que se turnan para lanzar los dados e intentar sumar puntos hasta alcanzar los 10.000.");
        println("");
        println("------------------------------------------------------");
        println("Objetivo del Juego");
        println("------------------------------------------------------");
        println("Para sumar puntos cada jugador/a debera obtener puntos a traves de diferentes combinaciones en su turno.");
        println("");
        println("----------------------------------------------------------");
        println("Cómo sumar puntos?:");
        println("----------------------------------------------------------");
        println("Para sumar puntos deberas obtener en cada lanzamientos dados o combinaciones que te ayuden a sumar puntos.");
        println("Los dados con el numero 1 suman cada uno 100 puntos y los dados con el numero 5 suman 50 puntos cada uno.");
        println("Si obtenes tres dados iguales del mismo valor, se suma como una centena.");
        println("Por ejemplo, tres dados iguales con el numero cuatro suma 400 puntos y tres iguales con el numero dos suman 200 puntos.");
        println("La unica excepción es el numero uno porque al obtener tres dados iguales se suman 1000 puntos.");
        println("En caso de conseguir una escalera el jugador sumara 500 puntos.");
        println("");
        println("----------------------------------------------------------");
        println("TABLA DE PUNTOS:");
        println("----------------------------------------------------------");
        println("- 50 puntos con cada numero 5");
        println("- 100 puntos con cada numero 1");
        println("- 200 puntos con tres numeros 2");
        println("- 300 puntos con tres numeros 3");
        println("- 400 puntos con tres numeros 4");
        println("- 500 puntos con tres numeros 5");
        println("- 500 puntos con escalera (1,2,3,4,5 o 2,3,4,5,6 o 3,4,5,6,1)");
        println("- 1000 puntos con tres numeros 1");
        println("");
        println("--------------------------------------------------------");
        println("Dinamica del Juego");
        println("--------------------------------------------------------");
        println("Un jugador lanza los dados. En cada turno puede decide si se planta y conserva los puntos acumulados");
        println("o si arriesga un nuevo lanzamiento con los dados restantes.");
        println("Si en un lanzamiento obtiene puntos, estos se suman al total acumulado en esa ronda.");
        println("Si el jugador decide apartar uno o más dados que le otorguen puntaje, únicamente podrá volver a lanzar");
        println("los dados que no hayan sido apartados.");
        println("Si en un lanzamiento no aparece ningún 1, ningún 5 ni una combinación válida, pierde el turno");
        println("y también los puntos obtenidos en esa ronda.");
        println("La estrategia consiste en elegir entre asegurar lo ganado o seguir arriesgando.");
        println("En ocasiones, un buen lanzamiento puede disparar el puntaje (como obtener tres 1 y sumar 1000 puntos),");
        println("pero siempre con el riesgo de perderlo todo si no salen ni un 1 ni un 5 o combinaciones.");
        println("");
        println("--------------------------------------------------------");
        println("¿Quién gana?");
        println("--------------------------------------------------------");
        println("Gana el jugador/a que llegue primero a 10mil puntos!");
        println("");
        println("1.VOLVER");
    }

    // TIMER================= para arrancar a jugar
    public void iniciarTimer() { /// timer interno: no se ve por los jugadores.
        timer = new Timer(30000, e -> { //2000 = 2 segundos
            try {
                println("Hay suficientes jugadores! El juego va a comenzar.");
                controlador.comenzarJuego();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        timer.setRepeats(false);
    }

    //metodos de la vista grafica sin implementar aca
    public void mostrarLobby() throws RemoteException {
        ArrayList<Jugador> jugadores = controlador.jugadoresLobby();
        limpiarPantalla();
        txtEntradaJugador.setEditable(false);
        println("Jugadores en lobby:");
        for(Jugador j: jugadores){
            println("ID: " + j.getNroJugador() + " - Nombre: " + j.getNombreJugador() );
        }
    }

    public void msjNombreRepetido() throws RemoteException{
        estado = EstadoVistaConsola.INGRESO_NOMBRE_JUGADOR;
        println("");
        println("¡Lo siento! El nombre que ingresó ya está en uso! Intente con otro.");
    }


}
