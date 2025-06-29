/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author Lenovo
 */
// ControllerRegistro.java
// Handles registration logic

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet; // Added for checking if user exists
import java.io.FileInputStream; // For project.properties
import java.io.IOException; // For project.properties
import java.util.Properties; // For project.properties

public class ControllerRegistro {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public ControllerRegistro() {
        // Load database configuration from project.properties
        // In a real application, consider a more robust config management or dependency injection
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("nbproject/project.properties")) {
            props.load(fis);
            this.dbUrl = props.getProperty("db.url");
            this.dbUser = props.getProperty("db.user");
            this.dbPassword = props.getProperty("db.password");

            // Ensure MySQL driver is loaded (though Class.forName is often not needed with modern JDBC)
            // Class.forName(props.getProperty("db.driver")); 
        } catch (IOException e) {
            // Handle error loading properties - for now, print error and use defaults or fail
            System.err.println("Error loading database configuration: " + e.getMessage());
            // Set to null or default so that operations requiring them fail clearly
            this.dbUrl = null; 
            // Or throw a runtime exception: throw new RuntimeException("Failed to load DB config", e);
        }
        // Catching ClassNotFoundException if Class.forName was used
        // catch (ClassNotFoundException e) {
        //     System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        //     throw new RuntimeException("JDBC Driver not found", e);
        // }
    }

    /**
     * Registers a new admin user.
     *
     * @param username The desired username for the new admin.
     * @param plainPassword The plain-text password for the new admin.
     * @return True if registration is successful, false otherwise (e.g., user exists, DB error).
     */
    public boolean registrarAdmin(String username, String plainPassword) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot register admin.");
            return false;
        }
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            System.err.println("Username or password cannot be empty.");
            return false;
        }

        // 1. Check if user already exists (to prevent duplicates)
        if (adminExists(username)) {
            System.err.println("Admin user '" + username + "' already exists.");
            return false;
        }

        // 2. Hash the password
        String hashedPasswordWithSalt = Password.hashPassword(plainPassword);
        if (hashedPasswordWithSalt == null) {
            System.err.println("Password hashing failed for user: " + username);
            return false;
        }

        // 3. Store the new admin in the database
        String sql = "INSERT INTO admin (username, password_hash) VALUES (?, ?)";
        // The table name is 'admin' and column for password is 'password_hash' (as per conceptual DB setup)

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPasswordWithSalt);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Admin user '" + username + "' registered successfully.");
                return true;
            } else {
                System.err.println("Admin user registration failed for '" + username + "' (no rows affected).");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database error during admin registration for '" + username + "': " + e.getMessage());
            // e.printStackTrace(); // For more detailed diagnostics during development
            return false;
        }
    }

    /**
     * Checks if an admin with the given username already exists in the database.
     * @param username The username to check.
     * @return true if an admin with that username exists, false otherwise.
     */
    private boolean adminExists(String username) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot check if admin exists.");
            // Default to true to prevent attempts to create if config is broken,
            // or false if you want to allow registration attempt which will then fail at DB level.
            // Let's say false, so the registration attempt proceeds and fails there.
            return false; 
        }
        String sql = "SELECT id FROM admin WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If rs.next() is true, a user with that username exists
            }
        } catch (SQLException e) {
            System.err.println("Database error checking if admin '" + username + "' exists: " + e.getMessage());
            // In case of error, assume user might exist to be safe, or handle error differently
            return true; // Safer to assume exists on error to prevent duplicate attempts if DB is flaky
        }
    }
    
    // Main method for testing ControllerRegistro independently (optional)
    public static void main(String[] args) {
        ControllerRegistro controller = new ControllerRegistro();
        
        // Ensure your MySQL server is running and the database/table exists
        // and project.properties has correct db.url, db.user, db.password
        // And mysql-connector JAR is in the classpath (e.g. in 'lib' and referenced by Ant)
        
        // Example: Attempt to register a new admin
        // boolean success = controller.registrarAdmin("newadmin", "securepassword123");
        // if (success) {
        //     System.out.println("Test registration successful.");
        // } else {
        //     System.out.println("Test registration failed.");
        // }
        
        // Example: Attempt to register an existing admin (should fail if already present)
        // boolean successExisting = controller.registrarAdmin("newadmin", "anotherpassword");
        // if (!successExisting) {
        //     System.out.println("Test registration for existing user correctly failed.");
        // } else {
        //     System.out.println("Test registration for existing user incorrectly succeeded.");
        // }
        System.out.println("ControllerRegistro main method for testing. Uncomment internal tests.");
        System.out.println("DB URL: " + controller.dbUrl); // Check if props loaded
        if (controller.dbUrl == null) {
             System.err.println("DB Config not loaded. Tests will fail. Check nbproject/project.properties path and content.");
        }
    }
}

