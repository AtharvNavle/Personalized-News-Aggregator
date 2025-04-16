package com.newsaggregator.controller;

import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserCreatedArticle;
import com.newsaggregator.service.UserArticleService;
import com.newsaggregator.view.UserArticleView;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for handling user-created article operations and events.
 */
public class UserArticleController {
    private static final Logger LOGGER = Logger.getLogger(UserArticleController.class.getName());
    private final UserArticleView view;
    private final UserArticleService service;
    private final User currentUser;
    private final ExecutorService executorService;
    private BorderPane mainContainer;
    
    /**
     * Constructor.
     * 
     * @param user the current user
     * @param mainContainer the main container for the application
     * @param isDarkTheme whether dark theme is enabled
     */
    public UserArticleController(User user, BorderPane mainContainer, boolean isDarkTheme) {
        this.currentUser = user;
        this.mainContainer = mainContainer;
        this.service = new UserArticleService();
        this.view = new UserArticleView(user);
        this.view.setDarkTheme(isDarkTheme);
        this.executorService = Executors.newSingleThreadExecutor();
        
        setupEventHandlers();
    }
    
    /**
     * Sets up event handlers for the view.
     */
    private void setupEventHandlers() {
        // Back button
        view.getBackButton().setOnAction(event -> goBackToMain());
        
        // Create article button
        view.getCreateArticleButton().setOnAction(event -> showCreateArticleForm());
        
        // My articles button
        view.getMyArticlesButton().setOnAction(event -> loadMyArticles());
        
        // All articles button
        view.getAllArticlesButton().setOnAction(event -> loadAllArticles(currentUser.isAdmin() ? false : true));
        
        // Category filter
        view.getCategoryFilterComboBox().setOnAction(event -> filterArticlesByCategory());
    }
    
    /**
     * Shows the create article form.
     */
    private void showCreateArticleForm() {
        VBox form = view.createArticleForm();
        
        // Get the form submit button and add event handler
        Button submitButton = (Button) form.getChildren().stream()
                .filter(node -> node instanceof Button && ((Button) node).getText().equals("Submit Article"))
                .findFirst()
                .orElse(null);
        
        if (submitButton != null) {
            submitButton.setOnAction(event -> {
                // Get form fields
                TextField titleField = (TextField) form.getChildren().stream()
                        .filter(node -> node instanceof TextField)
                        .findFirst()
                        .orElse(null);
                
                TextArea descriptionArea = (TextArea) form.getChildren().stream()
                        .filter(node -> node instanceof TextArea && ((TextArea) node).getPromptText().contains("description"))
                        .findFirst()
                        .orElse(null);
                
                TextArea contentArea = (TextArea) form.getChildren().stream()
                        .filter(node -> node instanceof TextArea && ((TextArea) node).getPromptText().contains("content"))
                        .findFirst()
                        .orElse(null);
                
                ComboBox<Category> categoryComboBox = (ComboBox<Category>) form.getChildren().stream()
                        .filter(node -> node instanceof ComboBox)
                        .findFirst()
                        .orElse(null);
                
                // Validate form
                if (validateForm(titleField, descriptionArea, contentArea, categoryComboBox)) {
                    // Create article
                    createArticle(
                            titleField.getText(),
                            descriptionArea.getText(),
                            contentArea.getText(),
                            view.getSelectedImageFile(),
                            categoryComboBox.getValue()
                    );
                }
            });
        }
        
        view.getArticlesContainer().getChildren().clear();
        view.getArticlesContainer().getChildren().add(form);
    }
    
    /**
     * Validates the article form.
     * 
     * @param titleField the title field
     * @param descriptionArea the description area
     * @param contentArea the content area
     * @param categoryComboBox the category combo box
     * @return true if the form is valid, false otherwise
     */
    private boolean validateForm(TextField titleField, TextArea descriptionArea, 
                                TextArea contentArea, ComboBox<Category> categoryComboBox) {
        StringBuilder errorMessage = new StringBuilder();
        
        if (titleField == null || titleField.getText().trim().isEmpty()) {
            errorMessage.append("- Title is required\n");
        }
        
        if (descriptionArea == null || descriptionArea.getText().trim().isEmpty()) {
            errorMessage.append("- Description is required\n");
        }
        
        if (contentArea == null || contentArea.getText().trim().isEmpty()) {
            errorMessage.append("- Content is required\n");
        }
        
        if (categoryComboBox == null || categoryComboBox.getValue() == null) {
            errorMessage.append("- Category is required\n");
        }
        
        if (errorMessage.length() > 0) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates a new article.
     * 
     * @param title the article title
     * @param description the article description
     * @param content the article content
     * @param imageFile the article image file
     * @param category the article category
     */
    private void createArticle(String title, String description, String content, 
                              File imageFile, Category category) {
        executorService.submit(() -> {
            UserCreatedArticle article = service.createArticle(
                    currentUser, title, description, content, imageFile, category);
            
            if (article != null) {
                javafx.application.Platform.runLater(() -> {
                    showAlert(AlertType.INFORMATION, "Success", "Article created successfully", 
                            "Your article has been submitted for review.");
                    loadMyArticles();
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    showAlert(AlertType.ERROR, "Error", "Failed to create article", 
                            "An error occurred while creating the article.");
                });
            }
        });
    }
    
    /**
     * Loads articles created by the current user.
     */
    private void loadMyArticles() {
        view.getArticlesContainer().getChildren().clear();
        view.displayMyArticles();
        
        executorService.submit(() -> {
            List<UserCreatedArticle> articles = service.getArticlesByUser(currentUser.getId());
            
            javafx.application.Platform.runLater(() -> {
                if (articles.isEmpty()) {
                    view.getArticlesContainer().getChildren().add(
                            createNoArticlesMessage("You haven't created any articles yet."));
                } else {
                    displayArticles(articles);
                }
            });
        });
    }
    
    /**
     * Loads all articles, optionally filtered by approval status.
     * 
     * @param approvedOnly whether to load only approved articles
     */
    private void loadAllArticles(boolean approvedOnly) {
        view.getArticlesContainer().getChildren().clear();
        view.displayAllArticles(approvedOnly);
        
        executorService.submit(() -> {
            List<UserCreatedArticle> articles = service.getAllArticles(approvedOnly);
            
            javafx.application.Platform.runLater(() -> {
                if (articles.isEmpty()) {
                    view.getArticlesContainer().getChildren().add(
                            createNoArticlesMessage("No articles available."));
                } else {
                    displayArticles(articles);
                }
            });
        });
    }
    
    /**
     * Filters articles by the selected category.
     */
    private void filterArticlesByCategory() {
        Category selectedCategory = view.getCategoryFilterComboBox().getValue();
        
        // Get the current articles and filter them
        VBox container = view.getArticlesContainer();
        List<Node> currentCards = container.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("article-card"))
                .collect(Collectors.toList());
        
        // If no category is selected, show all articles
        if (selectedCategory == null) {
            currentCards.forEach(card -> card.setVisible(true));
            return;
        }
        
        // TODO: Implement proper filtering based on category information stored in the nodes
        
        // For now, just reload the appropriate articles
        if (view.getHeaderLabel().getText().contains("My Articles")) {
            loadMyArticles();
        } else {
            loadAllArticles(currentUser.isAdmin() ? false : true);
        }
    }
    
    /**
     * Displays a list of articles in the view.
     * 
     * @param articles the articles to display
     */
    private void displayArticles(List<UserCreatedArticle> articles) {
        view.getArticlesContainer().getChildren().clear();
        
        for (UserCreatedArticle article : articles) {
            Node card = view.createArticleCard(article, currentUser.isAdmin());
            view.getArticlesContainer().getChildren().add(card);
            
            // Add event handlers to the card's buttons
            addCardEventHandlers(card, article);
        }
    }
    
    /**
     * Adds event handlers to the buttons in an article card.
     * 
     * @param card the article card
     * @param article the associated article
     */
    private void addCardEventHandlers(Node card, UserCreatedArticle article) {
        // Add handlers for read, edit, delete, approve buttons here
        if (card instanceof VBox) {
            VBox cardBox = (VBox) card;
            
            // Get the button box (usually the last child)
            Node buttonBoxNode = cardBox.getChildren().get(cardBox.getChildren().size() - 1);
            
            if (buttonBoxNode instanceof Node) {
                Node buttonBox = (Node) buttonBoxNode;
                
                // Add handlers for each button
                if (buttonBox instanceof javafx.scene.layout.HBox) {
                    javafx.scene.layout.HBox buttonHBox = (javafx.scene.layout.HBox) buttonBox;
                    
                    for (Node buttonNode : buttonHBox.getChildren()) {
                        if (buttonNode instanceof Button) {
                            Button button = (Button) buttonNode;
                            
                            switch (button.getText()) {
                                case "Read Full Article":
                                    button.setOnAction(event -> showFullArticle(article));
                                    break;
                                case "Edit":
                                    button.setOnAction(event -> editArticle(article));
                                    break;
                                case "Delete":
                                    button.setOnAction(event -> deleteArticle(article));
                                    break;
                                case "Approve":
                                    button.setOnAction(event -> approveArticle(article, true));
                                    break;
                                case "Unapprove":
                                    button.setOnAction(event -> approveArticle(article, false));
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Shows the full article view.
     * 
     * @param article the article to display
     */
    private void showFullArticle(UserCreatedArticle article) {
        Node fullArticleView = view.createFullArticleView(article);
        view.getArticlesContainer().getChildren().clear();
        view.getArticlesContainer().getChildren().add(fullArticleView);
    }
    
    /**
     * Opens the article edit form.
     * 
     * @param article the article to edit
     */
    private void editArticle(UserCreatedArticle article) {
        // TODO: Implement edit functionality
        // Similar to createArticleForm but pre-filled with article data
        showAlert(AlertType.INFORMATION, "Edit Article", 
                "Edit functionality", "This feature will be implemented soon.");
    }
    
    /**
     * Deletes an article after confirmation.
     * 
     * @param article the article to delete
     */
    private void deleteArticle(UserCreatedArticle article) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Are you sure you want to delete this article?");
        confirmAlert.setContentText("This action cannot be undone.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                executorService.submit(() -> {
                    boolean success = service.deleteArticle(article.getId(), currentUser);
                    
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            showAlert(AlertType.INFORMATION, "Success", 
                                    "Article deleted", "The article has been deleted successfully.");
                            
                            // Reload the appropriate view
                            if (currentUser.isAdmin()) {
                                loadAllArticles(false);
                            } else {
                                loadMyArticles();
                            }
                        } else {
                            showAlert(AlertType.ERROR, "Error", 
                                    "Failed to delete article", "An error occurred while deleting the article.");
                        }
                    });
                });
            }
        });
    }
    
    /**
     * Approves or unapproves an article.
     * 
     * @param article the article to approve/unapprove
     * @param approve whether to approve or unapprove
     */
    private void approveArticle(UserCreatedArticle article, boolean approve) {
        if (!currentUser.isAdmin()) {
            showAlert(AlertType.ERROR, "Error", 
                    "Unauthorized", "Only administrators can approve or unapprove articles.");
            return;
        }
        
        executorService.submit(() -> {
            boolean success = approve ? 
                    service.approveArticle(article.getId(), currentUser) : 
                    service.unapproveArticle(article.getId(), currentUser);
            
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    showAlert(AlertType.INFORMATION, "Success", 
                            approve ? "Article approved" : "Article unapproved", 
                            approve ? "The article is now published." : "The article is no longer published.");
                    
                    loadAllArticles(false);
                } else {
                    showAlert(AlertType.ERROR, "Error", 
                            "Failed to update article status", 
                            "An error occurred while updating the article status.");
                }
            });
        });
    }
    
    /**
     * Creates a message to display when no articles are available.
     * 
     * @param message the message to display
     * @return the message node
     */
    private Node createNoArticlesMessage(String message) {
        VBox messageBox = new VBox();
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPadding(new javafx.geometry.Insets(50));
        
        javafx.scene.control.Label label = new javafx.scene.control.Label(message);
        label.getStyleClass().add("no-articles-message");
        label.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.NORMAL, 16));
        
        messageBox.getChildren().add(label);
        return messageBox;
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
     * Returns to the main news view.
     */
    private void goBackToMain() {
        mainContainer.getChildren().remove(view.getRoot());
    }
    
    /**
     * Gets the user article view.
     * 
     * @return the view
     */
    public UserArticleView getView() {
        return view;
    }
    
    /**
     * Updates the theme of the view.
     * 
     * @param isDarkTheme whether dark theme is enabled
     */
    public void updateTheme(boolean isDarkTheme) {
        view.setDarkTheme(isDarkTheme);
    }
    
    /**
     * Loads the view with initial data.
     */
    public void loadView() {
        // Default to load all approved articles for regular users,
        // or all articles for admins
        loadAllArticles(currentUser.isAdmin() ? false : true);
    }
    
    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}