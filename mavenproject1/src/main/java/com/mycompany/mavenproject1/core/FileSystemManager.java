/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.core;

/**
 *
 * @author gonzalo
 */

import com.mycompany.mavenproject1.modelo.*;
import com.mycompany.mavenproject1.procesos.Proceso;
import com.mycompany.mavenproject1.cache.BufferCache;
import com.mycompany.mavenproject1.estructuras.CustomLinkedList;

import java.util.NoSuchElementException;

public class FileSystemManager {
    private static FileSystemManager instance;

    private final Directorio root;
    private final Disco disco;
    private final BufferCache bufferCache;

    private FileSystemManager() {
        // Inicializar con un tamaño razonable
        this.disco = new Disco(100); 
        this.root = new Directorio("/", null);
        this.bufferCache = new BufferCache(); // Suponiendo tamaño y política por defecto
    }

    public static FileSystemManager getInstance() {
        if (instance == null) {
            instance = new FileSystemManager();
        }
        return instance;
    }
    
    // Método para reemplazar la instancia (usado al Cargar)
    public static void setInstance(FileSystemManager newInstance) {
        instance = newInstance;
    }

    // --- Helpers de Navegación ---
    public FileSystemNode findNode(String path) {
        if (path == null || path.equals("/")) return root;

        String[] partes = path.substring(1).split("/");
        FileSystemNode actual = root;

        for (String nombre : partes) {
            if (actual instanceof Directorio dirActual) {
                actual = dirActual.findHijo(nombre);
                if (actual == null) return null;
            } else {
                return null; // Intentando navegar dentro de un Archivo
            }
        }
        return actual;
    }

    // --- Métodos de Operaciones ---

    public boolean crearArchivo(String path, int tamano, Proceso p) {
        if (disco.getBloquesLibres() < tamano) {
            System.out.println("Fallo al crear: No hay suficientes bloques libres.");
            return false;
        }

        int ultimoSlash = path.lastIndexOf('/');
        String pathPadre = (ultimoSlash == 0) ? "/" : path.substring(0, ultimoSlash);
        String nombreArchivo = path.substring(ultimoSlash + 1);

        FileSystemNode nodoPadre = findNode(pathPadre);
        if (!(nodoPadre instanceof Directorio padre)) return false; // Padre no encontrado o es Archivo

        int primerBloque = -1;
        int bloqueAnterior = -1;

        for (int i = 0; i < tamano; i++) {
            int siguienteBloque = disco.allocateBlock(p.PID);
            if (siguienteBloque == -1) {
                // Debería ser imposible si la comprobación inicial es correcta
                // Se necesitaría lógica para liberar los bloques ya asignados
                return false; 
            }

            if (i == 0) {
                primerBloque = siguienteBloque;
            } else {
                disco.linkBlocks(bloqueAnterior, siguienteBloque);
            }
            bloqueAnterior = siguienteBloque;
        }

        Archivo archivo = new Archivo(nombreArchivo, padre, tamano, primerBloque, p);
        padre.addHijo(archivo);
        return true;
    }

    public boolean eliminarArchivo(String path) {
        FileSystemNode nodoAEliminar = findNode(path);
        if (!(nodoAEliminar instanceof Archivo archivo)) return false;

        int actual = archivo.getPrimerBloque();
        while (actual != -1) {
            // Usa el BufferCache para leer (aunque en un caso real se necesitaría la dirección física)
            Block b = bufferCache.leerBloque(actual, disco); 
            int siguiente = b.siguienteBloque;
            disco.freeBlock(actual);
            actual = siguiente;
        }

        Directorio padre = archivo.getPadre();
        if (padre != null) {
            return padre.removeHijo(archivo.getNombre());
        }
        return false;
    }
    
    public boolean crearDirectorio(String path) {
        int ultimoSlash = path.lastIndexOf('/');
        String pathPadre = (ultimoSlash == 0) ? "/" : path.substring(0, ultimoSlash);
        String nombreDir = path.substring(ultimoSlash + 1);

        FileSystemNode nodoPadre = findNode(pathPadre);
        if (!(nodoPadre instanceof Directorio padre)) return false;

        Directorio nuevoDir = new Directorio(nombreDir, padre);
        padre.addHijo(nuevoDir);
        return true;
    }
    
    public boolean eliminarDirectorio(String path) {
        FileSystemNode nodoAEliminar = findNode(path);
        if (!(nodoAEliminar instanceof Directorio dir)) return false;

        // Implementación recursiva para liberar el contenido
        CustomLinkedList<FileSystemNode> hijos = dir.getHijos();
        // Usar una copia o iterador seguro si se modifica la lista
        CustomLinkedList<FileSystemNode> copiaHijos = new CustomLinkedList<>();
        for (FileSystemNode hijo : hijos) {
            copiaHijos.addLast(hijo);
        }

        for (FileSystemNode hijo : copiaHijos) {
            String hijoPath = path + "/" + hijo.getNombre();
            if (hijo instanceof Archivo) {
                eliminarArchivo(hijoPath);
            } else if (hijo instanceof Directorio) {
                eliminarDirectorio(hijoPath);
            }
        }
        
        // Finalmente, eliminar el directorio del padre
        Directorio padre = dir.getPadre();
        if (padre != null) {
            return padre.removeHijo(dir.getNombre());
        }
        return false;
    }

    // Getters para la GUI y el Simulador
    public Directorio getRoot() { return root; }
    public Disco getDisco() { return disco; }
    public BufferCache getBufferCache() { return bufferCache; }
}
