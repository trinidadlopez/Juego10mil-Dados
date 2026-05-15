package Vista.grafica;

import Controlador.Controlador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class VJuego extends JFrame {
    private Controlador controlador;
    private VistaGrafica vista;
    private JLabel fondo;
    private JLabel cubilete;
    private JButton btnLanzar;
    private JButton btnApartar;
    private JButton btnPlantarse;
    private ArrayList<JLabel> dadosMios;
    private ArrayList<JLabel> dadosOtros;
    private ArrayList<JLabel> dadosApartados;
    private JPanel panelMisDados;
    private JPanel panelDadosOtros;
    private JPanel panelDadosApartados;
    private JDMensajes mensajes;
    private JDPuntaje tabla_puntaje;

    public VJuego(Controlador controlador, VistaGrafica vista) {
        inicializar_comp(controlador, vista);
    }

    private void inicializar_comp(Controlador controlador, VistaGrafica vista) {
        this.controlador = controlador;
        this.vista=vista;

        mensajes = new JDMensajes(this,controlador, vista);
        tabla_puntaje = new JDPuntaje(this,controlador);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700); //le da tamaño a la ventana y la centra
        setLocationRelativeTo(null);
        setResizable(false); //puede no estar. Es para que no se pueda redimensionar

        // fondo
        ImageIcon fondoJ = new ImageIcon("src/ImagenesJuego/fondo.png");
        this.fondo = new JLabel(fondoJ);
        fondo.setLayout(new BorderLayout());
        setContentPane(fondo);

        // cubilete escalado
        ImageIcon iconoOriginal = new ImageIcon("src/ImagenesJuego/cubilete.png");
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(
                150, 200, Image.SCALE_SMOOTH
        );
        ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);
        cubilete = new JLabel(iconoEscalado);
        cubilete.setAlignmentX(Component.CENTER_ALIGNMENT);

        // botones
        btnLanzar = new JButton("Lanzar dados");
        btnApartar = new JButton("Apartar dados con puntos");
        btnPlantarse = new JButton("Plantarse");

        //PANEL INFERIOR DEL FONDO
        JPanel panelInferiorSouth = new JPanel(new BorderLayout());
        panelInferiorSouth.setOpaque(false);
        panelInferiorSouth.add(cubilete, BorderLayout.NORTH);

        panelInferiorSouth.add(btnLanzar, BorderLayout.CENTER);
        panelInferiorSouth.add(btnApartar, BorderLayout.WEST);
        panelInferiorSouth.add(btnPlantarse, BorderLayout.EAST);

        fondo.add(panelInferiorSouth, BorderLayout.SOUTH);
        btnApartar.setEnabled(false);
        btnPlantarse.setEnabled(false);
        btnLanzar.setEnabled(false);

        //PARA DARLE MAS PROTAGONISMO A LOS BOTONES
        btnLanzar.setPreferredSize(new Dimension(160, 45));
        btnApartar.setPreferredSize(new Dimension(185, 45));
        btnPlantarse.setPreferredSize(new Dimension(160, 45));

        //PANEL DADOS APARTADOS
        panelDadosApartados = new JPanel();
        panelDadosApartados.setLayout(new BoxLayout(panelDadosApartados, BoxLayout.Y_AXIS)); //esto es para que pueda aparecer en vertical
        panelDadosApartados.setOpaque(false);

        fondo.add(panelDadosApartados, BorderLayout.EAST);
        panelDadosApartados.setVisible(false);
        dadosApartados = new ArrayList<>();

        // PANEL DADOS OTROS
        panelDadosOtros = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panelDadosOtros.setOpaque(false);
        fondo.add(panelDadosOtros, BorderLayout.NORTH);
        panelDadosOtros.setVisible(false);
        dadosOtros = new ArrayList<>();

        // PANEL DADOS MIOS
        panelMisDados = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panelMisDados.setOpaque(false);
        //lanzamiento (labels de dados vacíos)
        dadosMios = new ArrayList<>();

        fondo.add(panelMisDados, BorderLayout.CENTER);
        panelMisDados.setVisible(false);


        btnPlantarse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    controlador.plantarse();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // acción del botón lanzar
        btnLanzar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    controlador.lanzar_dados();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // acción del botón apartar
        btnApartar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    controlador.apartarDados();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }

    // mostrar
    public void mostrar_mis_dados(ArrayList<Integer> valores){
        panelMisDados.setVisible(false);
        actualizarListaDados(valores, panelMisDados, 100, 100, dadosMios);
        panelMisDados.setVisible(true);
    }

    public void mostrar_dados_otros(ArrayList<Integer> valores){
        panelDadosOtros.setVisible(false);
        actualizarListaDados(valores, panelDadosOtros, 50, 50, dadosOtros);
        panelDadosOtros.setVisible(true);
    }

    public void mostrarTabla(){
        tabla_puntaje.mostrarTabla();
    }

    //botones
    public void deshabilitarBotonesTodos(){
        btnLanzar.setEnabled(false);
        btnPlantarse.setEnabled(false);
        btnApartar.setEnabled(false);
    }

    public void habilitarBotonLanzar(){
        btnLanzar.setEnabled(true);
        btnPlantarse.setEnabled(false);
        btnApartar.setEnabled(false);
    }

    public void habilitarBotonesPlantarseYApartar() {
        btnPlantarse.setEnabled(true);
        btnApartar.setEnabled(true);
        btnLanzar.setEnabled(false);
    }

    // mensajes
    public void turno(String nombre){
        mensajes.msjTurno(nombre);
    }

    public void msjPlantado(String nombre, int puntos){
        mensajes.mostrarPlantado(nombre, puntos);
    }

    public void msjEscalera(String nombre){
        mensajes.escalera(nombre);
    }

    public void msjDadosSinPuntos(String nombre){
        mensajes.dadosSinPuntos(nombre);
    }

    public void msjMaxApartado(String nombre,int punto) throws RemoteException {
        deshabilitarBotonesTodos();
        mensajes.maxApartado(nombre, punto);
    }

    public void jugador_fuera(){
        mensajes.jugadorFuera();
    }

    public void nombre_repetido(){
        mensajes.nombreRepetido();
    }
    // actualizar
    public void actualizarListaDados(ArrayList<Integer> valores, JPanel panel, int ancho, int alto, ArrayList<JLabel> lista){
        panel.removeAll();
        for (int i =0 ; i < valores.size() ; i++) {
            int valor = valores.get(i);
            ImageIcon iconoOriginal = new ImageIcon(
                    getClass().getResource("/ImagenesJuego/cara" + valor + ".png" )
            );
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(
                    ancho, alto, Image.SCALE_SMOOTH
            );
            JLabel dado = new JLabel(new ImageIcon(imagenEscalada));
            lista.add(dado);
            panel.add(dado);
        }
        panel.revalidate();
        panel.repaint();
    }

    public void actualizarTablaPuntaje (int puntaje, String nombre, int puntajeTotal, int ronda){
        tabla_puntaje.agregarPuntaje(puntaje, nombre, puntajeTotal, ronda);
    }

    public void dados_apartados(ArrayList<Integer> dadosApart) {
        panelDadosApartados.removeAll();
        panelDadosOtros.setVisible(false);
        panelMisDados.setVisible(false);

        actualizarListaDados(dadosApart, panelDadosApartados, 50, 50, dadosApartados);

        panelDadosApartados.setVisible(true);
        panelDadosApartados.revalidate();
        panelDadosApartados.repaint();
    }

    // limpiar dados
    public void limpiar_dados_mesa(){
        dadosOtros.clear();
        dadosMios.clear();
        dadosApartados.clear();

        panelMisDados.removeAll();
        panelDadosOtros.removeAll();
        panelDadosApartados.removeAll();

        panelMisDados.revalidate();
        panelDadosOtros.revalidate();
        panelDadosApartados.revalidate();

        panelMisDados.repaint();
        panelDadosOtros.repaint();
        panelDadosApartados.repaint();
    }

    public void limpiar_tabla_puntaje(){
        tabla_puntaje.clear();
    }
}
