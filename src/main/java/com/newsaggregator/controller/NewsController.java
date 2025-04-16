package com.newsaggregator.controller;

import com.newsaggregator.Main;
import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.service.NewsService;
import com.newsaggregator.service.UserService;
import com.newsaggregator.view.AdminView;
import com.newsaggregator.view.LoginView;
import com.newsaggregator.view.NewsView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for handling news operations and events.
 */
public class NewsController {
    private static final Logger LOGGER = Logger.getLogger(NewsController.class.getName());
    
    private final NewsView newsView;
    private final UserService userService;
    private final NewsService newsService;
    private final ExecutorService executorService;
    
    private Category currentCategory = Category.GENERAL;
    private String currentQuery = "";
    private int currentPage = 1;
    private final int articlesPerPage = 20;
    private boolean isLoading = false;
    private final Map<String, Article> originalArticles = new HashMap<>(); // Store original articles for reverting translations

    /**
     * Constructor for NewsController.
     *
     * @param newsView    the news view
     * @param userService the user service
     */
    public NewsController(NewsView newsView, UserService userService) {
        this.newsView = newsView;
        this.userService = userService;
        this.newsService = new NewsService();
        this.executorService = Executors.newCachedThreadPool();
        
        // Initialize UI components
        initializeComponents();
        
        // Load initial news articles
        loadNews();
    }

    /**
     * Initializes the UI components and event handlers.
     */
    private void initializeComponents() {
        // Initialize category selection
        newsView.getCategoryComboBox().getItems().setAll(Category.values());
        newsView.getCategoryComboBox().setValue(Category.GENERAL);
        newsView.getCategoryComboBox().setOnAction(event -> handleCategoryChange());
        
        // Initialize search functionality
        newsView.getSearchButton().setOnAction(event -> handleSearch());
        newsView.getSearchField().setOnAction(event -> handleSearch());
        
        // Initialize refresh button
        newsView.getRefreshButton().setOnAction(event -> refreshNews());
        
        // Initialize navigation buttons
        newsView.getPreviousButton().setOnAction(event -> loadPreviousPage());
        newsView.getNextButton().setOnAction(event -> loadNextPage());
        
        // Initialize saved articles button
        newsView.getSavedArticlesButton().setOnAction(event -> showSavedArticles());
        
        // Initialize preferences button
        newsView.getPreferencesButton().setOnAction(event -> showPreferencesDialog());
        
        // Initialize admin dashboard button (visible only for admin users)
        if (userService.getCurrentUser() != null && userService.getCurrentUser().isAdmin()) {
            newsView.getAdminButton().setVisible(true);
            newsView.getAdminButton().setOnAction(event -> navigateToAdminView());
        } else {
            newsView.getAdminButton().setVisible(false);
        }
        
        // Initialize logout button
        newsView.getLogoutButton().setOnAction(event -> handleLogout());
        
        // Initialize scroll pane listener for infinite scrolling
        newsView.getArticlesScrollPane().vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0.9 && !isLoading) {
                // Load more articles when scrolling near the bottom
                loadMoreArticles();
            }
        });
        
        // Set welcome message
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            newsView.getWelcomeLabel().setText("Welcome, " + currentUser.getUsername() + "!");
        }
    }

    /**
     * Handles category change events.
     */
    private void handleCategoryChange() {
        currentCategory = newsView.getCategoryComboBox().getValue();
        currentPage = 1;
        
        // Clear current articles
        newsView.getArticlesContainer().getChildren().clear();
        
        // Load articles for the selected category
        loadNews();
    }

    /**
     * Handles search button clicks and search field enter presses.
     */
    private void handleSearch() {
        String searchQuery = newsView.getSearchField().getText().trim();
        currentQuery = searchQuery;
        currentPage = 1;
        
        // Clear current articles
        newsView.getArticlesContainer().getChildren().clear();
        
        // Load articles for the search query
        loadNews();
    }

    /**
     * Loads the news articles.
     */
    private void loadNews() {
        isLoading = true;
        newsView.getLoadingIndicator().setVisible(true);
        
        executorService.submit(() -> {
            List<Article> articles;
            User currentUser = userService.getCurrentUser();
            
            if (currentQuery.isEmpty()) {
                // Fetch top headlines if no search query
                articles = newsService.getTopHeadlines(currentCategory, null, currentPage, currentUser);
            } else {
                // Search for articles with the query
                articles = newsService.searchNews(currentQuery, currentCategory, currentPage, currentUser);
            }
            
            // Update UI on the JavaFX thread
            Platform.runLater(() -> {
                displayArticles(articles);
                updateNavigationButtons();
                newsView.getLoadingIndicator().setVisible(false);
                isLoading = false;
            });
        });
    }

    /**
     * Displays articles in the UI.
     *
     * @param articles the articles to display
     */
    private void displayArticles(List<Article> articles) {
        // Clear existing articles if this is the first page
        if (currentPage == 1) {
            newsView.getArticlesContainer().getChildren().clear();
        }
        
        if (articles.isEmpty()) {
            newsView.getNoArticlesLabel().setVisible(true);
        } else {
            newsView.getNoArticlesLabel().setVisible(false);
            
            for (Article article : articles) {
                // Add article to the UI
                newsView.addArticleToUI(article, event -> openArticle(article), 
                        event -> toggleSaveArticle(article),
                        event -> translateArticle(article));
            }
        }
        
        // Update the current page label
        newsView.getCurrentPageLabel().setText("Page " + currentPage);
    }

    /**
     * Updates the visibility of navigation buttons.
     */
    private void updateNavigationButtons() {
        // Previous button is disabled on the first page
        newsView.getPreviousButton().setDisable(currentPage <= 1);
        
        // Next button is always enabled unless we know we're on the last page
        // (In a real implementation, you might want to check if there are more articles available)
        newsView.getNextButton().setDisable(false);
    }

    /**
     * Loads the previous page of articles.
     */
    private void loadPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadNews();
            
            // Scroll to the top
            newsView.getArticlesScrollPane().setVvalue(0);
        }
    }

    /**
     * Loads the next page of articles.
     */
    private void loadNextPage() {
        currentPage++;
        loadNews();
        
        // Scroll to the top
        newsView.getArticlesScrollPane().setVvalue(0);
    }

    /**
     * Loads more articles for infinite scrolling.
     */
    private void loadMoreArticles() {
        currentPage++;
        loadNews();
    }

    /**
     * Refreshes the current news articles.
     */
    private void refreshNews() {
        // Reset to the first page
        currentPage = 1;
        
        // Clear the current search query if needed
        if (!currentQuery.isEmpty()) {
            currentQuery = "";
            newsView.getSearchField().clear();
        }
        
        // Load fresh articles
        loadNews();
    }

    /**
     * Opens an article in a detailed view.
     *
     * @param article the article to open
     */
    private void openArticle(Article article) {
        // Create a dialog to display the article
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(article.getTitle());
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        
        // Create a WebView to display the article content
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        
        // Build HTML content
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><style>")
                  .append("body {font-family: Arial; margin: 20px;}")
                  .append("h1 {color: #333; font-size: 24px;}")
                  .append("h2 {color: #666; font-size: 16px;}")
                  .append(".source {color: #999; font-style: italic;}")
                  .append(".content {margin-top: 20px; line-height: 1.5;}")
                  .append("img {max-width: 100%; height: auto; margin: 10px 0;}")
                  .append("</style></head><body>");
        
        htmlContent.append("<h1>").append(article.getTitle()).append("</h1>");
        
        if (article.getAuthor() != null && !article.getAuthor().isEmpty()) {
            htmlContent.append("<h2>By ").append(article.getAuthor()).append("</h2>");
        }
        
        htmlContent.append("<div class='source'>Source: ").append(article.getSource())
                  .append(" | Category: ").append(article.getCategory().getDisplayName()).append("</div>");
        
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            htmlContent.append("<img src='").append(article.getImageUrl()).append("' alt='Article Image'>");
        }
        
        htmlContent.append("<div class='content'>");
        
        if (article.getDescription() != null && !article.getDescription().isEmpty()) {
            htmlContent.append("<p><strong>").append(article.getDescription()).append("</strong></p>");
        }
        
        if (article.getContent() != null && !article.getContent().isEmpty()) {
            htmlContent.append("<p>").append(article.getContent()).append("</p>");
        }
        
        htmlContent.append("</div>");
        
        // Add a link to the full article
        htmlContent.append("<p><a href='").append(article.getUrl()).append("' target='_blank'>Read full article</a></p>");
        
        htmlContent.append("</body></html>");
        
        webEngine.loadContent(htmlContent.toString());
        
        // Add a handler for the "Read full article" link
        webEngine.setOnAlert(event -> {
            if (event.getData().startsWith("http")) {
                try {
                    Desktop.getDesktop().browse(new URI(event.getData()));
                } catch (IOException | URISyntaxException e) {
                    LOGGER.log(Level.WARNING, "Failed to open URL", e);
                }
            }
        });
        
        // Set dialog content and size
        ScrollPane scrollPane = new ScrollPane(webView);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        webView.setPrefWidth(800);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(850, 650);
        
        // Show the dialog
        dialog.showAndWait();
    }

    /**
     * Toggles saving/unsaving an article.
     *
     * @param article the article to save or unsave
     */
    private void toggleSaveArticle(Article article) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to save articles");
            return;
        }
        
        if (article.isSaved()) {
            // Unsave the article
            boolean removed = userService.removeSavedArticle(article);
            if (removed) {
                article.setSaved(false);
                newsView.updateArticleSaveButton(article);
                showAlert(AlertType.INFORMATION, "Article Unsaved", "Article removed from your saved articles");
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to remove article from saved articles");
            }
        } else {
            // Save the article
            boolean saved = userService.saveArticle(article);
            if (saved) {
                article.setSaved(true);
                newsView.updateArticleSaveButton(article);
                showAlert(AlertType.INFORMATION, "Article Saved", "Article saved to your collection");
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to save article");
            }
        }
    }

    /**
     * Translates an article to the user's preferred language or reverts to original.
     *
     * @param article the article to translate
     */
    private void translateArticle(Article article) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to translate articles");
            return;
        }
        
        if (!article.isTranslated()) {
            // Save original article for later reverting
            originalArticles.put(article.getId(), article);
            
            // Get user's preferred language or use a default
            String targetLanguage = currentUser.getPreferredLanguage();
            
            // Translate the article
            try {
                Article translatedArticle = newsService.translateArticle(article, targetLanguage);
                
                // Update UI to reflect translation state
                if (translatedArticle.isTranslated()) {
                    newsView.updateArticleTranslateButton(translatedArticle);
                    showAlert(AlertType.INFORMATION, "Article Translated", 
                            "Article has been translated to " + translatedArticle.getTranslatedLanguage());
                } else {
                    showAlert(AlertType.WARNING, "Translation Failed", 
                            "Could not translate the article. Please try again later.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to translate article", e);
                showAlert(AlertType.ERROR, "Translation Error", 
                        "An error occurred while translating the article: " + e.getMessage());
            }
        } else {
            // Revert to original article
            Article originalArticle = originalArticles.get(article.getId());
            if (originalArticle != null) {
                article.setTranslated(false);
                article.setTranslatedLanguage(null);
                article.setTitle(originalArticle.getTitle());
                article.setDescription(originalArticle.getDescription());
                article.setContent(originalArticle.getContent());
                
                // Update UI
                newsView.updateArticleTranslateButton(article);
                showAlert(AlertType.INFORMATION, "Original Article", 
                        "Showing original article content");
            }
        }
    }
    
    /**
     * Shows the user's saved articles.
     */
    private void showSavedArticles() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to view saved articles");
            return;
        }
        
        List<Article> savedArticles = userService.getSavedArticles();
        
        if (savedArticles.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Saved Articles", "You haven't saved any articles yet");
            return;
        }
        
        // Clear the current articles
        newsView.getArticlesContainer().getChildren().clear();
        
        // Display the saved articles
        for (Article article : savedArticles) {
            newsView.addArticleToUI(article, event -> openArticle(article), 
                    event -> toggleSaveArticle(article),
                    event -> translateArticle(article));
        }
        
        // Update UI to show we're in saved articles mode
        newsView.getPageTitle().setText("Saved Articles");
        newsView.getCurrentPageLabel().setText("");
        newsView.getPreviousButton().setDisable(true);
        newsView.getNextButton().setDisable(true);
        
        // Add a back button to return to normal news view
        newsView.getBackButton().setVisible(true);
        newsView.getBackButton().setOnAction(event -> {
            newsView.getPageTitle().setText("News Feed");
            newsView.getBackButton().setVisible(false);
            refreshNews();
        });
    }

    /**
     * Shows the user preferences dialog.
     */
    private void showPreferencesDialog() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to change preferences");
            return;
        }
        
        // Create a dialog for user preferences
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("User Preferences");
        dialog.setHeaderText("Customize Your News Feed");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        // Username field
        TextField usernameField = new TextField(currentUser.getUsername());
        grid.add(new javafx.scene.control.Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        
        // Email field
        TextField emailField = new TextField(currentUser.getEmail());
        grid.add(new javafx.scene.control.Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        
        // Language preference
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("en", "fr", "de", "es", "it", "ru");
        languageCombo.setValue(currentUser.getPreferredLanguage());
        grid.add(new javafx.scene.control.Label("Preferred Language:"), 0, 2);
        grid.add(languageCombo, 1, 2);
        
        // Country preference
        ComboBox<String> countryCombo = new ComboBox<>();
        countryCombo.getItems().addAll("us", "gb", "ca", "au", "fr", "de");
        countryCombo.setValue(currentUser.getPreferredCountry());
        grid.add(new javafx.scene.control.Label("Preferred Country:"), 0, 3);
        grid.add(countryCombo, 1, 3);
        
        // Category preferences
        grid.add(new javafx.scene.control.Label("Preferred Categories:"), 0, 4);
        
        VBox categoriesBox = new VBox(5);
        Set<Category> userCategories = currentUser.getPreferredCategories();
        
        for (Category category : Category.values()) {
            CheckBox categoryCheck = new CheckBox(category.getDisplayName());
            categoryCheck.setSelected(userCategories.contains(category));
            categoriesBox.getChildren().add(categoryCheck);
        }
        
        grid.add(categoriesBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Collect selected categories
            Set<Category> selectedCategories = new HashSet<>();
            for (int i = 0; i < categoriesBox.getChildren().size(); i++) {
                CheckBox check = (CheckBox) categoriesBox.getChildren().get(i);
                if (check.isSelected()) {
                    selectedCategories.add(Category.values()[i]);
                }
            }
            
            // Update user preferences
            boolean updated = userService.updateProfile(
                    usernameField.getText(),
                    emailField.getText(),
                    languageCombo.getValue(),
                    countryCombo.getValue(),
                    selectedCategories
            );
            
            if (updated) {
                showAlert(AlertType.INFORMATION, "Preferences Updated", "Your preferences have been updated");
                
                // Update welcome message
                newsView.getWelcomeLabel().setText("Welcome, " + userService.getCurrentUser().getUsername() + "!");
                
                // Refresh news to reflect new preferences
                refreshNews();
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "Failed to update preferences. Username or email may already be in use.");
            }
        }
    }

    /**
     * Navigates to the admin view.
     */
    private void navigateToAdminView() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            showAlert(AlertType.ERROR, "Permission Denied", "You must be an admin to access this area");
            return;
        }
        
        AdminView adminView = new AdminView();
        AdminController adminController = new AdminController(adminView, userService);
        
        Stage stage = Main.getPrimaryStage();
        Scene scene = new Scene(adminView.getRoot(), stage.getScene().getWidth(), stage.getScene().getHeight());
        scene.getStylesheets().add(getClass().getResource("/com/newsaggregator/css/styles.css").toExternalForm());
        
        stage.setScene(scene);
    }

    /**
     * Handles the logout button click event.
     */
    private void handleLogout() {
        userService.logout();
        
        // Navigate back to login view
        LoginView loginView = new LoginView();
        LoginController loginController = new LoginController(loginView);
        
        Stage stage = Main.getPrimaryStage();
        Scene scene = new Scene(loginView.getRoot(), stage.getScene().getWidth(), stage.getScene().getHeight());
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
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Cleans up resources when the controller is no longer needed.
     */
    public void cleanup() {
        executorService.shutdown();
        newsService.close();
    }
}
