package Vista.grafica;

import Controlador.Controlador;
import Modelo.Jugador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class VLobby extends JFrame {
    private Controlador controlador;
    private JPanel panel;
    private JPanel panelJugadores;
    private JLabel lblEstado;
    private JPanel panelEstado;
    Timer timer;


    public VLobby(Controlador controlador) {
        inicializar_comp(controlador);
        iniciarTimer();
    }

    private void inicializar_comp(Controlador controlador){
        this.controlador = controlador;

        setTitle("Juego 10mil - Lobby");
        setSize(400, 300); //100, 100, 350, 300        setLocationRelativeTo(null);         // Centra la ventana en la pantalla (si todavía no está visible)
        setLocationRelativeTo(null);         // Centra la ventana en la pantalla (si todavía no está visible)
        setResizable(false); //puede no estar. Es para que no se pueda redimensionar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //si cerras esta ventana, se termina la aplicacion
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
        panel.setLayout(new BorderLayout());

        //estado
        panelEstado = new JPanel();
        panelEstado.setLayout(new BoxLayout(panelEstado, BoxLayout.Y_AXIS));
        lblEstado =new JLabel("Esperando al menos 2 jugadores");
        panel.add(panelEstado, BorderLayout.NORTH);
        panelEstado.add(lblEstado);

        //jugadores
        panelJugadores = new JPanel();
        panelJugadores.setLayout(new BoxLayout(panelJugadores, BoxLayout.Y_AXIS));
        panelJugadores.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(panelJugadores, BorderLayout.CENTER);

    }

    public void actualizarJugadores(ArrayList<Jugador> jugadores) throws RemoteException {
        panelJugadores.removeAll();
        for (Jugador j : jugadores) {
            System.out.println("Lobby. actualizarJugadores(). jugador:" + j.getNombreJugador());
            JLabel lblJugador = new JLabel("Id: " + j.getNroJugador() + " - Nombre: " +j.getNombreJugador());
            panelJugadores.add(lblJugador);
        }
        panelJugadores.revalidate();
        panelJugadores.repaint();
        if (jugadores.size() < 2) {
            lblEstado.setText("Esperando al menos 2 jugadores (" + jugadores.size() + "/6)");
        } else {
            lblEstado.setText("Esperando a mas jugadores...\n Jugadores conectados: " + jugadores.size() + "/6.");
        }
        if(jugadores.size() == 6 && timer.isRunning()){
            timer.stop();
            controlador.comenzarJuego();
        }
    }

    public void iniciarTimer() { /// timer interno: no se ve por los jugadores.
        timer = new Timer(30000, e -> { //15000 =15 segundos
            try {
                controlador.comenzarJuego();
                setVisible(false);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        timer.setRepeats(false);
    }
}
