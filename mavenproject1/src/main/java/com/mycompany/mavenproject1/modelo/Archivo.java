/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.modelo;

/**
 *
 * @author gonzalo
 */
import com.mycompany.mavenproject1.procesos.Proceso;

public class Archivo extends FileSystemNode {
    private final int tamanoEnBloques;
    private final int primerBloque;
    private final Proceso procesoCreador;

    public Archivo(String nombre, Directorio padre, int tamanoEnBloques, int primerBloque, Proceso procesoCreador) {
        super(nombre, padre);
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = primerBloque;
        this.procesoCreador = procesoCreador;
    }
    
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public int getPrimerBloque() { return primerBloque; }
    public Proceso getProcesoCreador() { return procesoCreador; }
}