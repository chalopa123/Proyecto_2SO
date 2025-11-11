/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.procesos;

/**
 *
 * @author gonzalo
 */

public class Proceso {
    private static int nextPID = 1;

    public final int PID;
    public EstadoProceso estado;
    public final OperacionCRUD operacionAsociada;
    public final String path;
    public final int tamanoArchivo;

    public Proceso(OperacionCRUD op, String path, int tamano) {
        this.PID = nextPID++;
        this.estado = EstadoProceso.NUEVO;
        this.operacionAsociada = op;
        this.path = path;
        this.tamanoArchivo = tamano;
    }
    
    // Getters (para simplificar, no se usan en el ejemplo)
}

class SolicitudES {
    public final Proceso procesoOrigen;
    public final OperacionCRUD tipoOperacion;
    public final String path;
    public final int tamano;
    public final int targetBlock; // Bloque físico objetivo; -1 si es CREAR

    public SolicitudES(Proceso p, OperacionCRUD tipo, String path, int tamano, int targetBlock) {
        this.procesoOrigen = p;
        this.tipoOperacion = tipo;
        this.path = path;
        this.tamano = tamano;
        this.targetBlock = targetBlock;
    }
}
