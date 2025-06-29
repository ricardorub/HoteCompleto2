
package Controller;

import General.Admin; // Import the Admin class
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
    private String dbDriver;

    public ControllerHome() {
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
                System.err.println("Database driver not specified in project.properties for ControllerHome.");
                this.dbUrl = null; // Prevent further operations
            }
        } catch (IOException e) {
            System.err.println("Error loading database configuration for ControllerHome: " + e.getMessage());
            this.dbUrl = null;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found for ControllerHome: " + e.getMessage());
            this.dbUrl = null;
            // throw new RuntimeException("JDBC Driver not found for ControllerHome", e);
        }
    }

    /**
     * Authenticates an admin user by email or username, checking plain text password.
     *
     * @param usernameOrEmail The username or email of the admin trying to sign in.
     * @param plainPassword The plain-text password provided.
     * @return Admin object if authentication is successful and user is approved, null otherwise.
     */
    public Admin authenticate(String usernameOrEmail, String plainPassword) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot authenticate.");
            return null;
        }
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() || 
            plainPassword == null || plainPassword.isEmpty()) { // Password can be empty if that's a valid state, but usually not for login
            System.err.println("Username/Email or password cannot be empty for authentication.");
            return null;
        }

        // The column name in DB is 'password_hash', but it stores plain text password
        String sql = "SELECT id, username, password_hash, email, security_question, answer, status FROM admin WHERE (username = ? OR email = ?) AND status = 'approved'";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password_hash");
                    // Direct string comparison for plain text password
                    if (plainPassword.equals(storedPassword)) {
                        String status = rs.getString("status");
                        if ("approved".equalsIgnoreCase(status)) {
                            System.out.println("Authentication successful for user: " + usernameOrEmail);
                            // Return a full Admin object
                            return new Admin(
                                rs.getInt("id"),
                                rs.getString("username"),
                                storedPassword, // or plainPassword, since they match
                                rs.getString("email"),
                                rs.getString("security_question"),
                                rs.getString("answer"),
                                status
                            );
                        } else {
                             System.out.println("Authentication failed: User '" + usernameOrEmail + "' is not approved.");
                             return null; // User found but not approved
                        }
                    } else {
                        System.out.println("Authentication failed: Incorrect password for user: " + usernameOrEmail);
                        return null; // Incorrect password
                    }
                } else {
                    System.out.println("Authentication failed: User '" + usernameOrEmail + "' not found or not approved.");
                    return null; // User not found or not approved
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication for user '" + usernameOrEmail + "': " + e.getMessage());
            // e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves admin details (excluding password) based on username or email.
     * Useful for password recovery (to get security question).
     *
     * @param usernameOrEmail The username or email of the admin.
     * @return Admin object if found, null otherwise.
     */
    public Admin recuperarAdmin(String usernameOrEmail) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot retrieve admin.");
            return null;
        }
         if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()){
            System.err.println("Username/Email cannot be empty for admin retrieval.");
            return null;
        }

        String sql = "SELECT id, username, email, security_question, answer, status FROM admin WHERE username = ? OR email = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Create Admin object without password for security question retrieval
                    return new Admin(
                        rs.getInt("id"),
                        rs.getString("username"),
                        null, // Password not retrieved here for security question flow
                        rs.getString("email"),
                        rs.getString("security_question"),
                        rs.getString("answer"), // Retrieving answer to verify, though UI should only show question
                        rs.getString("status")
                    );
                } else {
                    System.out.println("Admin user '" + usernameOrEmail + "' not found for recovery.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving admin '" + usernameOrEmail + "': " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifies the security answer for a given admin (identified by username/email).
     *
     * @param usernameOrEmail The username or email of the admin.
     * @param providedAnswer The answer provided by the user.
     * @return true if the answer is correct, false otherwise.
     */
    public boolean verificarRespuestaSeguridad(String usernameOrEmail, String providedAnswer) {
        Admin admin = recuperarAdmin(usernameOrEmail); // This already handles dbUrl null check
        if (admin != null) {
            if (admin.getAnswer() == null && (providedAnswer == null || providedAnswer.trim().isEmpty())) {
                 System.out.println("Security answer verification: Stored answer is null/empty, and provided is also null/empty. Considered a match for this case.");
                 return true; // Both are null/empty, consider it a match for robustness if SQ was optional
            }
            if (admin.getAnswer() != null && admin.getAnswer().equals(providedAnswer)) { // Case-sensitive comparison
                System.out.println("Security answer verified for " + usernameOrEmail);
                return true;
            } else {
                System.out.println("Security answer verification failed for " + usernameOrEmail + ". Provided: '"+ providedAnswer + "', Expected: '"+ admin.getAnswer() +"'");
                return false;
            }
        }
        return false; // Admin not found
    }


    /**
     * Updates the password for a given admin (identified by username or email).
     * Stores the new password in plain text.
     *
     * @param usernameOrEmail The username or email of the admin whose password is to be updated.
     * @param newPlainPassword The new plain-text password.
     * @return true if the update was successful, false otherwise.
     */
    public boolean actualizarPassword(String usernameOrEmail, String newPlainPassword) {
        if (dbUrl == null) {
            System.err.println("Database configuration not loaded. Cannot update password.");
            return false;
        }
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() ||
            newPlainPassword == null || newPlainPassword.isEmpty()) { // Usually new password shouldn't be empty
            System.err.println("Username/Email or new password cannot be empty for password update.");
            return false;
        }

        // Using 'password_hash' column name for plain text password storage
        String sql = "UPDATE admin SET password_hash = ? WHERE username = ? OR email = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPlainPassword); // Storing new plain password
            pstmt.setString(2, usernameOrEmail);
            pstmt.setString(3, usernameOrEmail);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Password updated successfully for user: " + usernameOrEmail);
                return true;
            } else {
                System.err.println("Password update failed for user '" + usernameOrEmail + "' (user not found or no change made).");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error during password update for '" + usernameOrEmail + "': " + e.getMessage());
            return false;
        }
    }
    
    // Main method for testing ControllerHome independently
    public static void main(String[] args) {
        ControllerHome controllerHome = new ControllerHome();
        ControllerRegistro controllerRegistro = new ControllerRegistro(); // For setting up test data

        if (controllerHome.dbUrl == null || controllerRegistro.dbUrl == null) {
            System.err.println("DB Config not loaded. Tests will fail. Check nbproject/project.properties, MySQL Connector JAR, and ensure DB server is running.");
            return;
        }
        
        // It's good practice to ensure a clean state or use unique data for tests.
        // For simplicity, we'll register users needed for tests if they don't exist.
        // Assumes 'admin' table exists as per previous steps.
        System.out.println("--- Setting up test users (if they don't exist) ---");
        controllerRegistro.registrarAdmin("authTestUser", "authPass123", "auth@example.com", "Fav Movie?", "Matrix");
        controllerRegistro.registrarAdmin("recoveryTestUser", "recoveryPass", "recovery@example.com", "Fav Book?", "Dune");
        controllerRegistro.registrarAdmin("unapprovedUser", "unapprovedPass", "unapproved@example.com", "Q", "A");
        // Manually set unapprovedUser to 'pending' or something other than 'approved' in DB for a full test,
        // or add a method to ControllerAdmin (if one existed) to change status.
        // For now, we'll assume registrarAdmin makes them 'approved'. We can test the 'approved' check.

        // Test Case 1: Successful Authentication (username)
        System.out.println("\n--- Test Case 1: Successful Authentication (username) ---");
        Admin admin1 = controllerHome.authenticate("authTestUser", "authPass123");
        if (admin1 != null && "authTestUser".equals(admin1.getUsername())) {
            System.out.println("Test Case 1: SUCCESS - Authenticated: " + admin1.getUsername());
        } else {
            System.out.println("Test Case 1: FAIL - Authentication failed for authTestUser");
        }

        // Test Case 2: Successful Authentication (email)
        System.out.println("\n--- Test Case 2: Successful Authentication (email) ---");
        Admin admin2 = controllerHome.authenticate("auth@example.com", "authPass123");
        if (admin2 != null && "auth@example.com".equals(admin2.getEmail())) {
            System.out.println("Test Case 2: SUCCESS - Authenticated: " + admin2.getEmail());
        } else {
            System.out.println("Test Case 2: FAIL - Authentication failed for auth@example.com");
        }
        
        // Test Case 3: Failed Authentication (wrong password)
        System.out.println("\n--- Test Case 3: Failed Authentication (wrong password) ---");
        Admin admin3 = controllerHome.authenticate("authTestUser", "wrongPassword");
        if (admin3 == null) {
            System.out.println("Test Case 3: SUCCESS - Correctly failed authentication for wrong password.");
        } else {
            System.out.println("Test Case 3: FAIL - Incorrectly authenticated with wrong password.");
        }

        // Test Case 4: Failed Authentication (user not found)
        System.out.println("\n--- Test Case 4: Failed Authentication (user not found) ---");
        Admin admin4 = controllerHome.authenticate("nonExistentUser", "anyPassword");
        if (admin4 == null) {
            System.out.println("Test Case 4: SUCCESS - Correctly failed authentication for non-existent user.");
        } else {
            System.out.println("Test Case 4: FAIL - Incorrectly authenticated non-existent user.");
        }
        
        // Test Case 5: Retrieve Admin for Password Recovery
        System.out.println("\n--- Test Case 5: Retrieve Admin for Recovery ---");
        Admin recoveryAdmin = controllerHome.recuperarAdmin("recoveryTestUser");
        if (recoveryAdmin != null && "recoveryTestUser".equals(recoveryAdmin.getUsername())) {
            System.out.println("Test Case 5: SUCCESS - Retrieved admin: " + recoveryAdmin.getUsername() + ", SQ: " + recoveryAdmin.getSecurityQuestion());
        } else {
            System.out.println("Test Case 5: FAIL - Could not retrieve recoveryTestUser.");
        }
        
        // Test Case 6: Verify Correct Security Answer
        System.out.println("\n--- Test Case 6: Verify Correct Security Answer ---");
        boolean sqVerify1 = controllerHome.verificarRespuestaSeguridad("recoveryTestUser", "Dune");
        if (sqVerify1) {
            System.out.println("Test Case 6: SUCCESS - Security answer verified.");
        } else {
            System.out.println("Test Case 6: FAIL - Security answer verification failed for correct answer.");
        }

        // Test Case 7: Verify Incorrect Security Answer
        System.out.println("\n--- Test Case 7: Verify Incorrect Security Answer ---");
        boolean sqVerify2 = controllerHome.verificarRespuestaSeguridad("recoveryTestUser", "WrongAnswer");
        if (!sqVerify2) {
            System.out.println("Test Case 7: SUCCESS - Correctly failed verification for wrong security answer.");
        } else {
            System.out.println("Test Case 7: FAIL - Incorrectly verified wrong security answer.");
        }
        
        // Test Case 8: Update Password
        System.out.println("\n--- Test Case 8: Update Password ---");
        boolean passUpdate = controllerHome.actualizarPassword("recoveryTestUser", "newRecoveryPass123");
        if (passUpdate) {
            System.out.println("Test Case 8: SUCCESS - Password updated for recoveryTestUser.");
            // Try to authenticate with new password
            Admin reAuthAdmin = controllerHome.authenticate("recoveryTestUser", "newRecoveryPass123");
            if (reAuthAdmin != null) {
                System.out.println("Test Case 8: SUCCESS - Re-authentication with new password successful.");
            } else {
                System.out.println("Test Case 8: FAIL - Re-authentication with new password FAILED.");
            }
        } else {
            System.out.println("Test Case 8: FAIL - Password update failed for recoveryTestUser.");
        }
        
        // Test Case 9: Attempt to authenticate unapproved user (if status was changed manually in DB)
        // This test relies on 'unapprovedUser' having a status other than 'approved'.
        // If registrarAdmin always sets to 'approved', this test would need manual DB intervention or a status update method.
        System.out.println("\n--- Test Case 9: Authenticate Unapproved User (requires manual status change in DB or an update status method) ---");
        // Manually update 'unapprovedUser' status to 'pending' in your DB for this test.
        // E.g., UPDATE admin SET status = 'pending' WHERE username = 'unapprovedUser';
        Admin admin9 = controllerHome.authenticate("unapprovedUser", "unapprovedPass");
        if (admin9 == null) {
            System.out.println("Test Case 9: SUCCESS - Correctly failed to authenticate user 'unapprovedUser' (assuming status is not 'approved').");
        } else {
            System.out.println("Test Case 9: FAIL - Authenticated 'unapprovedUser'. Check status in DB; it should not be 'approved'.");
        }


        System.out.println("\nControllerHome main method testing complete.");
        System.out.println("Ensure your MySQL server is running, 'hotel' DB and 'admin' table are set up, and MySQL Connector JAR is in classpath.");
    }
}




