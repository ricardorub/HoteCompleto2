/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package General;

/**
 *
 * @author Lenovo
 */
// Admin.java
// Represents an administrator user

public class Admin {
    private int id;
    private String username;
    private String password; // Changed from hashedPassword to store plain text password
    private String email;
    private String securityQuestion;
    private String answer;
    private String status;

    // Constructor for creating a new admin before saving to DB (ID might not be known yet)
    public Admin(String username, String password, String email, String securityQuestion, String answer, String status) {
        this.username = username;
        this.password = password; // Storing plain text
        this.email = email;
        this.securityQuestion = securityQuestion;
        this.answer = answer;
        this.status = (status != null && !status.trim().isEmpty()) ? status : "approved"; // Default status if not provided
    }

    // Constructor for creating an admin object from DB data (ID is known)
    public Admin(int id, String username, String password, String email, String securityQuestion, String answer, String status) {
        this.id = id;
        this.username = username;
        this.password = password; // Storing plain text
        this.email = email;
        this.securityQuestion = securityQuestion;
        this.answer = answer;
        this.status = status;
    }
    
    // Minimal constructor for login attempts or other specific scenarios
    public Admin(String username, String password) {
        this.username = username;
        this.password = password; // Storing plain text
        this.email = ""; 
        this.securityQuestion = "";
        this.answer = "";
        this.status = "approved"; 
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // Renamed from getHashedPassword
    public String getPassword() { 
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getAnswer() {
        return answer;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) { 
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Renamed from setHashedPassword
    public void setPassword(String password) { 
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString method for debugging or logging (optional)
    @Override
    public String toString() {
        return "Admin{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", status='" + status + '\'' +
               // Password, answer, and securityQuestion are intentionally omitted for security.
               '}';
    }
}


