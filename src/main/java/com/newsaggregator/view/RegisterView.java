package com.newsaggregator.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * View for the registration screen.
 */
public class RegisterView {
    private BorderPane root;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;
    private Button backToLoginButton;

    /**
     * Constructor for RegisterView.
     */
    public RegisterView() {
        createView();
    }

    /**
     * Creates the register view.
     */
    private void createView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("register-view");

        // Container for the registration form
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(450);
        formContainer.setPadding(new Insets(30));
        formContainer.getStyleClass().add("form-container");

        // App title
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("app-title");

        Label subtitleLabel = new Label("Join News Aggregator to personalize your news");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.getStyleClass().add("app-subtitle");

        // Logo placeholder
        Label logoPlaceholder = new Label("📝");
        logoPlaceholder.setFont(Font.font("System", FontWeight.BOLD, 48));
        logoPlaceholder.setAlignment(Pos.CENTER);
        logoPlaceholder.getStyleClass().add("app-logo");

        // Header container
        VBox headerContainer = new VBox(10, logoPlaceholder, titleLabel, subtitleLabel);
        headerContainer.setAlignment(Pos.CENTER);
        
        // Registration form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        // Username field
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Choose a username (min 4 characters)");
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);

        // Email field
        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        formGrid.add(emailLabel, 0, 1);
        formGrid.add(emailField, 1, 1);

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Create a password (min 6 characters)");
        formGrid.add(passwordLabel, 0, 2);
        formGrid.add(passwordField, 1, 2);

        // Confirm password field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        formGrid.add(confirmPasswordLabel, 0, 3);
        formGrid.add(confirmPasswordField, 1, 3);

        // Help text
        Label helpText = new Label("Password must be at least 6 characters long");
        helpText.setFont(Font.font("System", FontWeight.NORMAL, 12));
        helpText.getStyleClass().add("help-text");
        formGrid.add(helpText, 1, 4);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        registerButton = new Button("Register");
        registerButton.getStyleClass().add("primary-button");
        registerButton.setDefaultButton(true);
        registerButton.setPrefWidth(100);

        backToLoginButton = new Button("Back to Login");
        backToLoginButton.getStyleClass().add("secondary-button");
        backToLoginButton.setPrefWidth(100);

        buttonBox.getChildren().addAll(registerButton, backToLoginButton);

        // Build form container
        formContainer.getChildren().addAll(headerContainer, formGrid, buttonBox);

        // Center the form in the window
        VBox centerBox = new VBox(formContainer);
        centerBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(centerBox, Priority.ALWAYS);
        HBox.setHgrow(centerBox, Priority.ALWAYS);

        root.setCenter(centerBox);
        
        // Footer
        Label footerLabel = new Label("© 2023 News Aggregator | Powered by NewsAPI");
        footerLabel.setTextAlignment(TextAlignment.CENTER);
        footerLabel.getStyleClass().add("footer-text");
        
        HBox footerBox = new HBox(footerLabel);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(10, 0, 0, 0));
        
        root.setBottom(footerBox);
    }

    /**
     * Gets the root pane.
     *
     * @return the root pane
     */
    public BorderPane getRoot() {
        return root;
    }

    /**
     * Gets the username field.
     *
     * @return the username field
     */
    public TextField getUsernameField() {
        return usernameField;
    }

    /**
     * Gets the email field.
     *
     * @return the email field
     */
    public TextField getEmailField() {
        return emailField;
    }

    /**
     * Gets the password field.
     *
     * @return the password field
     */
    public PasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Gets the confirm password field.
     *
     * @return the confirm password field
     */
    public PasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    /**
     * Gets the register button.
     *
     * @return the register button
     */
    public Button getRegisterButton() {
        return registerButton;
    }

    /**
     * Gets the back to login button.
     *
     * @return the back to login button
     */
    public Button getBackToLoginButton() {
        return backToLoginButton;
    }
}
