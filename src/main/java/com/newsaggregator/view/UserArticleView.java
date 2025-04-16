package com.newsaggregator.view;

import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserCreatedArticle;

import java.time.format.DateTimeFormatter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.scene.Node;

/**
 * View for the user-created articles section.
 */
public class UserArticleView {
    private BorderPane root;
    private VBox articlesContainer;
    private ScrollPane articlesScrollPane;
    private Button createArticleButton;
    private Button myArticlesButton;
    private Button allArticlesButton;
    private Button backButton;
    private ComboBox<Category> categoryFilterComboBox;
    private Label headerLabel;
    private User currentUser;
    private File selectedImageFile;
    private ImageView previewImageView;
    private boolean isDarkTheme = false;
    
    /**
     * Constructor.
     * 
     * @param user the current user
     */
    public UserArticleView(User user) {
        this.currentUser = user;
        initialize();
    }
    
    /**
     * Initializes the view.
     */
    private void initialize() {
        root = new BorderPane();
        root.getStyleClass().add("main-container");
        
        // Create header with title and navigation buttons
        VBox header = createHeader();
        root.setTop(header);
        
        // Create articles container
        articlesContainer = new VBox(10);
        articlesContainer.setPadding(new Insets(15));
        
        articlesScrollPane = new ScrollPane(articlesContainer);
        articlesScrollPane.setFitToWidth(true);
        articlesScrollPane.getStyleClass().add("article-scroll-pane");
        
        root.setCenter(articlesScrollPane);
    }
    
    /**
     * Creates the header section.
     * 
     * @return the header VBox
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15));
        header.getStyleClass().add("header");
        
        headerLabel = new Label("User-Created Articles");
        headerLabel.getStyleClass().add("header-title");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        // Navigation buttons
        HBox navButtons = new HBox(10);
        navButtons.setAlignment(Pos.CENTER_LEFT);
        
        backButton = new Button("Back to News");
        backButton.getStyleClass().add("header-button");
        
        createArticleButton = new Button("Create New Article");
        createArticleButton.getStyleClass().add("header-button");
        
        myArticlesButton = new Button("My Articles");
        myArticlesButton.getStyleClass().add("header-button");
        
        allArticlesButton = new Button("All Articles");
        allArticlesButton.getStyleClass().add("header-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Category filter
        Label categoryFilterLabel = new Label("Filter by Category:");
        categoryFilterLabel.getStyleClass().add("filter-label");
        
        categoryFilterComboBox = new ComboBox<>();
        categoryFilterComboBox.getItems().add(null); // Add "All" option
        categoryFilterComboBox.getItems().addAll(Category.values());
        categoryFilterComboBox.setPromptText("All Categories");
        categoryFilterComboBox.getStyleClass().add("category-combo-box");
        
        navButtons.getChildren().addAll(
                backButton, 
                createArticleButton, 
                myArticlesButton, 
                allArticlesButton,
                spacer,
                categoryFilterLabel,
                categoryFilterComboBox
        );
        
        header.getChildren().addAll(headerLabel, navButtons);
        return header;
    }
    
    /**
     * Creates the article creation form.
     * 
     * @return the form VBox
     */
    public VBox createArticleForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("article-form");
        
        Label formTitle = new Label("Create New Article");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        formTitle.getStyleClass().add("form-title");
        
        // Title field
        Label titleLabel = new Label("Title:");
        titleLabel.getStyleClass().add("form-label");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter article title");
        titleField.getStyleClass().add("form-field");
        
        // Description field
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.getStyleClass().add("form-label");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter a brief description or summary");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.getStyleClass().add("form-field");
        
        // Content field
        Label contentLabel = new Label("Content:");
        contentLabel.getStyleClass().add("form-label");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter the full article content");
        contentArea.setPrefRowCount(10);
        contentArea.getStyleClass().add("form-field");
        
        // Category selection
        Label categoryLabel = new Label("Category:");
        categoryLabel.getStyleClass().add("form-label");
        ComboBox<Category> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(Category.values());
        categoryComboBox.setPromptText("Select a category");
        categoryComboBox.getStyleClass().add("form-field");
        
        // Image upload
        Label imageLabel = new Label("Image:");
        imageLabel.getStyleClass().add("form-label");
        
        Button selectImageButton = new Button("Select Image");
        selectImageButton.getStyleClass().add("form-button");
        
        Label imagePathLabel = new Label("No image selected");
        imagePathLabel.getStyleClass().add("image-path-label");
        
        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        imageBox.getChildren().addAll(selectImageButton, imagePathLabel);
        
        // Image preview
        previewImageView = new ImageView();
        previewImageView.setFitHeight(150);
        previewImageView.setFitWidth(200);
        previewImageView.setPreserveRatio(true);
        previewImageView.getStyleClass().add("preview-image");
        previewImageView.setVisible(false);
        
        // Form buttons
        Button submitButton = new Button("Submit Article");
        submitButton.getStyleClass().add("submit-button");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("cancel-button");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, submitButton);
        
        // Add all components to the form
        form.getChildren().addAll(
                formTitle,
                titleLabel, titleField,
                descriptionLabel, descriptionArea,
                contentLabel, contentArea,
                categoryLabel, categoryComboBox,
                imageLabel, imageBox,
                previewImageView,
                buttonBox
        );
        
        // Handle image selection
        selectImageButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Article Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            Stage stage = (Stage) form.getScene().getWindow();
            selectedImageFile = fileChooser.showOpenDialog(stage);
            
            if (selectedImageFile != null) {
                try {
                    Image image = new Image(selectedImageFile.toURI().toString());
                    previewImageView.setImage(image);
                    previewImageView.setVisible(true);
                    imagePathLabel.setText(selectedImageFile.getName());
                } catch (Exception e) {
                    showAlert(AlertType.ERROR, "Error", "Failed to load image", e.getMessage());
                    selectedImageFile = null;
                }
            }
        });
        
        // Add event handlers
        cancelButton.setOnAction(event -> {
            selectedImageFile = null;
            articlesContainer.getChildren().clear();
            displayAllArticles(false);
        });
        
        return form;
    }
    
    /**
     * Creates a card view for a user-created article.
     * 
     * @param article the article to display
     * @param isAdmin whether the current user is an admin
     * @return the article card node
     */
    public Node createArticleCard(UserCreatedArticle article, boolean isAdmin) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("article-card");
        
        // Article title
        Label titleLabel = new Label(article.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("article-title");
        
        // Article metadata
        HBox metadataBox = new HBox(15);
        metadataBox.setAlignment(Pos.CENTER_LEFT);
        
        Label authorLabel = new Label("By: " + article.getAuthorUsername());
        authorLabel.getStyleClass().add("article-author");
        
        Label dateLabel = new Label("Posted: " + article.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.getStyleClass().add("article-date");
        
        Label categoryLabel = new Label("Category: " + article.getCategory().getDisplayName());
        categoryLabel.getStyleClass().add("article-category");
        
        Label statusLabel = new Label(article.isApproved() ? "Approved" : "Pending Approval");
        statusLabel.getStyleClass().add(article.isApproved() ? "status-approved" : "status-pending");
        
        metadataBox.getChildren().addAll(authorLabel, dateLabel, categoryLabel, statusLabel);
        
        // Article image (if available)
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(Paths.get(article.getImageUrl()).toUri().toString()));
                imageView.setFitWidth(350);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("article-image");
                card.getChildren().add(imageView);
            } catch (Exception e) {
                // Skip image if it can't be loaded
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
        
        // Article description
        Label descriptionLabel = new Label(article.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("article-description");
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button readButton = new Button("Read Full Article");
        readButton.getStyleClass().add("article-button");
        
        buttonBox.getChildren().add(readButton);
        
        // Add edit/delete buttons for author or admin
        if (article.getAuthorId() == currentUser.getId() || isAdmin) {
            Button editButton = new Button("Edit");
            editButton.getStyleClass().add("article-button");
            
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("article-button");
            
            buttonBox.getChildren().addAll(editButton, deleteButton);
        }
        
        // Add approve/unapprove button for admin
        if (isAdmin) {
            Button approveButton = new Button(article.isApproved() ? "Unapprove" : "Approve");
            approveButton.getStyleClass().add("article-button");
            
            buttonBox.getChildren().add(approveButton);
        }
        
        // Add all components to the card
        card.getChildren().addAll(titleLabel, metadataBox, descriptionLabel, buttonBox);
        
        return card;
    }
    
    /**
     * Creates a view for displaying the full article.
     * 
     * @param article the article to display
     * @return the full article view
     */
    public Node createFullArticleView(UserCreatedArticle article) {
        VBox fullView = new VBox(15);
        fullView.setPadding(new Insets(20));
        fullView.getStyleClass().add("full-article-view");
        
        // Back button
        Button backButton = new Button("Back to Articles");
        backButton.getStyleClass().add("back-button");
        
        // Article title
        Label titleLabel = new Label(article.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("full-article-title");
        
        // Article metadata
        HBox metadataBox = new HBox(15);
        metadataBox.setAlignment(Pos.CENTER_LEFT);
        
        Label authorLabel = new Label("By: " + article.getAuthorUsername());
        authorLabel.getStyleClass().add("full-article-author");
        
        Label dateLabel = new Label("Posted: " + article.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.getStyleClass().add("full-article-date");
        
        Label categoryLabel = new Label("Category: " + article.getCategory().getDisplayName());
        categoryLabel.getStyleClass().add("full-article-category");
        
        metadataBox.getChildren().addAll(authorLabel, dateLabel, categoryLabel);
        
        // Article image (if available)
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(Paths.get(article.getImageUrl()).toUri().toString()));
                imageView.setFitWidth(600);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("full-article-image");
                fullView.getChildren().add(imageView);
            } catch (Exception e) {
                // Skip image if it can't be loaded
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
        
        // Article content
        Label contentLabel = new Label(article.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("full-article-content");
        
        ScrollPane contentScrollPane = new ScrollPane(contentLabel);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.getStyleClass().add("content-scroll-pane");
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS);
        
        // Add all components to the view
        fullView.getChildren().addAll(backButton, titleLabel, metadataBox, contentScrollPane);
        
        // Add event handlers
        backButton.setOnAction(event -> displayAllArticles(article.isApproved()));
        
        return fullView;
    }
    
    /**
     * Displays all user-created articles.
     * 
     * @param approvedOnly whether to display only approved articles
     */
    public void displayAllArticles(boolean approvedOnly) {
        // This method will be implemented in the controller
        headerLabel.setText(approvedOnly ? "Published Articles" : "All Articles");
    }
    
    /**
     * Displays articles created by the current user.
     */
    public void displayMyArticles() {
        // This method will be implemented in the controller
        headerLabel.setText("My Articles");
    }
    
    /**
     * Sets the dark theme for the view.
     * 
     * @param isDarkTheme whether to use dark theme
     */
    public void setDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        
        if (isDarkTheme) {
            root.getStyleClass().add("dark-theme");
        } else {
            root.getStyleClass().remove("dark-theme");
        }
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param type the alert type
     * @param title the alert title
     * @param header the alert header
     * @param content the alert content
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Gets the root pane of the view.
     * 
     * @return the root pane
     */
    public BorderPane getRoot() {
        return root;
    }
    
    /**
     * Gets the create article button.
     * 
     * @return the create article button
     */
    public Button getCreateArticleButton() {
        return createArticleButton;
    }
    
    /**
     * Gets the my articles button.
     * 
     * @return the my articles button
     */
    public Button getMyArticlesButton() {
        return myArticlesButton;
    }
    
    /**
     * Gets the all articles button.
     * 
     * @return the all articles button
     */
    public Button getAllArticlesButton() {
        return allArticlesButton;
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
     * Gets the category filter combo box.
     * 
     * @return the category filter combo box
     */
    public ComboBox<Category> getCategoryFilterComboBox() {
        return categoryFilterComboBox;
    }
    
    /**
     * Gets the selected image file.
     * 
     * @return the selected image file
     */
    public File getSelectedImageFile() {
        return selectedImageFile;
    }
    
    /**
     * Gets the articles container.
     * 
     * @return the articles container
     */
    public VBox getArticlesContainer() {
        return articlesContainer;
    }
    
    /**
     * Gets the header label.
     * 
     * @return the header label
     */
    public Label getHeaderLabel() {
        return headerLabel;
    }
}