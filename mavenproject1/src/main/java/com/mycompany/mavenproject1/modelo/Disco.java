/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.modelo;

/**
 * @author adrian
 * @author gonzalo
 */

public class Disco {
    public final Block[] bloques;
    public final int totalBloques;
    private int bloquesLibres;

    public Disco(int totalBloques) {
        this.totalBloques = totalBloques;
        this.bloques = new Block[totalBloques];
        for (int i = 0; i < totalBloques; i++) {
            this.bloques[i] = new Block(i);
        }
        this.bloquesLibres = totalBloques;
    }

    public Block findFreeBlock() {
        for (Block bloque : bloques) {
            if (bloque.estaLibre) {
                return bloque;
            }
        }
        return null;
    }

    public int allocateBlock(int pid) {
        Block bloqueLibre = findFreeBlock();
        if (bloqueLibre != null) {
            bloqueLibre.estaLibre = false;
            bloqueLibre.pidProceso = pid;
            bloquesLibres--;
            return bloqueLibre.id;
        }
        return -1;
    }

    public void freeBlock(int index) {
        if (index >= 0 && index < bloques.length) {
        bloques[index].estaLibre = true;
        bloques[index].pidProceso = -1; // O 0, según como manejes "nadie"
        bloques[index].siguienteBloque = -1;
        }
    }

    public Block getBloque(int index) {
        if (index >= 0 && index < bloques.length) {
            return bloques[index];
        }
        return null;
    }

    public void linkBlocks(int blockIndex, int nextBlockIndex) {
        Block b1 = getBloque(blockIndex);
        if (b1 != null) {
            b1.siguienteBloque = nextBlockIndex;
        }
    }

    public int getBloquesLibres() {
        return bloquesLibres;
    }

    public Block getBlock(int actual) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}