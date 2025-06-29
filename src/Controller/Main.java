/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author Lenovo
 */
// Main.java
// Main class to launch the application

public class Main {
    public static void main(String[] args) {
        // Launch the SignIn GUI
        // SwingUtilities.invokeLater for thread safety with Swing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SignIn signInFrame = new SignIn();
                signInFrame.setVisible(true);
            }
        });
    }
}

