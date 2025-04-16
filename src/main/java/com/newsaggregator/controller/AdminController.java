package com.newsaggregator.controller;

import com.newsaggregator.Main;
import com.newsaggregator.model.User;
import com.newsaggregator.service.UserService;
import com.newsaggregator.view.AdminView;
import com.newsaggregator.view.NewsView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Controller for handling admin operations and events.
 */
public class AdminController {
    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());
    
    private final AdminView adminView;
    private final UserService userService;
    private final ExecutorService executorService;
    
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    /**
     * Constructor for AdminController.
     *
     * @param adminView   the admin view
     * @param userService the user service
     */
    public AdminController(AdminView adminView, UserService userService) {
        this.adminView = adminView;
        this.userService = userService;
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Initialize UI components
        initializeComponents();
        
        // Load users
        loadUsers();
    }

    /**
     * Initializes the UI components and event handlers.
     */
    private void initializeComponents() {
        // Set the table data
        adminView.getUsersTable().setItems(usersList);
        
        // Set up action buttons
        adminView.getBackButton().setOnAction(event -> navigateToNewsView());
        adminView.getRefreshButton().setOnAction(event -> loadUsers());
        adminView.getCreateUserButton().setOnAction(event -> showCreateUserDialog());
        
        // Set up table selection
        adminView.getUsersTable().getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateButtonStates(newValue));
        
        // Set up action handlers
        adminView.getEditUserButton().setOnAction(event -> editSelectedUser());
        adminView.getDeleteUserButton().setOnAction(event -> deleteSelectedUser());
        adminView.getToggleAdminButton().setOnAction(event -> toggleAdminStatus());
        
        // Initial button state
        updateButtonStates(null);
    }

    /**
     * Updates the state of the action buttons based on the selected user.
     *
     * @param selectedUser the currently selected user
     */
    private void updateButtonStates(User selectedUser) {
        boolean userSelected = selectedUser != null;
        boolean isCurrentUser = userSelected && selectedUser.getId() == userService.getCurrentUser().getId();
        
        adminView.getEditUserButton().setDisable(!userSelected);
        adminView.getDeleteUserButton().setDisable(!userSelected || isCurrentUser);
        adminView.getToggleAdminButton().setDisable(!userSelected || isCurrentUser);
    }

    /**
     * Loads all users into the table.
     */
    private void loadUsers() {
        adminView.getLoadingIndicator().setVisible(true);
        
        executorService.submit(() -> {
            List<User> users = userService.getAllUsers();
            
            Platform.runLater(() -> {
                usersList.setAll(users);
                adminView.getLoadingIndicator().setVisible(false);
            });
        });
    }

    /**
     * Shows a dialog to create a new user.
     */
    private void showCreateUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter user details");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        javafx.scene.control.CheckBox adminCheck = new javafx.scene.control.CheckBox("Admin");
        
        grid.add(new javafx.scene.control.Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new javafx.scene.control.Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(adminCheck, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the username field by default
        Platform.runLater(usernameField::requestFocus);
        
        // Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            boolean isAdmin = adminCheck.isSelected();
            
            // Validate input
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "All fields are required");
                return;
            }
            
            // Create the user
            boolean success = userService.register(username, email, password, isAdmin);
            
            if (success) {
                showAlert(AlertType.INFORMATION, "User Created", "User has been created successfully");
                loadUsers(); // Refresh the user list
            } else {
                showAlert(AlertType.ERROR, "Creation Failed", "Failed to create user. Username or email may already be in use.");
            }
        }
    }

    /**
     * Shows a dialog to edit the selected user.
     */
    private void editSelectedUser() {
        User selectedUser = adminView.getUsersTable().getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            return;
        }
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user details");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField usernameField = new TextField(selectedUser.getUsername());
        TextField emailField = new TextField(selectedUser.getEmail());
        javafx.scene.control.CheckBox adminCheck = new javafx.scene.control.CheckBox("Admin");
        adminCheck.setSelected(selectedUser.isAdmin());
        
        grid.add(new javafx.scene.control.Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(adminCheck, 1, 2);
        
        // Disable admin checkbox if editing current user
        if (selectedUser.getId() == userService.getCurrentUser().getId()) {
            adminCheck.setDisable(true);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        // Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            boolean isAdmin = adminCheck.isSelected();
            
            // Validate input
            if (username.isEmpty() || email.isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "All fields are required");
                return;
            }
            
            // Update the user
            selectedUser.setUsername(username);
            selectedUser.setEmail(email);
            
            // Only update admin status if not editing current user
            if (selectedUser.getId() != userService.getCurrentUser().getId()) {
                selectedUser.setAdmin(isAdmin);
            }
            
            // Save changes
            boolean updated = userService.updateProfile(
                    selectedUser.getUsername(),
                    selectedUser.getEmail(),
                    selectedUser.getPreferredLanguage(),
                    selectedUser.getPreferredCountry(),
                    selectedUser.getPreferredCategories()
            );
            
            if (updated) {
                showAlert(AlertType.INFORMATION, "User Updated", "User has been updated successfully");
                loadUsers(); // Refresh the user list
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Failed to update user. Username or email may already be in use.");
            }
        }
    }

    /**
     * Deletes the selected user.
     */
    private void deleteSelectedUser() {
        User selectedUser = adminView.getUsersTable().getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            return;
        }
        
        // Cannot delete current user
        if (selectedUser.getId() == userService.getCurrentUser().getId()) {
            showAlert(AlertType.WARNING, "Cannot Delete", "You cannot delete your own account");
            return;
        }
        
        // Confirm deletion
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete User: " + selectedUser.getUsername());
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = userService.deleteUser(selectedUser.getId());
            
            if (deleted) {
                showAlert(AlertType.INFORMATION, "User Deleted", "User has been deleted successfully");
                loadUsers(); // Refresh the user list
            } else {
                showAlert(AlertType.ERROR, "Deletion Failed", "Failed to delete user");
            }
        }
    }

    /**
     * Toggles the admin status of the selected user.
     */
    private void toggleAdminStatus() {
        User selectedUser = adminView.getUsersTable().getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            return;
        }
        
        // Cannot change admin status of current user
        if (selectedUser.getId() == userService.getCurrentUser().getId()) {
            showAlert(AlertType.WARNING, "Cannot Change", "You cannot change your own admin status");
            return;
        }
        
        boolean newStatus = !selectedUser.isAdmin();
        String action = newStatus ? "grant" : "revoke";
        
        // Confirm action
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Change Admin Status");
        alert.setHeaderText("Change Admin Status for: " + selectedUser.getUsername());
        alert.setContentText("Are you sure you want to " + action + " admin privileges for this user?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean updated = userService.updateUserAdminStatus(selectedUser.getId(), newStatus);
            
            if (updated) {
                showAlert(AlertType.INFORMATION, "Status Changed", "Admin status has been updated successfully");
                loadUsers(); // Refresh the user list
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Failed to update admin status");
            }
        }
    }

    /**
     * Navigates back to the news view.
     */
    private void navigateToNewsView() {
        NewsView newsView = new NewsView(userService);
        NewsController newsController = new NewsController(newsView, userService);
        
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

    /**
     * Cleans up resources when the controller is no longer needed.
     */
    public void cleanup() {
        executorService.shutdown();
    }
}
