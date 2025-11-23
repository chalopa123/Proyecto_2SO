/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mavenproject1;

import com.mycompany.mavenproject1.gui.MainFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author gonzalo
 */
public class Mavenproject1 {

    public static void main(String[] args) {
        // Es una buena práctica iniciar aplicaciones Swing en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Opcional: Intentar poner el estilo visual del sistema operativo
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) { // O "Windows", "GTK+"
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // Si falla, no pasa nada, usa el defecto
                System.err.println("No se pudo aplicar el LookAndFeel deseado.");
            }

            // Crear y mostrar la ventana principal
            MainFrame frame = new MainFrame();
            frame.setLocationRelativeTo(null); // Centrar la ventana en la pantalla
            frame.setVisible(true);
        });
    }
}