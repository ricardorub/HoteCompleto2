
package Controller;

import General.Admin; // Import the Admin class
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ControllerRegistro {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbDriver;

    public ControllerRegistro() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("nbproject/project.properties")) {
            props.load(fis);
            this.dbUrl = props.getProperty("db.url");
            this.dbUser = props.getProperty("db.user");
            this.dbPassword = props.getProperty("db.password");
            this.dbDriver = props.getProperty("db.driver");

            if (this.dbDriver != null && !this.dbDriver.isEmpty()) {
                Class.forName(this.dbDriver);
            } else {
                System.err.println("Database driver not specified in project.properties.");
                 this.dbUrl = null; // Prevent further operations if driver is missing
            }
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            this.dbUrl = null; 
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            this.dbUrl = null; 
            // Consider re-throwing as a RuntimeException if this is a critical failure
            // throw new RuntimeException("JDBC Driver not found, application cannot start.", e);
        }
    }

    /**
     * Registers a new admin user.
     * Stores password in plain text.
     *
     * @param username The desired username for the new admin.
     * @param plainPassword The plain-text password for the new admin.
     * @param email The email for the new admin.
     * @param securityQuestion The security question for the new admin.
     * @param answer The answer to the security question.
     * @return True if registration is successful, false otherwise.
     */
    public boolean registrarAdmin(String username, String plainPassword, String email, String securityQuestion, String answer) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot register admin.");
            return false;
        }
        if (username == null || username.trim().isEmpty() || 
            plainPassword == null || plainPassword.isEmpty() ||
            email == null || email.trim().isEmpty()) {
            System.err.println("Username, password, or email cannot be empty.");
            // Optionally, add more specific validation for email format, password complexity (if desired later)
            return false;
        }

        // 1. Check if user (username or email) already exists
        if (adminExists(username, email)) {
            // adminExists method will print the specific reason (username or email)
            return false;
        }

        // 2. Store the new admin in the database (password in plain text)
        // The column name in the database for the password is 'password_hash', 
        // but we are storing plain text in it as per user request.
        // It should ideally be named 'password' if storing plain text.
        String sql = "INSERT INTO admin (username, password_hash, email, security_question, answer, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, plainPassword); // Storing plain password
            pstmt.setString(3, email);
            pstmt.setString(4, securityQuestion);
            pstmt.setString(5, answer);
            pstmt.setString(6, "approved"); // Default status

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
            // e.printStackTrace(); 
            return false;
        }
    }

    /**
     * Checks if an admin with the given username or email already exists in the database.
     * @param username The username to check.
     * @param email The email to check.
     * @return true if an admin with that username or email exists, false otherwise.
     */
    private boolean adminExists(String username, String email) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot check if admin exists.");
            return true; // Safer to assume exists if DB is not configured
        }
        // Check for username OR email.
        String sql = "SELECT id FROM admin WHERE username = ? OR email = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Could add logic here to see if it was username or email if needed for a more specific message
                    System.err.println("Admin user with username '" + username + "' or email '" + email + "' already exists.");
                    return true; 
                }
                return false; // No user found with that username or email
            }
        } catch (SQLException e) {
            System.err.println("Database error checking if admin '" + username + "/" + email + "' exists: " + e.getMessage());
            return true; // Safer to assume exists on DB error to prevent duplicates
        }
    }
    
    // Main method for testing ControllerRegistro independently
    public static void main(String[] args) {
        ControllerRegistro controller = new ControllerRegistro();
        
        if (controller.dbUrl == null) {
             System.err.println("DB Config not loaded. Tests will fail. Check nbproject/project.properties path and content, and MySQL Connector JAR.");
             return;
        }
        System.out.println("DB URL: " + controller.dbUrl);
        System.out.println("DB User: " + controller.dbUser);
        // System.out.println("DB Password: " + controller.dbPassword); // Don't print password generally
        System.out.println("DB Driver: " + controller.dbDriver);


        // Test Case 1: Register a new admin
        System.out.println("\n--- Test Case 1: Register New Admin ---");
        boolean success1 = controller.registrarAdmin("testUser1", "password123", "test1@example.com", "Fav color?", "Blue");
        if (success1) {
            System.out.println("Test Case 1: Registration successful for testUser1.");
        } else {
            System.out.println("Test Case 1: Registration failed for testUser1.");
        }

        // Test Case 2: Try to register the same admin (should fail due to username)
        System.out.println("\n--- Test Case 2: Register Duplicate Username ---");
        boolean success2 = controller.registrarAdmin("testUser1", "password456", "test2@example.com", "Fav pet?", "Dog");
        if (!success2) {
            System.out.println("Test Case 2: Correctly failed to register duplicate username 'testUser1'.");
        } else {
            System.out.println("Test Case 2: INCORRECTLY registered duplicate username 'testUser1'.");
        }

        // Test Case 3: Try to register with existing email (should fail due to email)
        System.out.println("\n--- Test Case 3: Register Duplicate Email ---");
        boolean success3 = controller.registrarAdmin("testUser3", "password789", "test1@example.com", "Fav food?", "Pizza");
        if (!success3) {
            System.out.println("Test Case 3: Correctly failed to register duplicate email 'test1@example.com'.");
        } else {
            System.out.println("Test Case 3: INCORRECTLY registered duplicate email 'test1@example.com'.");
        }
        
        // Test Case 4: Register another new admin
        System.out.println("\n--- Test Case 4: Register Second New Admin ---");
        boolean success4 = controller.registrarAdmin("testUser2", "securePass", "test2unique@example.com", "City of birth?", "New York");
        if (success4) {
            System.out.println("Test Case 4: Registration successful for testUser2.");
        } else {
            System.out.println("Test Case 4: Registration failed for testUser2.");
        }
        
        // Test Case 5: Register with empty username (should fail)
        System.out.println("\n--- Test Case 5: Register Empty Username ---");
        boolean success5 = controller.registrarAdmin("", "password", "emptyuser@example.com", "Q", "A");
        if (!success5) {
            System.out.println("Test Case 5: Correctly failed to register empty username.");
        } else {
            System.out.println("Test Case 5: INCORRECTLY registered empty username.");
        }

        // Note: To run these tests effectively, you might want to clear the 'admin' table before each full test run
        // or use unique usernames/emails for each test that expects success.
        System.out.println("\nControllerRegistro main method testing complete. Check database for results.");
        System.out.println("Remember to have your MySQL server running and the 'hotel' database with 'admin' table created.");
        System.out.println("The 'admin' table should have columns: id, username, password_hash (stores plain pass), email, security_question, answer, status.");
    }
}



