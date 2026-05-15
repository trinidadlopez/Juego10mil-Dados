package Vista.grafica;

import Controlador.Controlador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class JDMensajes extends JDialog{
    private JPanel panelPrincipal;
    private JLabel texto;
    private Controlador controlador;
    private VistaGrafica vista;
    private JButton btnOK;
    private Runnable accionOK; //para que el boton ok tenga diferentes salidas segun donde lo use
    private Timer timer;

    public JDMensajes(JFrame vistaPadre, Controlador controlador, VistaGrafica vista){
        super(vistaPadre, false); //llamo al constructor de JDialog con los parametros: (ventana que lo contiene, modal=true/false)
        inicializar(controlador, vista);
    }

    private void inicializar(Controlador controlador, VistaGrafica vista){
        this.controlador = controlador;
        this.vista = vista;

        setResizable(false); //puede no estar. Es para que no se pueda redimensionar
        setTitle("Aviso");

        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25)); //le agrega margen al texto dentro de la ventana (arriba, izq, abajo, der)

        texto = new JLabel("", SwingConstants.CENTER);
        panelPrincipal.add(texto, BorderLayout.CENTER);

        btnOK= new JButton(" Ok ");
        panelPrincipal.add(btnOK, BorderLayout.SOUTH);


        setContentPane(panelPrincipal);
        texto.setVisible(true);
        btnOK.setVisible(false);

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                if(timer != null){
                    timer.stop();
                }
                if(accionOK != null){
                    accionOK.run();
                }
            }
        });

        pack(); //la ventana se ajusta al tamño del textos
        setLocationRelativeTo(null); // Centra la ventana en la pantalla (si todavía no está visible)
        setVisible(false);

    }


    private void mostrar(){
        pack();
        setLocationRelativeTo(null);
        if (timer != null) timer.stop();
        timer = new Timer(5000, e -> {
            setVisible(false);
            if (accionOK != null) accionOK.run();
        });
        timer.setRepeats(false);
        timer.start();

        setVisible(true);
    }

    public void msjTurno(String nombre){
        texto.setText("Turno del jugador/a: " + nombre);
        texto.setVisible(true);
        panelPrincipal.setVisible(true);
        btnOK.setVisible(true);
        accionOK = () -> setVisible(false);
        mostrar();
    }

    public void jugadorFuera(){
        setSize(new Dimension(600, 150));
        texto.setText("¡Lo siento! Usted no pudo ser agregado porque la partida ya está en curso! Intentelo más tarde.");
        texto.setVisible(true);
        panelPrincipal.setVisible(true);
        btnOK.setVisible(true);
        accionOK = () -> vista.mostrarMenuPrincipal();
        mostrar();
    }

    public void nombreRepetido(){
        setSize(new Dimension(600, 150));
        texto.setText("¡Lo siento! El nombre que ingresó ya está en uso! Intente con otro");
        texto.setVisible(true);
        panelPrincipal.setVisible(true);
        btnOK.setVisible(true);
        accionOK = () -> vista.mostrarNombreJugador();
        mostrar();
    }

    public void maxApartado(String nombre, int puntos){
        texto.setText(nombre + " apartó la cantidad maxima de dados. Sus puntos en esta ronda son: " + puntos);
        texto.setVisible(true);
        panelPrincipal.setVisible(true);
        btnOK.setVisible(true);
        accionOK = () -> {
            try {
                controlador.confirmar_mensaje();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };
        mostrar();
    }

    public void mostrarPlantado(String nombre, int puntos){
        texto.setText(nombre + " se plantó, suma " + puntos + " puntos.");
        texto.setVisible(true);
        panelPrincipal.setVisible(true);
        btnOK.setVisible(true);
        accionOK = () -> {
            try {
                controlador.confirmar_mensaje();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };
        mostrar();
    }

    public void escalera(String nombre){
        texto.setText(nombre + " obtuvo escalera! +500 puntos. Turno finalizado");
        texto.setVisible(true);
        panelPrincipal.setVisible(true);
        btnOK.setVisible(true);
        accionOK = () -> {
            try {
                controlador.confirmar_mensaje();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };
        mostrar();
    }

    public void dadosSinPuntos(String nombre){
        texto.setText("¡Dados sin puntos! En esta ronda " + nombre + " no suma puntos");
        texto.setVisible(true);
        panelPrincipal.setVisible(true);
        btnOK.setVisible(true);
        accionOK = () -> {
            try {
                controlador.confirmar_mensaje();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };
        mostrar();
    }
}
