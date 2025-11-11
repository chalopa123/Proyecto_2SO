/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.estructuras;

/**
 *
 * @author gonzalo
 */


import java.util.NoSuchElementException;

public class CustomQueue<T> {
    private final CustomLinkedList<T> listaInterna;

    public CustomQueue() {
        this.listaInterna = new CustomLinkedList<>();
    }

    public void enqueue(T data) {
        listaInterna.addLast(data);
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía.");
        }
        return listaInterna.removeFirst();
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía.");
        }
        return listaInterna.head.data;
    }

    public boolean isEmpty() {
        return listaInterna.isEmpty();
    }

    public int size() {
        return listaInterna.size();
    }
}
