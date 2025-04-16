package com.newsaggregator.controller;

import com.newsaggregator.Main;
import com.newsaggregator.service.UserService;
import com.newsaggregator.view.LoginView;
import com.newsaggregator.view.NewsView;
import com.newsaggregator.view.RegisterView;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.util.regex.Pattern;

/**
 * Controller for handling registration operations and events.
 */
public class RegisterController {
    private final RegisterView registerView;
    private final UserService userService;
    
    // Regular expression for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * Constructor for RegisterController.
     *
     * @param registerView the register view
     */
    public RegisterController(RegisterView registerView) {
        this.registerView = registerView;
        this.userService = new UserService();
        
        // Initialize event handlers
        initializeHandlers();
    }

    /**
     * Initializes the event handlers for the register view.
     */
    private void initializeHandlers() {
        // Register button click event
        registerView.getRegisterButton().setOnAction(event -> handleRegistration());
        
        // Back to login button click event
        registerView.getBackToLoginButton().setOnAction(event -> navigateToLogin());
        
        // Handle Enter key press in password confirmation field
        registerView.getConfirmPasswordField().setOnAction(event -> handleRegistration());
    }

    /**
     * Handles the registration button click event.
     */
    private void handleRegistration() {
        try {
            System.out.println("Register button clicked");
            String username = registerView.getUsernameField().getText().trim();
            String email = registerView.getEmailField().getText().trim();
            String password = registerView.getPasswordField().getText();
            String confirmPassword = registerView.getConfirmPasswordField().getText();
            
            System.out.println("Registration attempt with username: " + username + ", email: " + email);
            
            // Validate input
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                System.out.println("Registration validation failed: Empty fields");
                showAlert(AlertType.ERROR, "Registration Error", "All fields are required");
                return;
            }
            
            if (username.length() < 4) {
                System.out.println("Registration validation failed: Username too short");
                showAlert(AlertType.ERROR, "Registration Error", "Username must be at least 4 characters long");
                return;
            }
            
            if (!isValidEmail(email)) {
                System.out.println("Registration validation failed: Invalid email format");
                showAlert(AlertType.ERROR, "Registration Error", "Please enter a valid email address");
                return;
            }
            
            if (password.length() < 6) {
                System.out.println("Registration validation failed: Password too short");
                showAlert(AlertType.ERROR, "Registration Error", "Password must be at least 6 characters long");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                System.out.println("Registration validation failed: Passwords don't match");
                showAlert(AlertType.ERROR, "Registration Error", "Passwords do not match");
                return;
            }
            
            // Attempt to register
            System.out.println("Attempting to register user: " + username);
            boolean success = userService.register(username, email, password, false);
            
            if (success) {
                System.out.println("Registration successful for user: " + username);
                // Navigate to the main news view
                navigateToNewsView();
            } else {
                System.out.println("Registration failed: Username or email already exists");
                showAlert(AlertType.ERROR, "Registration Failed", "Username or email already exists");
            }
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Registration Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Validates an email address.
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Navigates to the login view.
     */
    private void navigateToLogin() {
        LoginView loginView = new LoginView();
        new LoginController(loginView);
        
        Stage stage = Main.getPrimaryStage();
        Scene scene = new Scene(loginView.getRoot(), stage.getScene().getWidth(), stage.getScene().getHeight());
        scene.getStylesheets().add(getClass().getResource("/com/newsaggregator/css/styles.css").toExternalForm());
        
        stage.setScene(scene);
    }

    /**
     * Navigates to the main news view.
     */
    private void navigateToNewsView() {
        NewsView newsView = new NewsView(userService);
        new NewsController(newsView, userService);
        
        Stage stage = Main.getPrimaryStage();
        Scene scene = new Scene(newsView.getRoot(), stage.getScene().getWidth(), stage.getScene().getHeight());
        scene.getStylesheets().add(getClass().getResource("/com/newsaggregator/css/styles.css").toExternalForm());
        
        stage.setScene(scene);
    }

    /**
     * Shows an alert dialog.
     *
     * @param alertType  the type of alert
     * @param title      the alert title
     * @param message    the alert message
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
