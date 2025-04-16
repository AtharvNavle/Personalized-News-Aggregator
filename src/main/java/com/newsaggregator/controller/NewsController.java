package com.newsaggregator.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.newsaggregator.Main;
import com.newsaggregator.controller.AdminController;
import com.newsaggregator.controller.LoginController;
import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.service.NewsService;
import com.newsaggregator.service.UserService;
import com.newsaggregator.view.AdminView;
import com.newsaggregator.view.LoginView;
import com.newsaggregator.view.NewsView;

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
    private final Map<String, Article> sharedArticles = new HashMap<>(); // Store articles that have been shared
    private final List<Article> sharingHistory = new ArrayList<>(); // Store sharing history

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
        
        // Initialize sharing history button
        newsView.getSharingHistoryButton().setOnAction(event -> showSharingHistory());
        
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
                        event -> shareArticle(article));
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
        
        // Always clear any search query
        currentQuery = "";
        newsView.getSearchField().clear();
        
        // Clear existing articles
        newsView.getArticlesContainer().getChildren().clear();
        
        // Show loading indicator
        newsView.getLoadingIndicator().setVisible(true);
        
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
     * Shares an article via email, social media, or reverts sharing state.
     *
     * @param article the article to share
     */
    private void shareArticle(Article article) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to share articles");
            return;
        }
        
        if (!article.isShared()) {
            // Save article for tracking sharing status
            sharedArticles.put(article.getId(), article);
            
            // Show sharing options dialog
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Share Article");
            dialog.setHeaderText("Share \"" + article.getTitle() + "\"");
            
            // Create the sharing options
            ComboBox<String> sharingOptions = new ComboBox<>();
            sharingOptions.getItems().addAll("Email", "Twitter", "Facebook", "LinkedIn", "Copy Link");
            sharingOptions.setValue("Email");
            
            // Additional fields based on the selection
            TextField recipientField = new TextField();
            recipientField.setPromptText("Enter recipient email");
            
            TextArea messageField = new TextArea();
            messageField.setPromptText("Add a message (optional)");
            messageField.setPrefRowCount(3);
            messageField.setPrefColumnCount(30);
            
            Label statusLabel = new Label("");
            statusLabel.setVisible(false);
            statusLabel.setStyle("-fx-text-fill: green;");
            
            // Set up the grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            
            grid.add(new Label("Share via:"), 0, 0);
            grid.add(sharingOptions, 1, 0);
            
            // Additional row for recipient
            grid.add(new Label("To:"), 0, 1);
            grid.add(recipientField, 1, 1);
            
            // Message area
            grid.add(new Label("Message:"), 0, 2);
            grid.add(messageField, 1, 2);
            
            // Status label
            grid.add(statusLabel, 0, 3, 2, 1);
            
            // Add article URL for reference
            Label urlLabel = new Label("Article URL:");
            TextField urlField = new TextField(article.getUrl());
            urlField.setEditable(false);
            grid.add(urlLabel, 0, 4);
            grid.add(urlField, 1, 4);
            
            // Add a copy link button for convenience
            Button copyLinkButton = new Button("Copy to Clipboard");
            grid.add(copyLinkButton, 1, 5);
            
            copyLinkButton.setOnAction(event -> {
                // Copy the URL to clipboard
                final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(urlField.getText());
                clipboard.setContent(content);
                
                // Show confirmation
                statusLabel.setText("Link copied to clipboard!");
                statusLabel.setVisible(true);
            });
            
            // Update fields based on sharing method
            sharingOptions.setOnAction(event -> {
                String selectedMethod = sharingOptions.getValue();
                
                if ("Email".equals(selectedMethod)) {
                    recipientField.setPromptText("Enter recipient email");
                    recipientField.setVisible(true);
                    messageField.setVisible(true);
                } else if ("Twitter".equals(selectedMethod) || "Facebook".equals(selectedMethod) || "LinkedIn".equals(selectedMethod)) {
                    recipientField.setVisible(false);
                    messageField.setPromptText("Add a message to your post (optional)");
                    messageField.setVisible(true);
                } else if ("Copy Link".equals(selectedMethod)) {
                    recipientField.setVisible(false);
                    messageField.setVisible(false);
                }
                
                statusLabel.setVisible(false);
            });
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Process the result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    String method = sharingOptions.getValue();
                    String to = recipientField.getText();
                    String message = messageField.getText();
                    
                    // For real functionality, we would handle different methods differently
                    if ("Email".equals(method) && (to == null || to.trim().isEmpty())) {
                        statusLabel.setText("Please enter a recipient email!");
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setVisible(true);
                        return null;
                    }
                    
                    // If copy link is selected, copy to clipboard
                    if ("Copy Link".equals(method)) {
                        final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                        final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                        content.putString(article.getUrl());
                        clipboard.setContent(content);
                    }
                    
                    // Return sharing details
                    return method + (to != null && !to.isEmpty() ? ":" + to : "");
                }
                return null;
            });
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(sharingMethod -> {
                try {
                    String method = sharingMethod.split(":")[0];
                    
                    // Use NewsService to handle sharing and update article status
                    Article sharedArticle = newsService.shareArticle(article, sharingMethod);
                    
                    // Add to sharing history
                    sharingHistory.add(0, sharedArticle); // Add at the beginning for most recent first
                    
                    // Update UI to reflect sharing state
                    newsView.updateArticleShareButton(sharedArticle);
                    
                    // Specific message based on sharing method
                    if ("Copy Link".equals(method)) {
                        showAlert(AlertType.INFORMATION, "Link Copied", 
                              "Article link has been copied to your clipboard");
                    } else if ("Email".equals(method)) {
                        showAlert(AlertType.INFORMATION, "Email Sharing", 
                              "Article would be shared via email. In a real app, this would open your email client.");
                    } else {
                        showAlert(AlertType.INFORMATION, "Article Shared", 
                              "Article has been shared via " + method);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to share article", e);
                    showAlert(AlertType.ERROR, "Sharing Error", 
                              "An error occurred while sharing the article: " + e.getMessage());
                }
            });
        } else {
            // Reset sharing status
            article.setShared(false);
            article.setSharedVia(null);
            
            // Update UI
            newsView.updateArticleShareButton(article);
            showAlert(AlertType.INFORMATION, "Sharing Reset", 
                     "Article sharing status has been reset");
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
                    event -> shareArticle(article));
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
     * Shows the user's sharing history.
     */
    private void showSharingHistory() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "You must be logged in to view sharing history");
            return;
        }
        
        if (sharingHistory.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Sharing History", "You haven't shared any articles yet");
            return;
        }
        
        // Clear the current articles
        newsView.getArticlesContainer().getChildren().clear();
        
        // Display the shared articles
        for (Article article : sharingHistory) {
            newsView.addArticleToUI(article, event -> openArticle(article), 
                    event -> toggleSaveArticle(article),
                    event -> shareArticle(article));
        }
        
        // Update UI to show we're in sharing history mode
        newsView.getPageTitle().setText("Sharing History");
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
