/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.core;

/**
 *
 * @author gonzalo
 * @author adrian
 */

import com.mycompany.mavenproject1.modelo.*;
import com.mycompany.mavenproject1.procesos.Proceso;
import com.mycompany.mavenproject1.cache.BufferCache;
import com.mycompany.mavenproject1.estructuras.CustomLinkedList;
import com.mycompany.mavenproject1.estructuras.Nodo;


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
        
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        
        if (cleanPath.isEmpty()) return root;

        String[] partes = cleanPath.split("/");
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
            System.out.println("ERROR: No hay suficientes bloques libres.");
            return false;
        }

        int ultimoSlash = path.lastIndexOf('/');
        String pathPadre = (ultimoSlash == 0) ? "/" : path.substring(0, ultimoSlash);
        String nombreArchivo = path.substring(ultimoSlash + 1);

        FileSystemNode nodoPadre = findNode(pathPadre);
        if (nodoPadre == null) {
            System.out.println("ERROR: El directorio padre no existe: " + pathPadre);
            return false;
        }
        if (!(nodoPadre instanceof Directorio)) {
            System.out.println("ERROR: La ruta padre no es un directorio: " + pathPadre);
            return false;
        }
        
        Directorio padre = (Directorio) nodoPadre;
        
        if (padre.findHijo(nombreArchivo) != null) {
             System.out.println("ERROR: Ya existe un archivo/directorio con el nombre: " + nombreArchivo);
             return false;
        }
        
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
        
        System.out.println("ÉXITO: Archivo creado en " + path);
        return true;
    }

    public boolean eliminarArchivo(String path) {
        FileSystemNode nodoAEliminar = findNode(path);
        
        if (nodoAEliminar == null) {
            System.out.println("ERROR: Archivo no encontrado: " + path);
            return false;
        }
        if (!(nodoAEliminar instanceof Archivo archivo)) {
             System.out.println("ERROR: La ruta no corresponde a un archivo.");
             return false;
        }
        
        int actual = archivo.getPrimerBloque();
        while (actual != -1) {
            // Usa el BufferCache para leer (aunque en un caso real se necesitaría la dirección física)
            Block b = disco.getBlock(actual); 
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
        if (nodoPadre == null) {
             System.out.println("ERROR: Ruta padre no existe: " + pathPadre);
             return false;
        }
        
        if (!(nodoPadre instanceof Directorio padre)) {
             System.out.println("ERROR: El padre no es un directorio.");
             return false;
        }
        
        if (padre.findHijo(nombreDir) != null) {
             System.out.println("ERROR: Ya existe " + nombreDir);
             return false;
        }        
        
        Directorio nuevoDir = new Directorio(nombreDir, padre);
        padre.addHijo(nuevoDir);
        System.out.println("ÉXITO: Directorio creado " + path);
        return true;
    }
    
    public boolean eliminarDirectorio(String path) {
        if (path.equals("/")) {
            System.out.println("ERROR: No se puede eliminar la raíz.");
            return false;
        }
        
        
        FileSystemNode nodoAEliminar = findNode(path);
        if (!(nodoAEliminar instanceof Directorio dir)) return false;

        // Primero recolectamos los nombres de los hijos en una lista temporal auxiliar
        // O iteramos manualmente sobre la lista enlazada original.
        
        CustomLinkedList<FileSystemNode> hijos = dir.getHijos();
        
        // Iteramos manualmente usando tu clase Nodo
        // Asumiendo que CustomLinkedList tiene un método getCabeza() que retorna Nodo<FileSystemNode>
        Nodo<FileSystemNode> actual = hijos.getCabeza(); 
        
        while (actual != null) {
            FileSystemNode hijo = actual.getValor();
            // Guardamos el siguiente ANTES de llamar recursivamente, 
            // porque al eliminar el hijo la estructura de la lista podría cambiar.
            Nodo<FileSystemNode> siguienteNodo = actual.getSiguiente(); 

            String hijoPath = path + "/" + hijo.getNombre();
            
            if (hijo instanceof Archivo) {
                eliminarArchivo(hijoPath);
            } else if (hijo instanceof Directorio) {
                eliminarDirectorio(hijoPath);
            }
            
            actual = siguienteNodo;
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
