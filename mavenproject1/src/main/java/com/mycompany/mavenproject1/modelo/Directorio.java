/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.modelo;

/**
 * @author adrian
 * @author gonzalo
 */
import com.mycompany.mavenproject1.estructuras.CustomLinkedList;
import com.mycompany.mavenproject1.estructuras.Nodo;

public class Directorio extends FileSystemNode {
    private final CustomLinkedList<FileSystemNode> hijos;

    public Directorio(String nombre, Directorio padre) {
        super(nombre, padre);
        this.hijos = new CustomLinkedList<>();
    }

    public void addHijo(FileSystemNode hijo) {
        hijos.addLast(hijo);
    }

    public boolean removeHijo(String nombre) {
        Nodo<FileSystemNode> actual = hijos.getCabeza();
        
        while (actual != null) {
            FileSystemNode hijo = actual.getValor();
            
            if (hijo.getNombre().equals(nombre)) {
                //CustomLinkedList tiene un método remove que acepta el objeto
                return hijos.remove(hijo);
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public FileSystemNode findHijo(String nombre) {
        // Recorrido manual sin usar Iterator
        Nodo<FileSystemNode> actual = hijos.getCabeza();
        
        while (actual != null) {
            FileSystemNode hijo = actual.getValor(); // Obtener el objeto del nodo
            
            // Comparar nombres
            if (hijo.getNombre().equals(nombre)) {
                return hijo;
            }
            
            actual = actual.getSiguiente(); // Avanzar al siguiente nodo
        }
        return null;
    }
    
    public CustomLinkedList<FileSystemNode> getHijos() { return hijos; }
}