/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.procesos;

/**
 *
 * @author gonzalo
 */
import com.mycompany.mavenproject1.core.FileSystemManager;
import com.mycompany.mavenproject1.estructuras.CustomQueue;

import java.util.NoSuchElementException;

public class PlanificadorDisco {
    private static PlanificadorDisco instance;

    private final CustomQueue<SolicitudES> colaES;
    private PoliticaPlanificacion politicaActual = PoliticaPlanificacion.FIFO;
    private int cabezaDiscoActual = 0;
    private boolean scanDirectionUp = true;
    
    // Se inyecta en el Singleton
    private FileSystemManager fileSystemManager;

    private PlanificadorDisco() {
        this.colaES = new CustomQueue<>();
    }

    public static PlanificadorDisco getInstance() {
        if (instance == null) {
            instance = new PlanificadorDisco();
        }
        return instance;
    }

    public void setFileSystemManager(FileSystemManager fsm) {
        this.fileSystemManager = fsm;
    }

    public void agregarSolicitud(SolicitudES s) {
        colaES.enqueue(s);
    }

    // --- Lógica de Planificación Principal ---
    public SolicitudES getSiguienteSolicitud() {
        if (colaES.isEmpty()) return null;

        return switch (politicaActual) {
            case FIFO -> colaES.dequeue();
            case SSTF -> logicaSSTF();
            // Implementación de SCAN/CSCAN omitida por brevedad
            default -> colaES.dequeue(); 
        };
    }
    
    private SolicitudES logicaSSTF() {
        // SSTF: Busca la solicitud con el seek mínimo (distancia a targetBlock)
        SolicitudES mejorSolicitud = null;
        int minSeek = Integer.MAX_VALUE;
        
        // La cola no tiene una forma eficiente de iterar y remover O(1),
        // por lo que debemos iterar sobre la lista interna y recrear la cola si se remueve.
        // **Este enfoque es ineficiente (O(N^2)) por la restricción de estructuras.**
        // Una lista con doble enlace y puntero al anterior simplificaría esto.
        
        // Simulación: Buscamos la mejor solicitud de la lista interna.
        for (SolicitudES s : colaES.listaInterna) {
            int target = s.targetBlock;
            if (target == -1) { // CREAR: Tratar como si fuera la primera
                return colaES.dequeue();
            }
            int seek = Math.abs(target - cabezaDiscoActual);
            
            if (seek < minSeek) {
                minSeek = seek;
                mejorSolicitud = s;
            }
        }
        
        // Si encontramos una solicitud SSTF, la eliminamos de la lista interna
        if (mejorSolicitud != null) {
            colaES.listaInterna.remove(mejorSolicitud);
            return mejorSolicitud;
        }
        // Si no se encontró ninguna (solo peticiones CREAR), tratamos como FIFO si existe algo.
        return colaES.dequeue();
    }
    
    // --- Métodos de Gestión ---
    public void setPolitica(PoliticaPlanificacion p) { politicaActual = p; }
    public int getPosicionCabeza() { return cabezaDiscoActual; }
    public void setCabezaDisco(int pos) { cabezaDiscoActual = pos; }
    public int getSizeColaES() { return colaES.size(); }
}
