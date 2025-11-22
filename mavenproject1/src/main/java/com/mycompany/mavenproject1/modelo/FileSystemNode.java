/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.modelo;

/**
 *
 * @author gonzalo
 */
public abstract class FileSystemNode {
    private final String nombre;
    private Directorio padre;

    public FileSystemNode(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
    }
    
    // Getters y Setters
    public String getNombre() { return nombre; }
    public Directorio getPadre() { return padre; }
    public void setPadre(Directorio padre) { this.padre = padre; }
}