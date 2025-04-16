package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing user-related operations.
 */
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private final DatabaseService databaseService;
    private User currentUser;

    /**
     * Constructor for UserService.
     */
    public UserService() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username the username
     * @param password the password
     * @return true if authentication is successful, false otherwise
     */
    public boolean login(String username, String password) {
        User user = databaseService.authenticateUser(username, password);
        if (user != null) {
            currentUser = user;
            
            // Ensure user has a preferred language set (for translation feature)
            if (currentUser.getPreferredLanguage() == null || currentUser.getPreferredLanguage().isEmpty()) {
                // Set a default language (fr = French) for translation demo
                currentUser.setPreferredLanguage("fr");
                databaseService.updateUser(currentUser);
                LOGGER.info("Set default preferred language for user: " + username);
            }
            
            LOGGER.info("User logged in: " + username);
            return true;
        }
        LOGGER.warning("Failed login attempt for username: " + username);
        return false;
    }

    /**
     * Registers a new user.
     *
     * @param username the username
     * @param email    the email
     * @param password the password
     * @param isAdmin  whether the user is an admin
     * @return true if registration is successful, false otherwise
     */
    public boolean register(String username, String email, String password, boolean isAdmin) {
        if (databaseService.usernameExists(username)) {
            LOGGER.warning("Registration failed: Username already exists: " + username);
            return false;
        }
        
        if (databaseService.emailExists(email)) {
            LOGGER.warning("Registration failed: Email already exists: " + email);
            return false;
        }
        
        User user = databaseService.registerUser(username, email, password, isAdmin);
        if (user != null) {
            currentUser = user;
            LOGGER.info("User registered: " + username);
            return true;
        }
        
        LOGGER.warning("Registration failed for username: " + username);
        return false;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        if (currentUser != null) {
            LOGGER.info("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Gets the current logged-in user.
     *
     * @return the current user or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Updates the current user's profile information.
     *
     * @param username           the new username
     * @param email              the new email
     * @param preferredLanguage  the preferred language
     * @param preferredCountry   the preferred country
     * @param preferredCategories the preferred categories
     * @return true if the update is successful, false otherwise
     */
    public boolean updateProfile(String username, String email, String preferredLanguage, 
                                String preferredCountry, Set<Category> preferredCategories) {
        if (currentUser == null) {
            LOGGER.warning("Cannot update profile: No user is logged in");
            return false;
        }
        
        // Check if the new username is already taken by someone else
        if (!username.equals(currentUser.getUsername()) && databaseService.usernameExists(username)) {
            LOGGER.warning("Profile update failed: Username already exists: " + username);
            return false;
        }
        
        // Check if the new email is already taken by someone else
        if (!email.equals(currentUser.getEmail()) && databaseService.emailExists(email)) {
            LOGGER.warning("Profile update failed: Email already exists: " + email);
            return false;
        }
        
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setPreferredLanguage(preferredLanguage);
        currentUser.setPreferredCountry(preferredCountry);
        currentUser.setPreferredCategories(preferredCategories);
        
        boolean updated = databaseService.updateUser(currentUser);
        if (updated) {
            LOGGER.info("Profile updated for user: " + currentUser.getUsername());
        } else {
            LOGGER.warning("Profile update failed for user: " + currentUser.getUsername());
        }
        
        return updated;
    }

    /**
     * Updates the current user's password.
     *
     * @param currentPassword the current password
     * @param newPassword     the new password
     * @return true if the password update is successful, false otherwise
     */
    public boolean updatePassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            LOGGER.warning("Cannot update password: No user is logged in");
            return false;
        }
        
        // Verify current password
        User user = databaseService.authenticateUser(currentUser.getUsername(), currentPassword);
        if (user == null) {
            LOGGER.warning("Password update failed: Current password is incorrect");
            return false;
        }
        
        boolean updated = databaseService.updateUserPassword(currentUser.getId(), newPassword);
        if (updated) {
            LOGGER.info("Password updated for user: " + currentUser.getUsername());
        } else {
            LOGGER.warning("Password update failed for user: " + currentUser.getUsername());
        }
        
        return updated;
    }

    /**
     * Saves an article for the current user.
     *
     * @param article the article to save
     * @return true if the article is saved successfully, false otherwise
     */
    public boolean saveArticle(Article article) {
        if (currentUser == null) {
            LOGGER.warning("Cannot save article: No user is logged in");
            return false;
        }
        
        boolean saved = databaseService.saveArticle(currentUser, article);
        if (saved) {
            LOGGER.info("Article saved for user: " + currentUser.getUsername());
        } else {
            LOGGER.warning("Failed to save article for user: " + currentUser.getUsername());
        }
        
        return saved;
    }
    
    /**
     * Updates a user's information.
     *
     * @param user the user to update
     * @return true if the update is successful, false otherwise
     */
    public boolean updateUser(User user) {
        if (user == null) {
            LOGGER.warning("Cannot update user: User is null");
            return false;
        }
        
        boolean updated = databaseService.updateUser(user);
        if (updated) {
            LOGGER.info("User updated: " + user.getUsername());
            
            // If updating the current user, refresh the current user reference
            if (currentUser != null && currentUser.getId() == user.getId()) {
                currentUser = user;
            }
        } else {
            LOGGER.warning("Failed to update user: " + user.getUsername());
        }
        
        return updated;
    }
    
    /**
     * Saves an article for a specified user.
     *
     * @param user the user
     * @param article the article to save
     * @return true if the article is saved successfully, false otherwise
     */
    public boolean saveArticle(User user, Article article) {
        if (user == null) {
            LOGGER.warning("Cannot save article: User is null");
            return false;
        }
        
        boolean saved = databaseService.saveArticle(user, article);
        if (saved) {
            LOGGER.info("Article saved for user: " + user.getUsername());
        } else {
            LOGGER.warning("Failed to save article for user: " + user.getUsername());
        }
        
        return saved;
    }

    /**
     * Removes a saved article for the current user.
     *
     * @param article the article to remove
     * @return true if the article is removed successfully, false otherwise
     */
    public boolean removeSavedArticle(Article article) {
        if (currentUser == null) {
            LOGGER.warning("Cannot remove article: No user is logged in");
            return false;
        }
        
        boolean removed = databaseService.removeSavedArticle(currentUser, article.getId());
        if (removed) {
            LOGGER.info("Article removed for user: " + currentUser.getUsername());
        } else {
            LOGGER.warning("Failed to remove article for user: " + currentUser.getUsername());
        }
        
        return removed;
    }
    
    /**
     * Removes a saved article for a specified user.
     *
     * @param user the user
     * @param article the article to remove
     * @return true if the article is removed successfully, false otherwise
     */
    public boolean unsaveArticle(User user, Article article) {
        if (user == null) {
            LOGGER.warning("Cannot remove article: User is null");
            return false;
        }
        
        boolean removed = databaseService.removeSavedArticle(user, article.getId());
        if (removed) {
            LOGGER.info("Article removed for user: " + user.getUsername());
        } else {
            LOGGER.warning("Failed to remove article for user: " + user.getUsername());
        }
        
        return removed;
    }

    /**
     * Gets all saved articles for the current user.
     *
     * @return a list of saved articles
     */
    public List<Article> getSavedArticles() {
        if (currentUser == null) {
            LOGGER.warning("Cannot get saved articles: No user is logged in");
            return List.of();
        }
        
        return currentUser.getSavedArticles();
    }
    
    /**
     * Gets all saved articles for a specified user.
     * 
     * @param user the user whose saved articles to retrieve
     * @return a list of saved articles
     */
    public List<Article> getSavedArticles(User user) {
        if (user == null) {
            LOGGER.warning("Cannot get saved articles: User is null");
            return List.of();
        }
        
        return user.getSavedArticles();
    }

    /**
     * Gets all users (admin only).
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        if (currentUser == null || !currentUser.isAdmin()) {
            LOGGER.warning("Cannot get users: No admin user is logged in");
            return List.of();
        }
        
        return databaseService.getAllUsers();
    }

    /**
     * Deletes a user (admin only).
     *
     * @param userId the ID of the user to delete
     * @return true if the user is deleted successfully, false otherwise
     */
    public boolean deleteUser(int userId) {
        if (currentUser == null || !currentUser.isAdmin()) {
            LOGGER.warning("Cannot delete user: No admin user is logged in");
            return false;
        }
        
        // Prevent admin from deleting themselves
        if (userId == currentUser.getId()) {
            LOGGER.warning("Admin cannot delete themselves");
            return false;
        }
        
        boolean deleted = databaseService.deleteUser(userId);
        if (deleted) {
            LOGGER.info("User deleted by admin: " + currentUser.getUsername());
        } else {
            LOGGER.warning("Failed to delete user by admin: " + currentUser.getUsername());
        }
        
        return deleted;
    }

    /**
     * Updates a user's admin status (admin only).
     *
     * @param userId  the ID of the user to update
     * @param isAdmin the new admin status
     * @return true if the update is successful, false otherwise
     */
    public boolean updateUserAdminStatus(int userId, boolean isAdmin) {
        if (currentUser == null || !currentUser.isAdmin()) {
            LOGGER.warning("Cannot update user admin status: No admin user is logged in");
            return false;
        }
        
        List<User> users = databaseService.getAllUsers();
        User userToUpdate = null;
        
        for (User user : users) {
            if (user.getId() == userId) {
                userToUpdate = user;
                break;
            }
        }
        
        if (userToUpdate == null) {
            LOGGER.warning("User not found for ID: " + userId);
            return false;
        }
        
        userToUpdate.setAdmin(isAdmin);
        boolean updated = databaseService.updateUser(userToUpdate);
        
        if (updated) {
            LOGGER.info("Admin status updated for user: " + userToUpdate.getUsername());
        } else {
            LOGGER.warning("Failed to update admin status for user: " + userToUpdate.getUsername());
        }
        
        return updated;
    }
    
    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return databaseService.usernameExists(username);
    }
}
