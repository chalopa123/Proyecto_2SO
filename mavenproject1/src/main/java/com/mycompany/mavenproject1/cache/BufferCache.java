/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.cache;

/**
 *
 * @author gonzalo
 */

import com.mycompany.mavenproject1.estructuras.CustomLinkedList;
import com.mycompany.mavenproject1.estructuras.CustomQueue;
import com.mycompany.mavenproject1.modelo.Block;
import com.mycompany.mavenproject1.modelo.Disco;

// Asumimos las definiciones en el paquete procesos
enum PoliticaReemplazo { FIFO, LRU, LFU } 

public class BufferCache {
    private final int CACHE_SIZE = 10;
    private final Block[] cache;
    private final PoliticaReemplazo politica;
    private final CustomQueue<Integer> fifoQueue; // Índice de bloque de caché
    // LRU: Índice del bloque de caché - el más usado/reciente al final
    private final CustomLinkedList<Integer> lruList; 

    public BufferCache() {
        this.cache = new Block[CACHE_SIZE];
        this.politica = PoliticaReemplazo.LRU; // Usaremos LRU por defecto
        this.fifoQueue = new CustomQueue<>();
        this.lruList = new CustomLinkedList<>();
    }

    public Block leerBloque(int blockId, Disco disco) {
        // 1. Buscar en la caché (HIT/MISS)
        int victimIndex = -1;
        
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cache[i] != null && cache[i].id == blockId) {
                // **HIT**
                // Actualizar LRU: Mover el índice al final de la lista
                if (politica == PoliticaReemplazo.LRU) {
                    lruList.remove(i);
                    lruList.addLast(i);
                }
                System.out.println("Cache HIT para bloque " + blockId);
                return cache[i];
            }
        }

        // **MISS**
        System.out.println("Cache MISS para bloque " + blockId);
        Block bloqueDeDisco = disco.getBloque(blockId);
        if (bloqueDeDisco == null) return null;

        // 2. Determinar el índice de la víctima (o un espacio libre)
        
        // Buscar un espacio libre primero
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cache[i] == null) {
                victimIndex = i;
                break;
            }
        }
        
        // Si no hay espacio libre, aplicar la política de reemplazo
        if (victimIndex == -1) {
            victimIndex = switch (politica) {
                case FIFO -> fifoQueue.dequeue();
                case LRU -> lruList.removeFirst(); // El menos recientemente usado
                case LFU -> 0; // LFU requiere más estructuras (Map o List de tuplas)
            };
        }

        // 3. Reemplazar y actualizar estructuras de política
        cache[victimIndex] = bloqueDeDisco;
        
        if (politica == PoliticaReemplazo.FIFO) {
            fifoQueue.enqueue(victimIndex);
        } else if (politica == PoliticaReemplazo.LRU) {
            lruList.addLast(victimIndex);
        }
        
        return bloqueDeDisco;
    }
}