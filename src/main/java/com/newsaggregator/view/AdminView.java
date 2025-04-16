package com.newsaggregator.view;

import com.newsaggregator.model.User;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * View for the admin dashboard.
 */
public class AdminView {
    private BorderPane root;
    private TableView<User> usersTable;
    private Button backButton;
    private Button createUserButton;
    private Button editUserButton;
    private Button deleteUserButton;
    private Button toggleAdminButton;
    private Button refreshButton;
    private ProgressIndicator loadingIndicator;

    /**
     * Constructor for AdminView.
     */
    public AdminView() {
        createView();
    }

    /**
     * Creates the admin view.
     */
    private void createView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("admin-view");

        // Header
        Label titleLabel = new Label("Admin Dashboard");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));

        // Back button
        backButton = new Button("Back to News");
        backButton.getStyleClass().add("action-button");

        HBox headerBox = new HBox(10, backButton, titleLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        root.setTop(headerBox);

        // Users table
        usersTable = new TableView<>();
        usersTable.setPlaceholder(new Label("No users found"));
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ID column
        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(50);

        // Username column
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setPrefWidth(150);

        // Email column
        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(200);

        // Admin column
        TableColumn<User, Boolean> adminColumn = new TableColumn<>("Admin");
        adminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));
        adminColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });
        adminColumn.setMaxWidth(80);

        usersTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, adminColumn);
        usersTable.setItems(FXCollections.observableArrayList());

        // Action buttons
        createUserButton = new Button("Create User");
        createUserButton.getStyleClass().add("action-button");
        
        editUserButton = new Button("Edit User");
        editUserButton.getStyleClass().add("action-button");
        
        deleteUserButton = new Button("Delete User");
        deleteUserButton.getStyleClass().add("action-button");
        deleteUserButton.getStyleClass().add("delete-button");
        
        toggleAdminButton = new Button("Toggle Admin");
        toggleAdminButton.getStyleClass().add("action-button");
        
        refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("action-button");
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(24, 24);

        HBox buttonBox = new HBox(10, createUserButton, editUserButton, deleteUserButton, 
                toggleAdminButton, refreshButton, loadingIndicator);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        VBox centerBox = new VBox(10, buttonBox, usersTable);
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        root.setCenter(centerBox);
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
     * Gets the users table.
     *
     * @return the users table
     */
    public TableView<User> getUsersTable() {
        return usersTable;
    }

    /**
     * Gets the back button.
     *
     * @return the back button
     */
    public Button getBackButton() {
        return backButton;
    }

    /**
     * Gets the create user button.
     *
     * @return the create user button
     */
    public Button getCreateUserButton() {
        return createUserButton;
    }

    /**
     * Gets the edit user button.
     *
     * @return the edit user button
     */
    public Button getEditUserButton() {
        return editUserButton;
    }

    /**
     * Gets the delete user button.
     *
     * @return the delete user button
     */
    public Button getDeleteUserButton() {
        return deleteUserButton;
    }

    /**
     * Gets the toggle admin button.
     *
     * @return the toggle admin button
     */
    public Button getToggleAdminButton() {
        return toggleAdminButton;
    }

    /**
     * Gets the refresh button.
     *
     * @return the refresh button
     */
    public Button getRefreshButton() {
        return refreshButton;
    }

    /**
     * Gets the loading indicator.
     *
     * @return the loading indicator
     */
    public ProgressIndicator getLoadingIndicator() {
        return loadingIndicator;
    }
}
