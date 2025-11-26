/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gonzalo
 */

package com.mycompany.mavenproject1.estructuras;

import com.mycompany.mavenproject1.modelo.FileSystemNode;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomLinkedList<T> implements Iterable<T> {
    Nodo<T> head;
    private Nodo<T> tail;
    private int size;

    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // O(1)
    public void addLast(T data) {
        Nodo<T> nuevoNodo = new Nodo<>(data);
        if (isEmpty()) {
            head = nuevoNodo;
        } else {
            tail.siguiente = nuevoNodo;
        }
        tail = nuevoNodo;
        size++;
    }

    // O(1)
    public void addFirst(T data) {
        Nodo<T> nuevoNodo = new Nodo<>(data);
        nuevoNodo.siguiente = head;
        head = nuevoNodo;
        if (tail == null) {
            tail = head;
        }
        size++;
    }

    // O(1)
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("La lista está vacía.");
        }
        T data = head.data;
        head = head.siguiente;
        if (head == null) {
            tail = null;
        }
        size--;
        return data;
    }

    // O(n) - Requiere buscar el nodo anterior
    public boolean remove(T data) {
        if (isEmpty()) return false;

        if (head.data.equals(data)) {
            removeFirst();
            return true;
        }

        Nodo<T> actual = head;
        while (actual.siguiente != null && !actual.siguiente.data.equals(data)) {
            actual = actual.siguiente;
        }

        if (actual.siguiente != null) { // Encontrado
            Nodo<T> aEliminar = actual.siguiente;
            actual.siguiente = aEliminar.siguiente;
            if (aEliminar == tail) {
                tail = actual;
            }
            size--;
            return true;
        }
        return false;
    }
    
    // O(1)
    public boolean isEmpty() {
        return size == 0;
    }

    // O(1)
    public int size() {
        return size;
    }
    
    public Nodo<T> getCabeza() {
        return this.head; // O como hayas llamado a tu variable 'head' o 'first'
    }
    
    // O(1) para empezar la iteración
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = head;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T data = actual.data;
                actual = actual.siguiente;
                return data;
            }
        };
    }

    
}

