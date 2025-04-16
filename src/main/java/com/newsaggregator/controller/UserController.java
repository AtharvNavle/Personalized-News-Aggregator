package com.newsaggregator.controller;

import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.service.UserService;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Controller for handling user profile operations.
 */
public class UserController {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    
    private final UserService userService;

    /**
     * Constructor for UserController.
     *
     * @param userService the user service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Shows the user profile dialog.
     */
    public void showProfileDialog() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to view your profile");
            return;
        }
        
        // Create a dialog for user profile
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("User Profile");
        dialog.setHeaderText("Your Profile Information");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        // Username field
        javafx.scene.control.TextField usernameField = new javafx.scene.control.TextField(currentUser.getUsername());
        grid.add(new javafx.scene.control.Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        
        // Email field
        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField(currentUser.getEmail());
        grid.add(new javafx.scene.control.Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        
        // Language preference
        javafx.scene.control.ComboBox<String> languageCombo = new javafx.scene.control.ComboBox<>();
        languageCombo.getItems().addAll("en", "fr", "de", "es", "it", "ru");
        languageCombo.setValue(currentUser.getPreferredLanguage());
        grid.add(new javafx.scene.control.Label("Preferred Language:"), 0, 2);
        grid.add(languageCombo, 1, 2);
        
        // Country preference
        javafx.scene.control.ComboBox<String> countryCombo = new javafx.scene.control.ComboBox<>();
        countryCombo.getItems().addAll("us", "gb", "ca", "au", "fr", "de");
        countryCombo.setValue(currentUser.getPreferredCountry());
        grid.add(new javafx.scene.control.Label("Preferred Country:"), 0, 3);
        grid.add(countryCombo, 1, 3);
        
        // Category preferences
        grid.add(new javafx.scene.control.Label("Preferred Categories:"), 0, 4);
        
        javafx.scene.layout.VBox categoriesBox = new javafx.scene.layout.VBox(5);
        Set<Category> userCategories = currentUser.getPreferredCategories();
        
        for (Category category : Category.values()) {
            javafx.scene.control.CheckBox categoryCheck = new javafx.scene.control.CheckBox(category.getDisplayName());
            categoryCheck.setSelected(userCategories.contains(category));
            categoriesBox.getChildren().add(categoryCheck);
        }
        
        grid.add(categoriesBox, 1, 4);
        
        // Add change password button
        javafx.scene.control.Button changePasswordButton = new javafx.scene.control.Button("Change Password");
        changePasswordButton.setOnAction(event -> showChangePasswordDialog());
        grid.add(changePasswordButton, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Collect selected categories
            Set<Category> selectedCategories = new HashSet<>();
            for (int i = 0; i < categoriesBox.getChildren().size(); i++) {
                javafx.scene.control.CheckBox check = (javafx.scene.control.CheckBox) categoriesBox.getChildren().get(i);
                if (check.isSelected()) {
                    selectedCategories.add(Category.values()[i]);
                }
            }
            
            // Update user profile
            boolean updated = userService.updateProfile(
                    usernameField.getText(),
                    emailField.getText(),
                    languageCombo.getValue(),
                    countryCombo.getValue(),
                    selectedCategories
            );
            
            if (updated) {
                showAlert(AlertType.INFORMATION, "Profile Updated", "Your profile has been updated successfully");
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Failed to update profile. Username or email may already be in use.");
            }
        }
    }

    /**
     * Shows the change password dialog.
     */
    public void showChangePasswordDialog() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to change your password");
            return;
        }
        
        // Create a dialog for changing password
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current and new password");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        
        grid.add(new javafx.scene.control.Label("Current Password:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new javafx.scene.control.Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new javafx.scene.control.Label("Confirm New Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the current password field by default
        javafx.application.Platform.runLater(currentPasswordField::requestFocus);
        
        // Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            // Validate input
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "All fields are required");
                return;
            }
            
            if (newPassword.length() < 6) {
                showAlert(AlertType.ERROR, "Input Error", "New password must be at least 6 characters long");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showAlert(AlertType.ERROR, "Input Error", "New passwords do not match");
                return;
            }
            
            // Update password
            boolean updated = userService.updatePassword(currentPassword, newPassword);
            
            if (updated) {
                showAlert(AlertType.INFORMATION, "Password Updated", "Your password has been updated successfully");
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Failed to update password. Current password may be incorrect.");
            }
        }
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
