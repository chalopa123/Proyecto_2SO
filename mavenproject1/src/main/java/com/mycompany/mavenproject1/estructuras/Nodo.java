/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gonzalo
 */
package com.mycompany.mavenproject1.estructuras;

public class Nodo<T> {
    public T data;
    public Nodo<T> siguiente;

    public Nodo(T data) {
        this.data = data;
        this.siguiente = null;
    }
}
