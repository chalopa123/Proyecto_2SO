/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.core;

/**
 * @author adrian
 * @author gonzalo
 */

import com.mycompany.mavenproject1.estructuras.CustomQueue;
import com.mycompany.mavenproject1.procesos.*;
import com.mycompany.mavenproject1.gui.MainFrame;
import com.mycompany.mavenproject1.modelo.FileSystemNode;
import com.mycompany.mavenproject1.modelo.Archivo;

import java.util.NoSuchElementException;

public class Simulador {
    private static Simulador instance;

    private final FileSystemManager fsManager;
    private final PlanificadorDisco planificador;
    private final CustomQueue<Proceso> colaListos;
    private final CustomQueue<Proceso> colaBloqueados;
    private MainFrame gui; // Referencia para actualizar la UI

    private Simulador() {
        this.fsManager = FileSystemManager.getInstance();
        this.planificador = PlanificadorDisco.getInstance();
        this.planificador.setFileSystemManager(fsManager); // Inyectar dependencia
        this.colaListos = new CustomQueue<>();
        this.colaBloqueados = new CustomQueue<>();
    }

    public static Simulador getInstance() {
        if (instance == null) {
            instance = new Simulador();
        }
        return instance;
    }

    public void setGui(MainFrame gui) {
        this.gui = gui;
    }

    public void crearProcesoSimulado(OperacionCRUD op, String path, int tamano) {
        Proceso p = new Proceso(op, path, tamano);
        // Lógica de Admisión
        p.estado = EstadoProceso.LISTO;
        colaListos.enqueue(p);
        if (gui != null) gui.recargarTodo();
    }

    public void tickSimulacion() {
        // Paso 1: Despachar Proceso
        Proceso pEjecutando = null;
        if (!colaListos.isEmpty()) {
            pEjecutando = colaListos.dequeue();
            // Paso 2: "Ejecutar" Proceso
            pEjecutando.estado = EstadoProceso.EJECUTANDO;

            // Generar Solicitud de E/S
            int targetBlock = -1;
            if (pEjecutando.operacionAsociada != OperacionCRUD.CREAR &&
                pEjecutando.operacionAsociada != OperacionCRUD.CREAR_DIR) {
                FileSystemNode nodo = fsManager.findNode(pEjecutando.path);
                if (nodo instanceof Archivo archivo) {
                    targetBlock = archivo.getPrimerBloque();
                }
            }

            SolicitudES req = new SolicitudES(
                pEjecutando, pEjecutando.operacionAsociada, pEjecutando.path, 
                pEjecutando.tamanoArchivo, targetBlock
            );
            
            planificador.agregarSolicitud(req);
            pEjecutando.estado = EstadoProceso.BLOQUEADO;
            colaBloqueados.enqueue(pEjecutando);
        }

        // Paso 3: Procesar E/S (Planificador)
        SolicitudES solicitud = planificador.getSiguienteSolicitud();
        if (solicitud != null) {
            ejecutarOperacionFS(solicitud);

            // Actualizar la posición de la cabeza del disco (simulación del seek)
            int nuevaPos = (solicitud.targetBlock != -1) ? solicitud.targetBlock : planificador.getPosicionCabeza();
            planificador.setCabezaDisco(nuevaPos);

            // Buscar y liberar el proceso de la cola de bloqueados (O(n) por CustomLinkedList)
            Proceso pTerminado = buscarYRemoverBloqueado(solicitud.procesoOrigen);
            if (pTerminado != null) {
                // Asumimos que la operación de E/S termina el proceso de simulación
                pTerminado.estado = EstadoProceso.TERMINADO; 
            }
        }

        // Paso 4: Actualizar GUI
        if (gui != null) gui.recargarTodo();
    }
    
    // O(n) - Función helper para la colaBloqueados
    private Proceso buscarYRemoverBloqueado(Proceso p) {
        // Necesitamos la referencia a la lista interna para remover por objeto
        if (colaBloqueados.listaInterna.remove(p)) {
            return p;
        }
        return null;
    }


    private void ejecutarOperacionFS(SolicitudES s) {
        boolean exito = switch (s.tipoOperacion) {
            case CREAR -> fsManager.crearArchivo(s.path, s.tamano, s.procesoOrigen);
            case CREAR_DIR -> fsManager.crearDirectorio(s.path);
            case ELIMINAR -> {
                //Verificar si es directorio para llamar al método correcto
                FileSystemNode nodo = fsManager.findNode(s.path);
                if (nodo instanceof com.mycompany.mavenproject1.modelo.Directorio) {
                    yield fsManager.eliminarDirectorio(s.path);
                } else {
                    yield fsManager.eliminarArchivo(s.path);
                }
            }
            case LEER, ACTUALIZAR -> { 
                // Operaciones simples de E/S, solo simulan el acceso
                System.out.println("Proceso " + s.procesoOrigen.PID + " leyó/actualizó " + s.path);
                yield true;
            }
            default -> false;
        };

        if (!exito) {
            System.err.println("Fallo la operación " + s.tipoOperacion + " en " + s.path);
        }
    }
    
    // Getters para la GUI
    public CustomQueue<Proceso> getColaListos() { return colaListos; }
    public CustomQueue<Proceso> getColaBloqueados() { return colaBloqueados; }
    public PlanificadorDisco getPlanificador() { return planificador; }
}
