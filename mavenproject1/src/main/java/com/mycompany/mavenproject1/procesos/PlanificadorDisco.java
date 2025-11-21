/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.procesos;

/**
 *
 * @author gonzalo
 */
import com.mycompany.mavenproject1.core.FileSystemManager;
import com.mycompany.mavenproject1.estructuras.CustomQueue;

public class PlanificadorDisco {
    private static PlanificadorDisco instance;
    private final CustomQueue<SolicitudES> colaES;
    private PoliticaPlanificacion politicaActual = PoliticaPlanificacion.FIFO;
    private int cabezaDiscoActual = 0;
    private boolean scanDirectionUp = true; // true = subiendo, false = bajando
    private FileSystemManager fileSystemManager;

    private PlanificadorDisco() {
        this.colaES = new CustomQueue<>();
    }

    public static PlanificadorDisco getInstance() {
        if (instance == null) instance = new PlanificadorDisco();
        return instance;
    }

    public void setFileSystemManager(FileSystemManager fsm) {
        this.fileSystemManager = fsm;
    }

    public void agregarSolicitud(SolicitudES s) {
        colaES.enqueue(s);
    }

    public SolicitudES getSiguienteSolicitud() {
        if (colaES.isEmpty()) return null;

        return switch (politicaActual) {
            case FIFO -> colaES.dequeue();
            case SSTF -> logicaSSTF();
            case SCAN -> logicaSCAN();
            case CSCAN -> logicaCSCAN();
            default -> colaES.dequeue();
        };
    }
    
    // --- Lógica SSTF (Shortest Seek Time First) ---
    private SolicitudES logicaSSTF() {
        SolicitudES mejor = null;
        int minSeek = Integer.MAX_VALUE;

        // Iterar lista interna (O(n)) ya que no podemos usar iterators de java.util
        // Accedemos a la lista pública 'listaInterna' de CustomQueue según tu código subido
        // NOTA: Asumo que hiciste 'public CustomLinkedList listaInterna' en CustomQueue
        for (SolicitudES s : colaES.listaInterna) {
            if (s.targetBlock == -1) return colaES.dequeue(); // Prioridad a CREAR
            
            int seek = Math.abs(s.targetBlock - cabezaDiscoActual);
            if (seek < minSeek) {
                minSeek = seek;
                mejor = s;
            }
        }
        if (mejor != null) colaES.listaInterna.remove(mejor);
        return mejor;
    }

    // --- Lógica SCAN (Elevador) ---
    private SolicitudES logicaSCAN() {
        SolicitudES mejor = null;
        int minDist = Integer.MAX_VALUE;
        
        // Buscar en la dirección actual
        for (SolicitudES s : colaES.listaInterna) {
             if (s.targetBlock == -1) return colaES.dequeue();

             int dist = s.targetBlock - cabezaDiscoActual;
             // Si vamos subiendo (dist > 0) o bajando (dist < 0)
             if ((scanDirectionUp && dist >= 0) || (!scanDirectionUp && dist <= 0)) {
                 if (Math.abs(dist) < minDist) {
                     minDist = Math.abs(dist);
                     mejor = s;
                 }
             }
        }
        
        // Si encontramos uno en la dirección actual, lo devolvemos
        if (mejor != null) {
            colaES.listaInterna.remove(mejor);
            return mejor;
        } else {
            // Si no, cambiamos de dirección y volvemos a llamar (recursivo simple)
            scanDirectionUp = !scanDirectionUp;
            // Para evitar loop infinito si la cola tiene elementos inalcanzables (raro), 
            // podriamos chequear si hubo cambio previo, pero para simplificar:
            return logicaSSTF(); // Fallback si SCAN falla en primera vuelta
        }
    }

    // --- Lógica C-SCAN (Circular SCAN) ---
    private SolicitudES logicaCSCAN() {
        // Solo atiende subiendo. Si llega al final, salta al 0.
        SolicitudES mejor = null;
        int minDist = Integer.MAX_VALUE;

        for (SolicitudES s : colaES.listaInterna) {
            if (s.targetBlock == -1) return colaES.dequeue();

            int dist = s.targetBlock - cabezaDiscoActual;
            if (dist >= 0) { // Solo consideramos bloques adelante
                if (dist < minDist) {
                    minDist = dist;
                    mejor = s;
                }
            }
        }

        if (mejor != null) {
            colaES.listaInterna.remove(mejor);
            return mejor;
        } else {
            // Si no hay nada adelante, saltamos al inicio (cabeza = 0) virtualmente
            // En simulación, devolvemos el bloque con el ID más bajo absoluto
            cabezaDiscoActual = 0; 
            return logicaSSTF(); // Buscará el más cercano a 0
        }
    }

    // Getters y Setters
    public void setPolitica(PoliticaPlanificacion p) { politicaActual = p; }
    public int getPosicionCabeza() { return cabezaDiscoActual; }
    public void setCabezaDisco(int pos) { cabezaDiscoActual = pos; }
    public int getSizeColaES() { return colaES.size(); }
}