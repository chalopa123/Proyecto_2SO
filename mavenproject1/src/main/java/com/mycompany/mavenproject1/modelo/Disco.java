/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.modelo;

/**
 *
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

    public void freeBlock(int blockIndex) {
        Block b = bloques[blockIndex];
        if (!b.estaLibre) {
            b.estaLibre = true;
            b.pidProceso = -1;
            b.siguienteBloque = -1;
            bloquesLibres++;
        }
    }

    public Block getBloque(int index) {
        if (index < 0 || index >= totalBloques) {
            return null;
        }
        return bloques[index];
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