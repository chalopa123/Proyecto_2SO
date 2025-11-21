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
import com.mycompany.mavenproject1.modelo.Archivo;
import com.mycompany.mavenproject1.modelo.Directorio;
import com.mycompany.mavenproject1.modelo.FileSystemNode;
import com.mycompany.mavenproject1.procesos.PoliticaPlanificacion;
import com.mycompany.mavenproject1.procesos.Proceso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;

public class MainFrame extends JFrame {
    
    // Atributos de Swing (declarados por el diseñador)
    private JTree treeArchivos;
    private JTable tableAsignacion;
    private JTable tableProcesos;
    private JPanel panelDisco;
    private JComboBox<PoliticaPlanificacion> comboPolitica;
    private JTextField textPath;
    private JTextField textSize;
    private JButton btnCrearArchivo, btnCrearDir, btnEliminar;
    // ... otros componentes

    private final Simulador simulador;
    private final FileSystemManager fsManager;

    public MainFrame() {
        initComponents(); // Método generado por NetBeans
        this.simulador = Simulador.getInstance();
        this.fsManager = FileSystemManager.getInstance();
        this.simulador.setGui(this); 
        
        iniciarTimerSimulacion();
        recargarTodo();
        
        // Configurar el dibujado del disco
        panelDisco.setLayout(new BorderLayout());
        panelDisco.add(new DiskPanel(fsManager.getDisco(), simulador.planificador), BorderLayout.CENTER);
        
        // Llenar el ComboBox
        for (PoliticaPlanificacion p : PoliticaPlanificacion.values()) {
            comboPolitica.addItem(p);
        }
    }
    
    // Método simulado
    private void initComponents() {
        // Inicialización de componentes...
    }

    public void iniciarTimerSimulacion() {
        // 1 segundo por "tick"
        new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulador.tickSimulacion();
            }
        }).start();
    }

    public void recargarTodo() {
        recargarArbol();
        recargarTablaAsignacion();
        recargarTablaProcesos();
        panelDisco.repaint();
    }

    // --- Recarga del Árbol de Archivos ---
    public void recargarArbol() {
        Directorio rootModelo = fsManager.getRoot();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootModelo.getNombre());
        construirNodos(rootNode, rootModelo);
        treeArchivos.setModel(new DefaultTreeModel(rootNode));
    }

    private void construirNodos(DefaultMutableTreeNode nodoSwing, Directorio dirModelo) {
        for (FileSystemNode hijo : dirModelo.getHijos()) {
            DefaultMutableTreeNode hijoNode = new DefaultMutableTreeNode(hijo.getNombre());
            nodoSwing.add(hijoNode);

            if (hijo instanceof Directorio dirHijo) {
                construirNodos(hijoNode, dirHijo);
            }
        }
    }

    // --- Recarga de Tabla de Asignación ---
    public void recargarTablaAsignacion() {
        DefaultTableModel model = (DefaultTableModel) tableAsignacion.getModel();
        model.setRowCount(0); // Limpiar

        recorrerYAnadirArchivos(fsManager.getRoot(), model);
    }
    
    private void recorrerYAnadirArchivos(Directorio dir, DefaultTableModel model) {
        for (FileSystemNode hijo : dir.getHijos()) {
            if (hijo instanceof Archivo archivo) {
                model.addRow(new Object[]{
                    archivo.getNombre(), 
                    archivo.getTamanoEnBloques(), 
                    archivo.getPrimerBloque(), 
                    archivo.getProcesoCreador().PID
                });
            } else if (hijo instanceof Directorio dirHijo) {
                recorrerYAnadirArchivos(dirHijo, model);
            }
        }
    }

    // --- Recarga de Tabla de Procesos ---
    public void recargarTablaProcesos() {
        DefaultTableModel model = (DefaultTableModel) tableProcesos.getModel();
        model.setRowCount(0); // Limpiar

        // Procesos en LISTO
        for (Proceso p : simulador.getColaListos().listaInterna) {
             model.addRow(new Object[]{p.PID, p.estado, p.operacionAsociada, p.path});
        }
        // Procesos en BLOQUEADO
        for (Proceso p : simulador.getColaBloqueados().listaInterna) {
             model.addRow(new Object[]{p.PID, p.estado, p.operacionAsociada, p.path});
        }
        // Nota: Procesos TERMINADO no se muestran a menos que se use una lista de procesos completa.
    }
    
    // --- Lógica de Eventos (Simulada) ---
    private void btnCrearArchivoActionPerformed(ActionEvent evt) {
        String path = textPath.getText();
        int size = Integer.parseInt(textSize.getText());
        simulador.crearProcesoSimulado(com.unimet.so.procesos.OperacionCRUD.CREAR, path, size);
    }
    
    // ... otros ActionListeners ...
}

// --- Clase interna para dibujar el disco ---
class DiskPanel extends JPanel {
    private final Disco disco;
    private final PlanificadorDisco planificador;
    private final int BLOCK_SIZE = 15;
    private final int BLOCKS_PER_ROW = 10;

    public DiskPanel(Disco disco, PlanificadorDisco planificador) {
        this.disco = disco;
        this.planificador = planificador;
        setPreferredSize(new Dimension(BLOCK_SIZE * BLOCKS_PER_ROW + 1, (int) Math.ceil((double) disco.totalBloques / BLOCKS_PER_ROW) * BLOCK_SIZE + 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawString("Cabeza: " + planificador.getPosicionCabeza(), 5, BLOCK_SIZE * BLOCKS_PER_ROW + 15);

        for (int i = 0; i < disco.totalBloques; i++) {
            int row = i / BLOCKS_PER_ROW;
            int col = i % BLOCKS_PER_ROW;
            int x = col * BLOCK_SIZE;
            int y = row * BLOCK_SIZE;

            if (disco.bloques[i].estaLibre) {
                g.setColor(Color.GREEN.darker());
            } else {
                // Color por proceso
                int pid = disco.bloques[i].pidProceso;
                g.setColor(new Color((pid * 100) % 256, (pid * 50) % 256, (pid * 150) % 256));
            }

            g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);

            // Resaltar la posición de la cabeza
            if (i == planificador.getPosicionCabeza()) {
                g.setColor(Color.YELLOW);
                g.drawRect(x + 1, y + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);
            }
        }
    }
}