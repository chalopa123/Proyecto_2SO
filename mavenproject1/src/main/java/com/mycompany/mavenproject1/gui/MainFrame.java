/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.gui;

/**
 *
 * @author gonzalo
 */
import com.mycompany.mavenproject1.core.FileSystemManager;
import com.mycompany.mavenproject1.core.Simulador;
import com.mycompany.mavenproject1.modelo.*;
import com.mycompany.mavenproject1.procesos.*;
import com.mycompany.mavenproject1.persistencia.GestorPersistencia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;

public class MainFrame extends JFrame {

    // Componentes
    private JTree treeArchivos;
    private JTable tableAsignacion;
    private JTable tableProcesos;
    private JPanel panelDisco;
    private JComboBox<PoliticaPlanificacion> comboPolitica;
    private JTextField textPath;
    private JTextField textSize;
    private JButton btnCrearArchivo, btnCrearDir, btnEliminar, btnGuardar, btnCargar;
    private JRadioButton radioAdmin, radioUsuario;
    
    // Modelos
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
        setTitle("Simulador SO - FileSystem (Proyecto 2)");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Superior (Controles) ---
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Modos de Usuario 
        ButtonGroup groupMode = new ButtonGroup();
        radioAdmin = new JRadioButton("Administrador", true);
        radioUsuario = new JRadioButton("Usuario");
        groupMode.add(radioAdmin);
        groupMode.add(radioUsuario);
        
        panelTop.add(new JLabel("Modo:"));
        panelTop.add(radioAdmin);
        panelTop.add(radioUsuario);
        
        panelTop.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Inputs
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
        
        // Políticas
        comboPolitica = new JComboBox<>(PoliticaPlanificacion.values());
        panelTop.add(new JLabel("Planificador:"));
        panelTop.add(comboPolitica);

        // Persistencia [cite: 68]
        btnGuardar = new JButton("Guardar");
        btnCargar = new JButton("Cargar");
        panelTop.add(btnGuardar);
        panelTop.add(btnCargar);

        add(panelTop, BorderLayout.NORTH);

        // --- Panel Central (Split Pane) ---
        JSplitPane splitCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Izquierda: Árbol [cite: 25]
        treeArchivos = new JTree();
        JScrollPane scrollTree = new JScrollPane(treeArchivos);
        scrollTree.setPreferredSize(new Dimension(250, 0));
        scrollTree.setBorder(BorderFactory.createTitledBorder("Estructura de Directorios"));
        splitCentral.setLeftComponent(scrollTree);
        
        // Derecha: Tabs (Disco, Tablas)
        JTabbedPane tabs = new JTabbedPane();
        
        // Tab 1: Disco [cite: 31]
        panelDisco = new DiskPanel(fsManager.getDisco(), Simulador.getInstance().planificador); // Usar clase interna DiskPanel
        JScrollPane scrollDisco = new JScrollPane(panelDisco);
        tabs.addTab("Visualización Disco", scrollDisco);
        
        // Tab 2: Tabla Asignación [cite: 58]
        modeloTablaArchivos = new DefaultTableModel(new String[]{"Archivo", "Bloques", "Inicio", "PID"}, 0);
        tableAsignacion = new JTable(modeloTablaArchivos);
        tabs.addTab("Tabla de Asignación", new JScrollPane(tableAsignacion));
        
        // Tab 3: Procesos [cite: 21]
        modeloTablaProcesos = new DefaultTableModel(new String[]{"PID", "Estado", "Operación", "Path"}, 0);
        tableProcesos = new JTable(modeloTablaProcesos);
        tabs.addTab("Cola de Procesos", new JScrollPane(tableProcesos));
        
        splitCentral.setRightComponent(tabs);
        add(splitCentral, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        // Crear Archivo
        btnCrearArchivo.addActionListener(e -> {
            try {
                String path = textPath.getText();
                int size = Integer.parseInt(textSize.getText());
                simulador.crearProcesoSimulado(OperacionCRUD.CREAR, path, size);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tamaño debe ser un número entero.");
            }
        });

        // Crear Directorio
        btnCrearDir.addActionListener(e -> {
             // Simulamos creación de directorio como proceso size 0 (o implementación directa)
             // Para el simulador, usaremos CREAR con size 0 y path terminado en / o manejado internamente
             String path = textPath.getText();
             // Simplificación: Llamada directa o proceso especial. 
             // El PDF implica que todo pasa por procesos.
             simulador.crearProcesoSimulado(OperacionCRUD.CREAR, path, 0); 
        });

        // Eliminar
        btnEliminar.addActionListener(e -> {
             DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeArchivos.getLastSelectedPathComponent();
             if (node == null) return;
             // Reconstruir path (simplificado)
             String path = "/" + node.getUserObject().toString(); // Esto requiere mejor lógica de path completo
             simulador.crearProcesoSimulado(OperacionCRUD.ELIMINAR, textPath.getText(), 0);
        });
        
        // Cambio de Política 
        comboPolitica.addActionListener(e -> {
            Simulador.getInstance().planificador.setPolitica((PoliticaPlanificacion) comboPolitica.getSelectedItem());
        });

        // Lógica Admin/Usuario 
        ActionListener modeListener = e -> {
            boolean isAdmin = radioAdmin.isSelected();
            btnCrearArchivo.setEnabled(isAdmin);
            btnCrearDir.setEnabled(isAdmin);
            btnEliminar.setEnabled(isAdmin);
            // Usuario solo puede leer (botones deshabilitados)
        };
        radioAdmin.addActionListener(modeListener);
        radioUsuario.addActionListener(modeListener);
        
        // Persistencia
        btnGuardar.addActionListener(e -> {
            try {
                gestorPersistencia.guardarEstado(fsManager, "filesystem.json");
                JOptionPane.showMessageDialog(this, "Guardado correctamente.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void iniciarTimerSimulacion() {
        new Timer(1000, e -> simulador.tickSimulacion()).start();
    }

    public void recargarTodo() {
        recargarArbol();
        recargarTablaAsignacion();
        recargarTablaProcesos();
        panelDisco.repaint();
    }

    // Lógica del JTree
    public void recargarArbol() {
        Directorio rootModelo = fsManager.getRoot();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootModelo.getNombre());
        construirNodos(rootNode, rootModelo);
        treeArchivos.setModel(new DefaultTreeModel(rootNode));
    }

    private void construirNodos(DefaultMutableTreeNode nodoSwing, Directorio dirModelo) {
        // Como CustomLinkedList es Iterable (según tu código), usamos for-each
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
        // Acceso directo a las listas internas (recuerda hacerlas public en CustomQueue)
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
}