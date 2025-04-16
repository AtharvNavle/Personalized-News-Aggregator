package com.newsaggregator.service;

import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserCreatedArticle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for handling user-created articles.
 */
public class UserArticleService {
    private static final Logger LOGGER = Logger.getLogger(UserArticleService.class.getName());
    private final DatabaseService databaseService;
    private static final String IMAGE_DIRECTORY = "data/images";
    
    /**
     * Constructor.
     */
    public UserArticleService() {
        this.databaseService = DatabaseService.getInstance();
        createImageDirectoryIfNotExists();
    }
    
    /**
     * Creates the directory for storing article images if it doesn't exist.
     */
    private void createImageDirectoryIfNotExists() {
        Path imageDir = Paths.get(IMAGE_DIRECTORY);
        if (!Files.exists(imageDir)) {
            try {
                Files.createDirectories(imageDir);
                LOGGER.info("Created image directory: " + imageDir.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to create image directory", e);
            }
        }
    }
    
    /**
     * Creates a new user-created article.
     *
     * @param user        the user creating the article
     * @param title       the title of the article
     * @param description the description/summary of the article
     * @param content     the full content of the article
     * @param imageFile   the image file for the article (optional)
     * @param category    the category of the article
     * @return the created article, or null if creation failed
     */
    public UserCreatedArticle createArticle(User user, String title, String description, 
                                          String content, File imageFile, Category category) {
        String imageUrl = null;
        
        // If an image is provided, save it to the image directory
        if (imageFile != null) {
            try {
                imageUrl = saveImage(imageFile);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to save article image", e);
                return null;
            }
        }
        
        UserCreatedArticle article = new UserCreatedArticle(
                user.getId(),
                user.getUsername(),
                title,
                description,
                content,
                imageUrl,
                category
        );
        
        return databaseService.createUserArticle(article);
    }
    
    /**
     * Saves an image file to the image directory.
     *
     * @param imageFile the image file to save
     * @return the URL path to the saved image
     * @throws IOException if an error occurs during saving
     */
    private String saveImage(File imageFile) throws IOException {
        // Generate a unique filename for the image
        String filename = UUID.randomUUID() + "_" + imageFile.getName();
        Path targetPath = Paths.get(IMAGE_DIRECTORY, filename);
        
        // Copy the image file to the target path
        Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return the relative path to the image
        return IMAGE_DIRECTORY + "/" + filename;
    }
    
    /**
     * Updates an existing user-created article.
     *
     * @param articleId   the ID of the article to update
     * @param title       the new title
     * @param description the new description
     * @param content     the new content
     * @param imageFile   the new image file (optional)
     * @param category    the new category
     * @param user        the user making the update
     * @return the updated article, or null if update failed
     */
    public UserCreatedArticle updateArticle(int articleId, String title, String description,
                                          String content, File imageFile, Category category, User user) {
        // Retrieve the existing article
        UserCreatedArticle article = databaseService.getUserArticleById(articleId);
        
        // Check if the article exists and if the user is authorized to edit it
        if (article == null || (article.getAuthorId() != user.getId() && !user.isAdmin())) {
            return null;
        }
        
        // If a new image is provided, save it
        if (imageFile != null) {
            try {
                // Delete the old image if it exists
                if (article.getImageUrl() != null) {
                    deleteImage(article.getImageUrl());
                }
                
                article.setImageUrl(saveImage(imageFile));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to update article image", e);
                return null;
            }
        }
        
        // Update article details
        article.setTitle(title);
        article.setDescription(description);
        article.setContent(content);
        article.setCategory(category);
        
        // Persist the changes
        boolean updated = databaseService.updateUserArticle(article);
        return updated ? article : null;
    }
    
    /**
     * Deletes an image file.
     *
     * @param imageUrl the URL path to the image
     */
    private void deleteImage(String imageUrl) {
        try {
            Path imagePath = Paths.get(imageUrl);
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to delete image: " + imageUrl, e);
        }
    }
    
    /**
     * Deletes a user-created article.
     *
     * @param articleId the ID of the article to delete
     * @param user      the user performing the deletion
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteArticle(int articleId, User user) {
        // Retrieve the article
        UserCreatedArticle article = databaseService.getUserArticleById(articleId);
        
        // Check if the article exists and if the user is authorized to delete it
        if (article == null || (article.getAuthorId() != user.getId() && !user.isAdmin())) {
            return false;
        }
        
        // Delete the article image if it exists
        if (article.getImageUrl() != null) {
            deleteImage(article.getImageUrl());
        }
        
        // Delete the article from the database
        return databaseService.deleteUserArticle(articleId);
    }
    
    /**
     * Retrieves a user-created article by its ID.
     *
     * @param articleId the ID of the article
     * @return the article, or null if not found
     */
    public UserCreatedArticle getArticleById(int articleId) {
        return databaseService.getUserArticleById(articleId);
    }
    
    /**
     * Retrieves all user-created articles.
     *
     * @param approvedOnly whether to retrieve only approved articles
     * @return a list of all user-created articles
     */
    public List<UserCreatedArticle> getAllArticles(boolean approvedOnly) {
        return databaseService.getAllUserArticles(approvedOnly);
    }
    
    /**
     * Retrieves all articles created by a specific user.
     *
     * @param userId the ID of the user
     * @return a list of the user's articles
     */
    public List<UserCreatedArticle> getArticlesByUser(int userId) {
        return databaseService.getUserArticlesByAuthor(userId);
    }
    
    /**
     * Approves a user-created article.
     *
     * @param articleId the ID of the article to approve
     * @param user      the user performing the approval
     * @return true if approval was successful, false otherwise
     */
    public boolean approveArticle(int articleId, User user) {
        // Only admins can approve articles
        if (!user.isAdmin()) {
            return false;
        }
        
        return databaseService.setUserArticleApproval(articleId, true);
    }
    
    /**
     * Unapproves a user-created article.
     *
     * @param articleId the ID of the article to unapprove
     * @param user      the user performing the unapproval
     * @return true if unapproval was successful, false otherwise
     */
    public boolean unapproveArticle(int articleId, User user) {
        // Only admins can unapprove articles
        if (!user.isAdmin()) {
            return false;
        }
        
        return databaseService.setUserArticleApproval(articleId, false);
    }
}