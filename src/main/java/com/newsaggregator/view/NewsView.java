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
    private Button preferencesButton;
    private Button adminButton;
    private Button logoutButton;
    private Button backButton;
    private Label welcomeLabel;
    private Label pageTitle;
    private Label noArticlesLabel;
    private ProgressIndicator loadingIndicator;
    
    private final Map<String, Button> saveButtonsMap = new HashMap<>();
    private final Map<String, Button> translateButtonsMap = new HashMap<>();
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
        
        preferencesButton = new Button("Preferences");
        preferencesButton.getStyleClass().add("header-button");
        
        adminButton = new Button("Admin Dashboard");
        adminButton.getStyleClass().add("header-button");
        adminButton.setVisible(false); // Only visible for admin users
        
        logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("header-button");
        
        headerBox.getChildren().addAll(pageTitle, welcomeLabel, headerSpacer, 
                savedArticlesButton, preferencesButton, adminButton, logoutButton);
        
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
     * @param onTranslateAction the action to perform when translating the article
     */
    public void addArticleToUI(Article article, EventHandler<ActionEvent> onOpenAction, 
                               EventHandler<ActionEvent> onSaveAction,
                               EventHandler<ActionEvent> onTranslateAction) {
        // Article card container
        VBox articleCard = new VBox(10);
        articleCard.setPadding(new Insets(15));
        articleCard.getStyleClass().add("article-card");
        
        // Article title
        Label titleLabel = new Label(article.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setWrapText(true);
        
        // Article description
        Label descriptionLabel = new Label(article.getDescription());
        descriptionLabel.setWrapText(true);
        
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
        
        // Add translate button
        Button translateButton = new Button(article.isTranslated() ? "Show Original" : "Translate");
        translateButton.getStyleClass().add("action-button");
        translateButton.setId("translate-" + article.getId());
        translateButton.setOnAction(onTranslateAction);
        
        // Store translate button reference for updating later
        translateButtonsMap.put(article.getId(), translateButton);
        
        actionBox.getChildren().addAll(openButton, saveButton, translateButton);
        
        // Add all components to the article card
        articleCard.getChildren().addAll(titleLabel, descriptionLabel, metadataBox, actionBox);
        
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
     * Updates the translate button text for an article.
     *
     * @param article the article whose translate button to update
     */
    public void updateArticleTranslateButton(Article article) {
        Button translateButton = translateButtonsMap.get(article.getId());
        if (translateButton != null) {
            translateButton.setText(article.isTranslated() ? "Show Original" : "Translate");
        }
    }
    
    /**
     * Gets the map of translate buttons.
     *
     * @return the map of translate buttons
     */
    public Map<String, Button> getTranslateButtonsMap() {
        return translateButtonsMap;
    }
}

/**
 * Utility Region class for layout spacing.
 */
class Region extends javafx.scene.layout.Region {
    // Simple utility class to create spacers in layouts
}
