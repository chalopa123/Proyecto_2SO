/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.modelo;

/**
 *
 * @author gonzalo
 */
public class Block {
    public int id;
    public boolean estaLibre;
    public int pidProceso;
    public int siguienteBloque;

    public Block(int id) {
        this.id = id;
        this.estaLibre = true;
        this.pidProceso = -1;
        this.siguienteBloque = -1;
    }
}
