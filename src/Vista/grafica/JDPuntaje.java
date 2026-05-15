package Vista.grafica;

import Controlador.Controlador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class JDPuntaje extends JDialog{
    private DefaultTableModel modeloTabla;
    private JTable tabla_puntaje;
    private JScrollPane scroll;
    private JPanel panelPrincipal;
    private JPanel panelBoton;
    private Controlador controlador;
    private JButton btnOk;
    private Timer timer;


    public JDPuntaje(JFrame vistaPadre, Controlador controlador){
        super(vistaPadre, false); //llamo al constructor de JDialog con los parametros: (ventana que lo contiene, modal=true/false)
        inicializar_comp(controlador);
        
    }

    private void inicializar_comp(Controlador controlador) {
        this.controlador = controlador;
        setTitle("Juego10Mil - Puntajes");
        setResizable(false);
        setBounds(100, 100, 500, 500);//posicion x (horizontal)=100, posicion y (vertical)=100, ancho=247 , largo=109
        setLocationRelativeTo(null);

        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("JUGADOR");
        modeloTabla.addColumn("RONDA NRO°");
        modeloTabla.addColumn("PUNTAJE RONDA");
        modeloTabla.addColumn("PUNTAJE TOTAL");

        panelPrincipal = new JPanel(new BorderLayout());

        tabla_puntaje = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) { //que no se pueda editar la tabla de puntajes/q sea solo lectura
                return false;
            }
        };
        tabla_puntaje.setModel(modeloTabla);

        panelBoton = new JPanel();
        btnOk = new JButton(" Ok ");
        panelBoton.add(btnOk);

        scroll = new JScrollPane(tabla_puntaje);
        panelPrincipal.add(scroll, BorderLayout.CENTER);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);
        setContentPane(panelPrincipal);

        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                try {
                    if(timer != null){
                        timer.stop();
                    }
                    controlador.respuesta_puntaje();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public void agregarPuntaje(int puntaje, String nombre, int puntajeT, int ronda){
        modeloTabla.addRow(new Object[]{
                nombre,ronda,puntaje,puntajeT
        });
    }

    public void mostrarTabla(){
        setLocationRelativeTo(null);
        setVisible(true);
        if (timer != null) timer.stop();
        timer = new Timer(5000, e -> {
            setVisible(false);
            try {
                controlador.respuesta_puntaje();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        timer.setRepeats(false);
        timer.start();
        btnOk.setVisible(true);
    }


    public void clear(){
        modeloTabla.setRowCount(0);
    }

}