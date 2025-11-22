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
        for (FileSystemNode hijo : hijos) {
            if (hijo.getNombre().equals(nombre)) {
                return hijos.remove(hijo);
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