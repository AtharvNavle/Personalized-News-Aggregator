package com.newsaggregator.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * View for the login screen.
 */
public class LoginView {
    private BorderPane root;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;

    /**
     * Constructor for LoginView.
     */
    public LoginView() {
        createView();
    }

    /**
     * Creates the login view.
     */
    private void createView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("login-view");

        // Container for the login form
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);
        formContainer.setPadding(new Insets(30));
        formContainer.getStyleClass().add("form-container");

        // App title and logo
        Label titleLabel = new Label("News Aggregator");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("app-title");

        Label subtitleLabel = new Label("Stay informed with personalized news");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.getStyleClass().add("app-subtitle");

        // Creating a placeholder for logo (using an SVG would be ideal in a real app)
        Label logoPlaceholder = new Label("📰");
        logoPlaceholder.setFont(Font.font("System", FontWeight.BOLD, 48));
        logoPlaceholder.setAlignment(Pos.CENTER);
        logoPlaceholder.getStyleClass().add("app-logo");

        // Header container
        VBox headerContainer = new VBox(10, logoPlaceholder, titleLabel, subtitleLabel);
        headerContainer.setAlignment(Pos.CENTER);
        
        // Login form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        // Username field
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        formGrid.add(passwordLabel, 0, 1);
        formGrid.add(passwordField, 1, 1);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(100);

        registerButton = new Button("Register");
        registerButton.getStyleClass().add("secondary-button");
        registerButton.setPrefWidth(100);

        buttonBox.getChildren().addAll(loginButton, registerButton);

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
     * Gets the password field.
     *
     * @return the password field
     */
    public PasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Gets the login button.
     *
     * @return the login button
     */
    public Button getLoginButton() {
        return loginButton;
    }

    /**
     * Gets the register button.
     *
     * @return the register button
     */
    public Button getRegisterButton() {
        return registerButton;
    }
}
