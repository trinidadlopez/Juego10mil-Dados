package Vista.grafica;

import Controlador.Controlador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class VGanador extends JFrame {
    private JPanel panel;
    private JPanel panelBotones;
    private JButton btnSalir;
    private JButton btnVolverAJugar;
    private JLabel mensaje;
    private Controlador controlador;

    public VGanador(Controlador controlador,String nombreGanador, int puntosGanador) {
        this.controlador=controlador;

        setTitle("Fin de la partida");
        setResizable(false); //puede no estar. Es para que no se pueda redimensionar

        //panel principal
        panel = new JPanel(new BorderLayout());

        //mensaje
        mensaje =new JLabel("<html><center>" +
                "|PARTIDA TERMINADA| <br><br>" +
                "Ganador/a de la partida: " + nombreGanador + "<br>" +
                "Con una puntuación de: " + puntosGanador +
                "</center></html>"
        );
        panel.add(mensaje, BorderLayout.CENTER);
        mensaje.setHorizontalAlignment(SwingConstants.CENTER);

        //botones
        panelBotones = new JPanel(new GridLayout(1, 2, 10, 0));
        btnVolverAJugar = new JButton("Volver a jugar");
        btnSalir = new JButton("Salir");
        panelBotones.add(btnVolverAJugar);
        panelBotones.add(btnSalir);

        panel.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setMinimumSize(new Dimension(350, 180));
        setLocationRelativeTo(null); // centra la ventana en la pantalla (si todavía no está visible)

        btnVolverAJugar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setVisible(false);
                    controlador.volverAJugar();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    controlador.terminarJuego();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


    }


}
