/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.modelo;

/**
 *
 * @author gonzalo
 */


import com.mycompany.mavenproject1.estructuras.CustomLinkedList;
import com.mycompany.mavenproject1.procesos.Proceso;

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

// --- Archivo ---
class Archivo extends FileSystemNode {
    private final int tamanoEnBloques;
    private final int primerBloque;
    private final Proceso procesoCreador;

    public Archivo(String nombre, Directorio padre, int tamanoEnBloques, int primerBloque, Proceso procesoCreador) {
        super(nombre, padre);
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = primerBloque;
        this.procesoCreador = procesoCreador;
    }
    
    // Getters
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public int getPrimerBloque() { return primerBloque; }
    public Proceso getProcesoCreador() { return procesoCreador; }
}

// --- Directorio ---
class Directorio extends FileSystemNode {
    private final CustomLinkedList<FileSystemNode> hijos;

    public Directorio(String nombre, Directorio padre) {
        super(nombre, padre);
        this.hijos = new CustomLinkedList<>();
    }

    public void addHijo(FileSystemNode hijo) {
        hijos.addLast(hijo);
    }

    public boolean removeHijo(String nombre) {
        // La CustomLinkedList ya tiene el método remove(T data)
        // pero requiere que T.equals(data) para funcionar.
        // Aquí necesitamos iterar para encontrar por nombre.
        for (FileSystemNode hijo : hijos) {
            if (hijo.getNombre().equals(nombre)) {
                return hijos.remove(hijo); // Elimina el objeto encontrado
            }
        }
        return false;
    }

    public FileSystemNode findHijo(String nombre) {
        for (FileSystemNode hijo : hijos) {
            if (hijo.getNombre().equals(nombre)) {
                return hijo;
            }
        }
        return null;
    }
    
    public CustomLinkedList<FileSystemNode> getHijos() { return hijos; }
}

