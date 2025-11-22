/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.procesos;

/**
 *
 * @author gonzalo
 */

public class SolicitudES {
    public final Proceso procesoOrigen;
    public final OperacionCRUD tipoOperacion;
    public final String path;
    public final int tamano;
    public final int targetBlock; 

    public SolicitudES(Proceso p, OperacionCRUD tipo, String path, int tamano, int targetBlock) {
        this.procesoOrigen = p;
        this.tipoOperacion = tipo;
        this.path = path;
        this.tamano = tamano;
        this.targetBlock = targetBlock;
    }
}
