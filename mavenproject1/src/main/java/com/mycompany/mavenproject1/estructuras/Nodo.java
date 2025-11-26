/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *@author adrian
 * @author gonzalo
 */
package com.mycompany.mavenproject1.estructuras;

import com.mycompany.mavenproject1.modelo.FileSystemNode;

public class Nodo<T> {
    public T data;
    public Nodo<T> siguiente;

    public Nodo(T data) {
        this.data = data;
        this.siguiente = null;
    }

    public T getValor() { return data; } // o getData()
    public Nodo<T> getSiguiente() { return siguiente; } // o getNext()
}
