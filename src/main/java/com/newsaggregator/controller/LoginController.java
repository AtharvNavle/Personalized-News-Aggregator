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

/**
 * Controller for handling login operations and events.
 */
public class LoginController {
    private final LoginView loginView;
    private final UserService userService;

    /**
     * Constructor for LoginController.
     *
     * @param loginView the login view
     */
    public LoginController(LoginView loginView) {
        this.loginView = loginView;
        this.userService = new UserService();
        
        // Initialize event handlers
        initializeHandlers();
    }

    /**
     * Initializes the event handlers for the login view.
     */
    private void initializeHandlers() {
        // Login button click event
        loginView.getLoginButton().setOnAction(event -> handleLogin());
        
        // Register button click event
        loginView.getRegisterButton().setOnAction(event -> navigateToRegister());
        
        // Handle Enter key press in password field
        loginView.getPasswordField().setOnAction(event -> handleLogin());
    }

    /**
     * Handles the login button click event.
     */
    private void handleLogin() {
        String username = loginView.getUsernameField().getText().trim();
        String password = loginView.getPasswordField().getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Error", "Username and password are required");
            return;
        }
        
        // Attempt to login
        boolean success = userService.login(username, password);
        
        if (success) {
            // Navigate to the main news view
            navigateToNewsView();
        } else {
            showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password");
            loginView.getPasswordField().clear();
        }
    }

    /**
     * Navigates to the registration view.
     */
    private void navigateToRegister() {
        RegisterView registerView = new RegisterView();
        new RegisterController(registerView);
        
        Stage stage = Main.getPrimaryStage();
        Scene scene = new Scene(registerView.getRoot(), stage.getScene().getWidth(), stage.getScene().getHeight());
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
