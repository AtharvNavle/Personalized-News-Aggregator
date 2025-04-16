package com.newsaggregator.view;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.service.UserService;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * View for the news feed screen.
 */
public class NewsView {
    private BorderPane root;
    private VBox articlesContainer;
    private ScrollPane articlesScrollPane;
    private ComboBox<Category> categoryComboBox;
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private Button previousButton;
    private Button nextButton;
    private Label currentPageLabel;
    private Button savedArticlesButton;
    private Button sharingHistoryButton;
    private Button preferencesButton;
    private Button adminButton;
    private Button logoutButton;
    private Button backButton;
    private Button darkThemeToggleButton;
    private Label welcomeLabel;
    private Label pageTitle;
    private Label noArticlesLabel;
    private ProgressIndicator loadingIndicator;
    private boolean isDarkTheme = true; // Start in dark mode by default
    
    private final Map<String, Button> saveButtonsMap = new HashMap<>();
    private final Map<String, Button> shareButtonsMap = new HashMap<>();
    private final UserService userService;

    /**
     * Constructor for NewsView.
     * 
     * @param userService the user service
     */
    public NewsView(UserService userService) {
        this.userService = userService;
        createView();
    }

    /**
     * Creates the news view.
     */
    private void createView() {
        root = new BorderPane();
        root.setPadding(new Insets(15));
        root.getStyleClass().add("news-view");
        
        // Apply dark theme by default at startup
        if (isDarkTheme) {
            root.getStyleClass().add("dark-theme");
        }

        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        pageTitle = new Label("News Feed");
        pageTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        welcomeLabel = new Label("Welcome!");
        welcomeLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        // User controls in header
        savedArticlesButton = new Button("Saved Articles");
        savedArticlesButton.getStyleClass().add("header-button");
        
        sharingHistoryButton = new Button("Sharing History");
        sharingHistoryButton.getStyleClass().add("header-button");
        
        preferencesButton = new Button("Preferences");
        preferencesButton.getStyleClass().add("header-button");
        
        adminButton = new Button("Admin Dashboard");
        adminButton.getStyleClass().add("header-button");
        adminButton.setVisible(false); // Only visible for admin users
        
        logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("header-button");
        
        // Dark theme toggle button
        darkThemeToggleButton = new Button("Dark Theme: On"); // Changed to On since dark theme is enabled by default
        darkThemeToggleButton.getStyleClass().add("header-button");
        darkThemeToggleButton.setOnAction(event -> toggleDarkTheme());
        
        headerBox.getChildren().addAll(pageTitle, welcomeLabel, headerSpacer, 
                savedArticlesButton, sharingHistoryButton, preferencesButton, adminButton, darkThemeToggleButton, logoutButton);
        
        // Toolbar
        HBox toolbarBox = new HBox(10);
        toolbarBox.setAlignment(Pos.CENTER_LEFT);
        toolbarBox.setPadding(new Insets(10, 0, 10, 0));
        
        // Category selector
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setPrefWidth(150);
        
        // Search components
        searchField = new TextField();
        searchField.setPromptText("Search news...");
        searchField.setPrefWidth(250);
        
        searchButton = new Button("Search");
        searchButton.getStyleClass().add("action-button");
        
        // Refresh button
        refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("action-button");
        
        // Back button (initially hidden)
        backButton = new Button("Back to News");
        backButton.getStyleClass().add("action-button");
        backButton.setVisible(false);
        
        Region toolbarSpacer = new Region();
        HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);
        
        // Add loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(24, 24);
        
        toolbarBox.getChildren().addAll(
                backButton, categoryComboBox, searchField, searchButton, refreshButton, 
                toolbarSpacer, loadingIndicator);
        
        // Navigation controls
        HBox navigationBox = new HBox(10);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.setPadding(new Insets(10, 0, 10, 0));
        
        previousButton = new Button("Previous");
        previousButton.getStyleClass().add("nav-button");
        
        currentPageLabel = new Label("Page 1");
        currentPageLabel.setPadding(new Insets(5, 10, 5, 10));
        
        nextButton = new Button("Next");
        nextButton.getStyleClass().add("nav-button");
        
        navigationBox.getChildren().addAll(previousButton, currentPageLabel, nextButton);
        
        // Combine toolbar and navigation
        VBox topControls = new VBox(10, headerBox, toolbarBox, navigationBox);
        root.setTop(topControls);
        
        // Articles container
        articlesContainer = new VBox(15);
        articlesContainer.setPadding(new Insets(10));
        
        articlesScrollPane = new ScrollPane(articlesContainer);
        articlesScrollPane.setFitToWidth(true);
        articlesScrollPane.setFitToHeight(true);
        articlesScrollPane.getStyleClass().add("articles-scroll-pane");
        
        // No articles message
        noArticlesLabel = new Label("No articles found. Try another search or category.");
        noArticlesLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        noArticlesLabel.setAlignment(Pos.CENTER);
        noArticlesLabel.setTextAlignment(TextAlignment.CENTER);
        noArticlesLabel.setVisible(false);
        noArticlesLabel.setPadding(new Insets(50, 0, 0, 0));
        noArticlesLabel.setMaxWidth(Double.MAX_VALUE);
        articlesContainer.getChildren().add(noArticlesLabel);
        
        root.setCenter(articlesScrollPane);
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
     * Gets the articles container.
     *
     * @return the articles container
     */
    public VBox getArticlesContainer() {
        return articlesContainer;
    }

    /**
     * Gets the articles scroll pane.
     *
     * @return the articles scroll pane
     */
    public ScrollPane getArticlesScrollPane() {
        return articlesScrollPane;
    }

    /**
     * Gets the category combo box.
     *
     * @return the category combo box
     */
    public ComboBox<Category> getCategoryComboBox() {
        return categoryComboBox;
    }

    /**
     * Gets the search field.
     *
     * @return the search field
     */
    public TextField getSearchField() {
        return searchField;
    }

    /**
     * Gets the search button.
     *
     * @return the search button
     */
    public Button getSearchButton() {
        return searchButton;
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
     * Gets the previous button.
     *
     * @return the previous button
     */
    public Button getPreviousButton() {
        return previousButton;
    }

    /**
     * Gets the next button.
     *
     * @return the next button
     */
    public Button getNextButton() {
        return nextButton;
    }

    /**
     * Gets the current page label.
     *
     * @return the current page label
     */
    public Label getCurrentPageLabel() {
        return currentPageLabel;
    }

    /**
     * Gets the saved articles button.
     *
     * @return the saved articles button
     */
    public Button getSavedArticlesButton() {
        return savedArticlesButton;
    }
    
    /**
     * Gets the sharing history button.
     *
     * @return the sharing history button
     */
    public Button getSharingHistoryButton() {
        return sharingHistoryButton;
    }

    /**
     * Gets the preferences button.
     *
     * @return the preferences button
     */
    public Button getPreferencesButton() {
        return preferencesButton;
    }

    /**
     * Gets the admin button.
     *
     * @return the admin button
     */
    public Button getAdminButton() {
        return adminButton;
    }

    /**
     * Gets the logout button.
     *
     * @return the logout button
     */
    public Button getLogoutButton() {
        return logoutButton;
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
     * Gets the welcome label.
     *
     * @return the welcome label
     */
    public Label getWelcomeLabel() {
        return welcomeLabel;
    }

    /**
     * Gets the page title.
     *
     * @return the page title
     */
    public Label getPageTitle() {
        return pageTitle;
    }

    /**
     * Gets the no articles label.
     *
     * @return the no articles label
     */
    public Label getNoArticlesLabel() {
        return noArticlesLabel;
    }

    /**
     * Gets the loading indicator.
     *
     * @return the loading indicator
     */
    public ProgressIndicator getLoadingIndicator() {
        return loadingIndicator;
    }

    /**
     * Adds an article to the UI.
     *
     * @param article         the article to add
     * @param onOpenAction    the action to perform when opening the article
     * @param onSaveAction    the action to perform when saving/unsaving the article
     * @param onShareAction   the action to perform when sharing the article
     */
    public void addArticleToUI(Article article, EventHandler<ActionEvent> onOpenAction, 
                               EventHandler<ActionEvent> onSaveAction,
                               EventHandler<ActionEvent> onShareAction) {
        // Article card container
        VBox articleCard = new VBox(10);
        articleCard.setPadding(new Insets(15));
        articleCard.getStyleClass().add("article-card");
        
        // Apply dark theme if active
        if (isDarkTheme) {
            articleCard.getStyleClass().add("dark-theme");
        }
        
        // Article title
        Label titleLabel = new Label(article.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setWrapText(true);
        
        // Article description
        Label descriptionLabel = new Label(article.getDescription());
        descriptionLabel.setWrapText(true);
        
        // Create a content box with image and text side by side
        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.setPadding(new Insets(0, 0, 10, 0)); // Add some bottom padding
        
        // Article image (if available)
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            try {
                // Create an image view with a placeholder initially
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
                imageView.setFitWidth(150);  // Smaller image width
                imageView.setFitHeight(120); // Smaller image height
                imageView.setPreserveRatio(true);
                
                // Add a border and padding
                imageView.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-padding: 5px;");
                
                // Load image in a background thread to avoid UI freezing
                javafx.concurrent.Task<javafx.scene.image.Image> loadImageTask = new javafx.concurrent.Task<>() {
                    @Override
                    protected javafx.scene.image.Image call() throws Exception {
                        try {
                            return new javafx.scene.image.Image(article.getImageUrl(), true);
                        } catch (Exception e) {
                            System.err.println("Failed to load image: " + article.getImageUrl());
                            return null;
                        }
                    }
                };
                
                loadImageTask.setOnSucceeded(event -> {
                    javafx.scene.image.Image image = loadImageTask.getValue();
                    if (image != null && !image.isError()) {
                        imageView.setImage(image);
                    } else {
                        // Hide the image view if loading fails
                        imageView.setVisible(false);
                    }
                });
                
                loadImageTask.setOnFailed(event -> {
                    imageView.setVisible(false);
                });
                
                // Start loading the image
                Thread imageThread = new Thread(loadImageTask);
                imageThread.setDaemon(true);
                imageThread.start();
                
                // Add image to the content box
                contentBox.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Error setting up image loading: " + e.getMessage());
            }
        }
        
        // Create a VBox for text content (title and description)
        VBox textContentBox = new VBox(10);
        textContentBox.setAlignment(Pos.TOP_LEFT);
        textContentBox.setPrefWidth(400); // Set preferred width for text content
        
        // Make title bold and larger
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);
        
        // Ensure description has wrap text enabled and is visible
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);
        
        textContentBox.getChildren().addAll(titleLabel, descriptionLabel);
        HBox.setHgrow(textContentBox, Priority.ALWAYS);
        
        // Add the text content to the content box
        contentBox.getChildren().add(textContentBox);
        
        // Add the content box to the article card
        articleCard.getChildren().add(contentBox);
        
        // Article metadata
        HBox metadataBox = new HBox(15);
        metadataBox.setAlignment(Pos.CENTER_LEFT);
        
        String authorText = article.getAuthor() != null && !article.getAuthor().isEmpty() 
                          ? article.getAuthor() : "Unknown";
        Label authorLabel = new Label("By: " + authorText);
        
        String dateText = article.getPublishedAt() != null 
                        ? article.getPublishedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) 
                        : "Unknown date";
        Label dateLabel = new Label("Published: " + dateText);
        
        Label sourceLabel = new Label("Source: " + article.getSource());
        Label categoryLabel = new Label("Category: " + article.getCategory().getDisplayName());
        
        metadataBox.getChildren().addAll(authorLabel, dateLabel, sourceLabel, categoryLabel);
        
        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        
        Button openButton = new Button("Read Article");
        openButton.getStyleClass().add("action-button");
        openButton.setOnAction(onOpenAction);
        
        Button saveButton = new Button(article.isSaved() ? "Unsave Article" : "Save Article");
        saveButton.getStyleClass().add("action-button");
        saveButton.setId("save-" + article.getId());
        saveButton.setOnAction(onSaveAction);
        
        // Store save button reference for updating later
        saveButtonsMap.put(article.getId(), saveButton);
        
        // Add share button
        Button shareButton = new Button("Share");
        shareButton.getStyleClass().add("action-button");
        shareButton.setId("share-" + article.getId());
        shareButton.setOnAction(onShareAction);
        
        // Store share button reference for updating later
        shareButtonsMap.put(article.getId(), shareButton);
        
        actionBox.getChildren().addAll(openButton, saveButton, shareButton);
        
        // Add components to the article card
        // IMPORTANT: Clear any existing children first to avoid duplicates
        articleCard.getChildren().clear();
        
        // Add all components in proper order
        articleCard.getChildren().add(contentBox);     // Image and title/description
        articleCard.getChildren().add(metadataBox);    // Author, date, source info 
        articleCard.getChildren().add(actionBox);      // Action buttons
        
        // Add to articles container
        articlesContainer.getChildren().add(articleCard);
    }

    /**
     * Updates the save button text for an article.
     *
     * @param article the article whose save button to update
     */
    public void updateArticleSaveButton(Article article) {
        Button saveButton = saveButtonsMap.get(article.getId());
        if (saveButton != null) {
            saveButton.setText(article.isSaved() ? "Unsave Article" : "Save Article");
        }
    }
    
    /**
     * Updates the share button text and appearance for an article.
     *
     * @param article the article whose share button to update
     */
    public void updateArticleShareButton(Article article) {
        Button shareButton = shareButtonsMap.get(article.getId());
        if (shareButton != null) {
            if (article.isShared()) {
                String sharedText;
                if (article.getSharedVia() != null) {
                    if (article.getSharedVia().startsWith("Email to")) {
                        // Truncate long email addresses for display
                        String recipient = article.getSharedVia().substring(9);
                        if (recipient.length() > 15) {
                            recipient = recipient.substring(0, 12) + "...";
                        }
                        sharedText = "Shared via Email to " + recipient;
                    } else if ("Copied link".equals(article.getSharedVia())) {
                        sharedText = "Link copied";
                    } else {
                        sharedText = "Shared via " + article.getSharedVia();
                    }
                } else {
                    sharedText = "Shared";
                }
                
                shareButton.setText(sharedText);
                shareButton.getStyleClass().add("shared-button");
                
                // Add visual indicator based on sharing method
                if (article.getSharedVia() != null) {
                    if (article.getSharedVia().startsWith("Email")) {
                        shareButton.setStyle("-fx-background-color: #4285F4;");
                    } else if (article.getSharedVia().equals("Twitter")) {
                        shareButton.setStyle("-fx-background-color: #1DA1F2;");
                    } else if (article.getSharedVia().equals("Facebook")) {
                        shareButton.setStyle("-fx-background-color: #4267B2;");
                    } else if (article.getSharedVia().equals("LinkedIn")) {
                        shareButton.setStyle("-fx-background-color: #0077B5;");
                    } else if (article.getSharedVia().equals("Copied link")) {
                        shareButton.setStyle("-fx-background-color: #34A853;");
                    } else {
                        shareButton.setStyle("-fx-background-color: #673AB7;");
                    }
                    // Add a more visible text color
                    shareButton.setStyle(shareButton.getStyle() + " -fx-text-fill: white;");
                }
            } else {
                shareButton.setText("Share Article");
                shareButton.getStyleClass().remove("shared-button");
                shareButton.setStyle(""); // Reset any custom styles
            }
        }
    }
    
    /**
     * Gets the map of share buttons.
     *
     * @return the map of share buttons
     */
    public Map<String, Button> getShareButtonsMap() {
        return shareButtonsMap;
    }
    
    /**
     * Gets the dark theme toggle button.
     * 
     * @return the dark theme toggle button
     */
    public Button getDarkThemeToggleButton() {
        return darkThemeToggleButton;
    }
    
    /**
     * Toggles between dark and light themes.
     */
    private void toggleDarkTheme() {
        isDarkTheme = !isDarkTheme;
        darkThemeToggleButton.setText("Dark Theme: " + (isDarkTheme ? "On" : "Off"));
        
        // Apply or remove dark theme class to all UI elements
        if (isDarkTheme) {
            root.getStyleClass().add("dark-theme");
        } else {
            root.getStyleClass().remove("dark-theme");
        }
        
        // Update all article cards to reflect theme
        for (Node node : articlesContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox articleCard = (VBox) node;
                if (isDarkTheme) {
                    if (!articleCard.getStyleClass().contains("dark-theme")) {
                        articleCard.getStyleClass().add("dark-theme");
                    }
                } else {
                    articleCard.getStyleClass().remove("dark-theme");
                }
            }
        }
    }
}

/**
 * Utility Region class for layout spacing.
 */
class Region extends javafx.scene.layout.Region {
    // Simple utility class to create spacers in layouts
}
