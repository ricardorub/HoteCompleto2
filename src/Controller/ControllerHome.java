/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

// ControllerHome.java
// Handles logic for the home screen after sign-in, primarily authentication for now.

// ControllerHome.java
// Handles logic for the home screen after sign-in, primarily authentication for now.

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ControllerHome {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public ControllerHome() {
        // Load database configuration
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("nbproject/project.properties")) {
            props.load(fis);
            this.dbUrl = props.getProperty("db.url");
            this.dbUser = props.getProperty("db.user");
            this.dbPassword = props.getProperty("db.password");
            // Class.forName(props.getProperty("db.driver")); // Ensure driver is loaded
        } catch (IOException e) {
            System.err.println("Error loading database configuration for ControllerHome: " + e.getMessage());
            this.dbUrl = null;
        }
        // catch (ClassNotFoundException e) {
        //     System.err.println("MySQL JDBC Driver not found for ControllerHome: " + e.getMessage());
        //     throw new RuntimeException("JDBC Driver not found for ControllerHome", e);
        // }
    }

    /**
     * Authenticates an admin user.
     *
     * @param username The username of the admin trying to sign in.
     * @param plainPassword The plain-text password provided.
     * @return True if authentication is successful, false otherwise.
     */
    public boolean authenticate(String username, String plainPassword) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot authenticate.");
            return false;
        }
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            System.err.println("Username or password cannot be empty for authentication.");
            return false;
        }

        String sql = "SELECT password_hash FROM admin WHERE username = ?";
        // Assumes table 'admin' and columns 'username', 'password_hash'

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedSaltAndHash = rs.getString("password_hash");
                    // Verify the password
                    if (Password.verifyPassword(plainPassword, storedSaltAndHash)) {
                        System.out.println("Authentication successful for user: " + username);
                        return true;
                    } else {
                        System.out.println("Authentication failed: Incorrect password for user: " + username);
                        return false;
                    }
                } else {
                    System.out.println("Authentication failed: User '" + username + "' not found.");
                    return false; // User not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication for user '" + username + "': " + e.getMessage());
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * Placeholder for displaying home information or navigating to a home screen.
     * This would be called after successful authentication.
     * @param username The username of the authenticated admin.
     */
    public void displayHomeScreen(String username) {
        // For now, just a print statement.
        // In a real application, this might open a new JFrame (e.g., AdminDashboardFrame)
        // or update the current UI.
        System.out.println("Welcome to the Admin Home Screen, " + username + "!");
        // Example:
        // AdminDashboardFrame dashboard = new AdminDashboardFrame(username);
        // dashboard.setVisible(true);
    }
    
    // Main method for testing ControllerHome independently (optional)
    public static void main(String[] args) {
        ControllerHome controller = new ControllerHome();

        // Ensure your MySQL server is running, the database/table exists,
        // and an admin user is registered (e.g., using ControllerRegistro's test).
        // And project.properties has correct db.url, db.user, db.password.
        // And mysql-connector JAR is in the classpath.

        if (controller.dbUrl == null) {
            System.err.println("DB Config not loaded for ControllerHome. Tests will fail.");
            return;
        }

        // Test authentication
        // String testUser = "newadmin"; // A user you registered via ControllerRegistro
        // String testPass = "securepassword123";
        // String wrongPass = "wrongpassword";

        // System.out.println("Attempting authentication for " + testUser + " with correct password...");
        // boolean success = controller.authenticate(testUser, testPass);
        // if (success) {
        //     System.out.println("Test authentication successful for " + testUser);
        //     controller.displayHomeScreen(testUser);
        // } else {
        //     System.out.println("Test authentication FAILED for " + testUser);
        // }

        // System.out.println("\nAttempting authentication for " + testUser + " with WRONG password...");
        // boolean fail = controller.authenticate(testUser, wrongPass);
        // if (!fail) {
        //     System.out.println("Test authentication with wrong password correctly failed for " + testUser);
        // } else {
        //     System.out.println("Test authentication with wrong password INCORRECTLY succeeded for " + testUser);
        // }
        
        // System.out.println("\nAttempting authentication for non_existent_user with some password...");
        // boolean nonExistent = controller.authenticate("non_existent_user", "somepassword");
        // if (!nonExistent) {
        //     System.out.println("Test authentication for non-existent user correctly failed.");
        // } else {
        //     System.out.println("Test authentication for non-existent user INCORRECTLY succeeded.");
        // }
         System.out.println("ControllerHome main method for testing. Uncomment internal tests.");

    }
}


