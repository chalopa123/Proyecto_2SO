/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.gui;

/**
 * @author adrian
 * @author gonzalo
 */

import com.mycompany.mavenproject1.core.FileSystemManager;
import com.mycompany.mavenproject1.core.Simulador;
import com.mycompany.mavenproject1.modelo.Archivo;
import com.mycompany.mavenproject1.modelo.Directorio;
import com.mycompany.mavenproject1.modelo.FileSystemNode;
import com.mycompany.mavenproject1.modelo.Disco;
import com.mycompany.mavenproject1.procesos.PoliticaPlanificacion;
import com.mycompany.mavenproject1.procesos.Proceso;
import com.mycompany.mavenproject1.procesos.PlanificadorDisco;
import com.mycompany.mavenproject1.procesos.OperacionCRUD;
import com.mycompany.mavenproject1.persistencia.GestorPersistencia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainFrame extends JFrame {

    private JTree treeArchivos;
    private JTable tableAsignacion;
    private JTable tableProcesos;
    private JPanel panelDisco;
    private JComboBox<PoliticaPlanificacion> comboPolitica;
    private JTextField textPath;
    private JTextField textSize;
    private JButton btnCrearArchivo, btnCrearDir, btnEliminar, btnGuardar, btnCargar;
    private JRadioButton radioAdmin, radioUsuario;
    
    private DefaultTableModel modeloTablaArchivos;
    private DefaultTableModel modeloTablaProcesos;
    
    private final Simulador simulador;
    private final FileSystemManager fsManager;
    private final GestorPersistencia gestorPersistencia;

    public MainFrame() {
        this.simulador = Simulador.getInstance();
        this.fsManager = FileSystemManager.getInstance();
        this.gestorPersistencia = new GestorPersistencia();
        this.simulador.setGui(this);

        initComponents(); 
        configurarEventos();
        
        iniciarTimerSimulacion();
        recargarTodo();
    }

    private void initComponents() {
        setTitle("Simulador SO - FileSystem");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        ButtonGroup groupMode = new ButtonGroup();
        radioAdmin = new JRadioButton("Administrador", true);
        radioUsuario = new JRadioButton("Usuario");
        groupMode.add(radioAdmin);
        groupMode.add(radioUsuario);
        
        panelTop.add(new JLabel("Modo:"));
        panelTop.add(radioAdmin);
        panelTop.add(radioUsuario);
        
        panelTop.add(new JSeparator(SwingConstants.VERTICAL));
        
        textPath = new JTextField("/home/archivo.txt", 15);
        textSize = new JTextField("5", 3);
        btnCrearArchivo = new JButton("Crear Archivo");
        btnCrearDir = new JButton("Crear Dir");
        btnEliminar = new JButton("Eliminar Sel.");
        
        panelTop.add(new JLabel("Path:"));
        panelTop.add(textPath);
        panelTop.add(new JLabel("Tamaño (Bloques):"));
        panelTop.add(textSize);
        panelTop.add(btnCrearArchivo);
        panelTop.add(btnCrearDir);
        panelTop.add(btnEliminar);
        
        comboPolitica = new JComboBox<>(PoliticaPlanificacion.values());
        panelTop.add(new JLabel("Planificador:"));
        panelTop.add(comboPolitica);

        btnGuardar = new JButton("Guardar");
        btnCargar = new JButton("Cargar");
        panelTop.add(btnGuardar);
        panelTop.add(btnCargar);

        add(panelTop, BorderLayout.NORTH);

        JSplitPane splitCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        treeArchivos = new JTree();
        JScrollPane scrollTree = new JScrollPane(treeArchivos);
        scrollTree.setPreferredSize(new Dimension(250, 0));
        scrollTree.setBorder(BorderFactory.createTitledBorder("Estructura de Directorios"));
        splitCentral.setLeftComponent(scrollTree);
        
        JTabbedPane tabs = new JTabbedPane();
        
        panelDisco = new DiskPanel(fsManager.getDisco(), simulador.getPlanificador()); 
        JScrollPane scrollDisco = new JScrollPane(panelDisco);
        tabs.addTab("Visualización Disco", scrollDisco);
        
        modeloTablaArchivos = new DefaultTableModel(new String[]{"Archivo", "Bloques", "Inicio", "PID"}, 0);
        tableAsignacion = new JTable(modeloTablaArchivos);
        tabs.addTab("Tabla de Asignación", new JScrollPane(tableAsignacion));
        
        modeloTablaProcesos = new DefaultTableModel(new String[]{"PID", "Estado", "Operación", "Path"}, 0);
        tableProcesos = new JTable(modeloTablaProcesos);
        tabs.addTab("Cola de Procesos", new JScrollPane(tableProcesos));
        
        splitCentral.setRightComponent(tabs);
        add(splitCentral, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        btnCrearArchivo.addActionListener(e -> {
            try {
                String path = textPath.getText();
                int size = Integer.parseInt(textSize.getText());
                simulador.crearProcesoSimulado(OperacionCRUD.CREAR, path, size);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tamaño debe ser un número entero.");
            }
        });

        btnCrearDir.addActionListener(e -> {
             String path = textPath.getText();
             simulador.crearProcesoSimulado(OperacionCRUD.CREAR_DIR, path, 0); 
        });

        btnEliminar.addActionListener(e -> {
            // Obtener el nodo seleccionado visualmente
            javax.swing.tree.DefaultMutableTreeNode node = 
                (javax.swing.tree.DefaultMutableTreeNode) treeArchivos.getLastSelectedPathComponent();

            if (node == null) {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona un elemento del árbol.");
                return;
            }

            // Construir el String del path (ej: "/home/archivo.txt")
            // Obtenemos el "camino" desde la raíz hasta el nodo seleccionado
            javax.swing.tree.TreePath selectionPath = treeArchivos.getSelectionPath();
            Object[] pathParts = selectionPath.getPath();

            StringBuilder fullPath = new StringBuilder();

            for (Object part : pathParts) {
                String nombreNodo = part.toString();

                // Lógica para armar el path correctamente:
                // Si el nodo es la raíz "/" no le ponemos slash antes.
                // Si es otro nodo, le ponemos "/" antes.
                if (!nombreNodo.equals("/")) {
                    fullPath.append("/").append(nombreNodo);
                }
            }

            // Si el string quedó vacío es porque seleccionaron la raíz "/"
            if (fullPath.length() == 0) {
                fullPath.append("/");
            }

            String pathFinal = fullPath.toString();

            // Validar que no intenten borrar la raíz
            if (pathFinal.equals("/")) {
                JOptionPane.showMessageDialog(this, "No puedes eliminar el directorio raíz.");
                return;
            } 
            
            // Enviar la solicitud al simulador con el path correcto
             simulador.crearProcesoSimulado(OperacionCRUD.ELIMINAR, textPath.getText(), 0);
        });
        
        comboPolitica.addActionListener(e -> {
            simulador.getPlanificador().setPolitica((PoliticaPlanificacion) comboPolitica.getSelectedItem());
        });

        ActionListener modeListener = e -> {
            boolean isAdmin = radioAdmin.isSelected();
            btnCrearArchivo.setEnabled(isAdmin);
            btnCrearDir.setEnabled(isAdmin);
            btnEliminar.setEnabled(isAdmin);
        };
        radioAdmin.addActionListener(modeListener);
        radioUsuario.addActionListener(modeListener);
        
        btnGuardar.addActionListener(e -> {
            try {
                gestorPersistencia.guardarEstado(fsManager, "filesystem.json");
                JOptionPane.showMessageDialog(this, "Guardado correctamente.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        });
        
        btnCargar.addActionListener(e -> {
            try {
                gestorPersistencia.cargarEstado("filesystem.json");
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Cargado correctamente.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage());
            }
        });
    }

    public void iniciarTimerSimulacion() {
        new Timer(1000, e -> simulador.tickSimulacion()).start();
    }

    public void recargarTodo() {
        recargarTablaAsignacion();
        recargarTablaProcesos();
        if (panelDisco != null) panelDisco.repaint();
    }

    public void recargarArbol() {
        Directorio rootModelo = fsManager.getRoot();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootModelo.getNombre());
        construirNodos(rootNode, rootModelo);
        treeArchivos.setModel(new DefaultTreeModel(rootNode));
        expandirArbol();
    }
    
    public void expandirArbol() {
        for (int i = 0; i < treeArchivos.getRowCount(); i++) {
            treeArchivos.expandRow(i);
        }
    }

    private void construirNodos(DefaultMutableTreeNode nodoSwing, Directorio dirModelo) {
        for (FileSystemNode hijo : dirModelo.getHijos()) {
            DefaultMutableTreeNode hijoNode = new DefaultMutableTreeNode(hijo.getNombre());
            nodoSwing.add(hijoNode);
            if (hijo instanceof Directorio) {
                construirNodos(hijoNode, (Directorio) hijo);
            }
        }
    }

    public void recargarTablaAsignacion() {
        modeloTablaArchivos.setRowCount(0);
        recorrerYAnadirArchivos(fsManager.getRoot());
    }
    
    private void recorrerYAnadirArchivos(Directorio dir) {
        for (FileSystemNode hijo : dir.getHijos()) {
            if (hijo instanceof Archivo) {
                Archivo a = (Archivo) hijo;
                modeloTablaArchivos.addRow(new Object[]{
                    a.getNombre(), a.getTamanoEnBloques(), a.getPrimerBloque(), a.getProcesoCreador().PID
                });
            } else if (hijo instanceof Directorio) {
                recorrerYAnadirArchivos((Directorio) hijo);
            }
        }
    }

    public void recargarTablaProcesos() {
        modeloTablaProcesos.setRowCount(0);
        if (simulador.getColaListos().listaInterna != null) {
             for (Proceso p : simulador.getColaListos().listaInterna) {
                 modeloTablaProcesos.addRow(new Object[]{p.PID, p.estado, p.operacionAsociada, p.path});
             }
        }
        if (simulador.getColaBloqueados().listaInterna != null) {
             for (Proceso p : simulador.getColaBloqueados().listaInterna) {
                 modeloTablaProcesos.addRow(new Object[]{p.PID, p.estado, p.operacionAsociada, p.path});
             }
        }
    }
    
    class DiskPanel extends JPanel {
        private final Disco disco;
        private final PlanificadorDisco planificador;
        private final int BLOCK_SIZE = 20;
        private final int BLOCKS_PER_ROW = 20;

        public DiskPanel(Disco disco, PlanificadorDisco planificador) {
            this.disco = disco;
            this.planificador = planificador;
            int rows = (int) Math.ceil((double) disco.totalBloques / BLOCKS_PER_ROW);
            setPreferredSize(new Dimension(BLOCK_SIZE * BLOCKS_PER_ROW + 10, rows * BLOCK_SIZE + 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.setColor(Color.BLACK);
            g.drawString("Posición Cabezal: " + planificador.getPosicionCabeza(), 5, getHeight() - 5);

            for (int i = 0; i < disco.totalBloques; i++) {
                int row = i / BLOCKS_PER_ROW;
                int col = i % BLOCKS_PER_ROW;
                int x = col * BLOCK_SIZE;
                int y = row * BLOCK_SIZE;

                if (disco.bloques[i].estaLibre) {
                    g.setColor(Color.GREEN);
                } else {
                    int pid = disco.bloques[i].pidProceso;
                    g.setColor(new Color((pid * 50) % 255, (pid * 80) % 255, (pid * 110) % 255));
                }

                g.fillRect(x, y, BLOCK_SIZE - 2, BLOCK_SIZE - 2);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, BLOCK_SIZE - 2, BLOCK_SIZE - 2);

                if (i == planificador.getPosicionCabeza()) {
                    g.setColor(Color.RED);
                    g.drawRect(x, y, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                    g.drawRect(x + 1, y + 1, BLOCK_SIZE - 3, BLOCK_SIZE - 3);
                }
            }
        }
    }
}