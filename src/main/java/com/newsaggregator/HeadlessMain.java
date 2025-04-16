package com.newsaggregator;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.service.DatabaseService;
import com.newsaggregator.service.NewsService;
import com.newsaggregator.service.UserService;
import com.newsaggregator.util.PasswordHasher;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;

/**
 * Headless version of the News Aggregator application for environments
 * where JavaFX GUI cannot be displayed (like Replit).
 */
public class HeadlessMain {

    private static UserService userService;
    private static NewsService newsService;
    private static Scanner scanner;
    private static User currentUser;

    public static void main(String[] args) {
        System.out.println("Starting News Aggregator (Headless Version)...");
        try {
            // Initialize services
            initializeServices();
            
            // In Replit environment, always run in test mode
            // This avoids issues with interactive console input
            boolean testMode = true;
            
            if (testMode) {
                System.out.println("Running in non-interactive test mode...");
                runTestMode();
            } else {
                // Start the CLI interface - only for local development
                scanner = new Scanner(System.in);
                showMainMenu();
            }
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close database connection when application exits
            if (scanner != null) {
                scanner.close();
            }
            DatabaseService.getInstance().closeConnection();
        }
    }
    
    /**
     * Runs a non-interactive test mode that demonstrates key functionality
     */
    private static void runTestMode() {
        System.out.println("\n==== Running Test Mode ====");
        
        // Test login with admin user
        System.out.println("\nTesting admin login:");
        boolean loginSuccess = userService.login("admin", "admin123");
        if (loginSuccess) {
            currentUser = userService.getCurrentUser();
            System.out.println("Admin login successful: " + currentUser.getUsername() + " (Admin: " + currentUser.isAdmin() + ")");
            
            // Test fetching news
            System.out.println("\nFetching top headlines:");
            try {
                List<Article> articles = newsService.getTopHeadlines(null, null, 1, currentUser);
                if (articles.isEmpty()) {
                    System.out.println("No articles found. This could be due to API limits or network issues.");
                } else {
                    System.out.println("Successfully fetched " + articles.size() + " articles");
                    // Display first article details
                    Article firstArticle = articles.get(0);
                    System.out.println("Sample article: " + firstArticle.getTitle());
                    System.out.println("Source: " + firstArticle.getSource());
                    System.out.println("Published: " + firstArticle.getPublishedAt());
                    
                    // Test saving an article
                    boolean saved = userService.saveArticle(currentUser, firstArticle);
                    System.out.println("Article saved successfully: " + saved);
                    
                    // Test retrieving saved articles
                    List<Article> savedArticles = userService.getSavedArticles(currentUser);
                    System.out.println("Retrieved " + savedArticles.size() + " saved articles");
                }
            } catch (Exception e) {
                System.err.println("Error fetching top headlines: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Test user management
            System.out.println("\nListing all users:");
            List<User> allUsers = userService.getAllUsers();
            for (User user : allUsers) {
                System.out.println("- " + user.getUsername() + " (" + user.getEmail() + ")" + 
                        (user.isAdmin() ? " [ADMIN]" : ""));
            }
            
            // Test creating a regular user
            System.out.println("\nCreating test user:");
            if (!userService.usernameExists("testuser")) {
                boolean registerSuccess = userService.register("testuser", "test@example.com", "password123", false);
                System.out.println("Test user creation: " + (registerSuccess ? "Success" : "Failed"));
            } else {
                System.out.println("Test user already exists");
            }
        } else {
            System.out.println("Admin login failed - check database initialization");
        }
        
        System.out.println("\n==== Test Mode Complete ====");
    }
    
    /**
     * Initializes all the required services
     */
    private static void initializeServices() {
        System.out.println("Initializing services...");
        
        // Initialize database
        System.out.println("Initializing database...");
        DatabaseService.getInstance().initializeDatabase();
        
        // Initialize user service
        userService = new UserService();
        
        // Initialize news service (API key is already hardcoded in NewsService)
        newsService = new NewsService();
        
        System.out.println("Services initialized successfully");
    }
    
    /**
     * Displays the main menu
     */
    private static void showMainMenu() {
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n==== News Aggregator Main Menu ====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice (1-3): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    exit = true;
                    System.out.println("Exiting application...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Handles user login
     */
    private static void login() {
        System.out.println("\n==== Login ====");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password are required.");
            return;
        }
        
        // Attempt to login
        boolean success = userService.login(username, password);
        
        if (success) {
            currentUser = userService.getCurrentUser();
            System.out.println("Login successful. Welcome, " + currentUser.getUsername() + "!");
            if (currentUser.isAdmin()) {
                showAdminMenu();
            } else {
                showUserMenu();
            }
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }
    
    /**
     * Handles user registration
     */
    private static void register() {
        System.out.println("\n==== Register ====");
        System.out.print("Username (at least 4 characters): ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password (at least 6 characters): ");
        String password = scanner.nextLine();
        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();
        
        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }
        
        if (username.length() < 4) {
            System.out.println("Username must be at least 4 characters long.");
            return;
        }
        
        if (!isValidEmail(email)) {
            System.out.println("Please enter a valid email address.");
            return;
        }
        
        if (password.length() < 6) {
            System.out.println("Password must be at least 6 characters long.");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }
        
        // Attempt to register
        boolean success = userService.register(username, email, password, false);
        
        if (success) {
            System.out.println("Registration successful. You can now login.");
        } else {
            System.out.println("Registration failed. Username or email already exists.");
        }
    }
    
    /**
     * Displays the user menu
     */
    private static void showUserMenu() {
        boolean logout = false;
        
        while (!logout) {
            System.out.println("\n==== User Menu ====");
            System.out.println("1. Fetch Top Headlines");
            System.out.println("2. Search News");
            System.out.println("3. View News By Category");
            System.out.println("4. Manage Preferences");
            System.out.println("5. View Saved Articles");
            System.out.println("6. Logout");
            System.out.print("Enter your choice (1-6): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    fetchTopHeadlines();
                    break;
                case "2":
                    searchNews();
                    break;
                case "3":
                    viewNewsByCategory();
                    break;
                case "4":
                    managePreferences();
                    break;
                case "5":
                    viewSavedArticles();
                    break;
                case "6":
                    logout = true;
                    currentUser = null;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Displays the admin menu
     */
    private static void showAdminMenu() {
        boolean logout = false;
        
        while (!logout) {
            System.out.println("\n==== Admin Menu ====");
            System.out.println("1. Fetch Top Headlines");
            System.out.println("2. Search News");
            System.out.println("3. View News By Category");
            System.out.println("4. Manage Preferences");
            System.out.println("5. View Saved Articles");
            System.out.println("6. View All Users");
            System.out.println("7. Logout");
            System.out.print("Enter your choice (1-7): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    fetchTopHeadlines();
                    break;
                case "2":
                    searchNews();
                    break;
                case "3":
                    viewNewsByCategory();
                    break;
                case "4":
                    managePreferences();
                    break;
                case "5":
                    viewSavedArticles();
                    break;
                case "6":
                    viewAllUsers();
                    break;
                case "7":
                    logout = true;
                    currentUser = null;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Fetches and displays top headlines
     */
    private static void fetchTopHeadlines() {
        System.out.println("\n==== Top Headlines ====");
        try {
            // Using null for category (all categories), page 1, and current user's preferences
            List<Article> articles = newsService.getTopHeadlines(null, null, 1, currentUser);
            
            displayArticles(articles);
        } catch (Exception e) {
            System.err.println("Error fetching top headlines: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Searches for news articles
     */
    private static void searchNews() {
        System.out.println("\n==== Search News ====");
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim();
        
        if (query.isEmpty()) {
            System.out.println("Search query cannot be empty.");
            return;
        }
        
        try {
            // Using the query, null for category, page 1, and current user
            List<Article> articles = newsService.searchNews(query, null, 1, currentUser);
            
            displayArticles(articles);
        } catch (Exception e) {
            System.err.println("Error searching news: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Views news by category
     */
    private static void viewNewsByCategory() {
        System.out.println("\n==== News By Category ====");
        System.out.println("Available categories:");
        
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i+1) + ". " + categories[i].getDisplayName());
        }
        
        System.out.print("Select a category (1-" + categories.length + "): ");
        String categoryChoice = scanner.nextLine();
        
        try {
            int index = Integer.parseInt(categoryChoice) - 1;
            if (index >= 0 && index < categories.length) {
                Category selectedCategory = categories[index];
                
                // Use getTopHeadlines with the selected category
                List<Article> articles = newsService.getTopHeadlines(
                        selectedCategory, null, 1, currentUser);
                
                System.out.println("\nNews in category: " + selectedCategory.getDisplayName());
                displayArticles(articles);
            } else {
                System.out.println("Invalid category selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("Error fetching news by category: " + e.getMessage());
        }
    }
    
    /**
     * Manages user preferences
     */
    private static void managePreferences() {
        boolean done = false;
        
        while (!done) {
            System.out.println("\n==== Manage Preferences ====");
            System.out.println("1. Change Preferred Country");
            System.out.println("2. Change Preferred Language");
            System.out.println("3. Manage Preferred Categories");
            System.out.println("4. Back");
            System.out.print("Enter your choice (1-4): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    changePreferredCountry();
                    break;
                case "2":
                    changePreferredLanguage();
                    break;
                case "3":
                    managePreferredCategories();
                    break;
                case "4":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Changes preferred country
     */
    private static void changePreferredCountry() {
        System.out.println("\n==== Change Preferred Country ====");
        System.out.println("Available countries:");
        System.out.println("1. United States (us)");
        System.out.println("2. United Kingdom (gb)");
        System.out.println("3. Canada (ca)");
        System.out.println("4. Australia (au)");
        System.out.println("5. Germany (de)");
        System.out.println("6. France (fr)");
        System.out.println("7. India (in)");
        System.out.print("Select a country (1-7): ");
        
        String countryChoice = scanner.nextLine();
        String countryCode;
        
        switch (countryChoice) {
            case "1":
                countryCode = "us";
                break;
            case "2":
                countryCode = "gb";
                break;
            case "3":
                countryCode = "ca";
                break;
            case "4":
                countryCode = "au";
                break;
            case "5":
                countryCode = "de";
                break;
            case "6":
                countryCode = "fr";
                break;
            case "7":
                countryCode = "in";
                break;
            default:
                System.out.println("Invalid choice. No changes made.");
                return;
        }
        
        currentUser.setPreferredCountry(countryCode);
        userService.updateUser(currentUser);
        System.out.println("Preferred country updated successfully.");
    }
    
    /**
     * Changes preferred language
     */
    private static void changePreferredLanguage() {
        System.out.println("\n==== Change Preferred Language ====");
        System.out.println("Available languages:");
        System.out.println("1. English (en)");
        System.out.println("2. German (de)");
        System.out.println("3. French (fr)");
        System.out.println("4. Spanish (es)");
        System.out.println("5. Italian (it)");
        System.out.println("6. Portuguese (pt)");
        System.out.print("Select a language (1-6): ");
        
        String languageChoice = scanner.nextLine();
        String languageCode;
        
        switch (languageChoice) {
            case "1":
                languageCode = "en";
                break;
            case "2":
                languageCode = "de";
                break;
            case "3":
                languageCode = "fr";
                break;
            case "4":
                languageCode = "es";
                break;
            case "5":
                languageCode = "it";
                break;
            case "6":
                languageCode = "pt";
                break;
            default:
                System.out.println("Invalid choice. No changes made.");
                return;
        }
        
        currentUser.setPreferredLanguage(languageCode);
        userService.updateUser(currentUser);
        System.out.println("Preferred language updated successfully.");
    }
    
    /**
     * Manages preferred categories
     */
    private static void managePreferredCategories() {
        boolean done = false;
        
        while (!done) {
            System.out.println("\n==== Manage Preferred Categories ====");
            System.out.println("Current preferred categories:");
            
            Set<Category> currentCategories = currentUser.getPreferredCategories();
            if (currentCategories.isEmpty()) {
                System.out.println("None");
            } else {
                for (Category category : currentCategories) {
                    System.out.println("- " + category.getDisplayName());
                }
            }
            
            System.out.println("\n1. Add Category");
            System.out.println("2. Remove Category");
            System.out.println("3. Back");
            System.out.print("Enter your choice (1-3): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    addPreferredCategory();
                    break;
                case "2":
                    removePreferredCategory();
                    break;
                case "3":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Adds a preferred category
     */
    private static void addPreferredCategory() {
        System.out.println("\n==== Add Preferred Category ====");
        System.out.println("Available categories:");
        
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i+1) + ". " + categories[i].getDisplayName());
        }
        
        System.out.print("Select a category to add (1-" + categories.length + "): ");
        String categoryChoice = scanner.nextLine();
        
        try {
            int index = Integer.parseInt(categoryChoice) - 1;
            if (index >= 0 && index < categories.length) {
                Category selectedCategory = categories[index];
                
                Set<Category> userCategories = currentUser.getPreferredCategories();
                if (userCategories == null) {
                    userCategories = new HashSet<>();
                }
                
                if (userCategories.contains(selectedCategory)) {
                    System.out.println("This category is already in your preferences.");
                } else {
                    userCategories.add(selectedCategory);
                    currentUser.setPreferredCategories(userCategories);
                    userService.updateUser(currentUser);
                    System.out.println("Category added successfully.");
                }
            } else {
                System.out.println("Invalid category selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("Error adding category: " + e.getMessage());
        }
    }
    
    /**
     * Removes a preferred category
     */
    private static void removePreferredCategory() {
        System.out.println("\n==== Remove Preferred Category ====");
        
        Set<Category> userCategories = currentUser.getPreferredCategories();
        if (userCategories == null || userCategories.isEmpty()) {
            System.out.println("You don't have any preferred categories to remove.");
            return;
        }
        
        Category[] categories = userCategories.toArray(new Category[0]);
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i+1) + ". " + categories[i].getDisplayName());
        }
        
        System.out.print("Select a category to remove (1-" + categories.length + "): ");
        String categoryChoice = scanner.nextLine();
        
        try {
            int index = Integer.parseInt(categoryChoice) - 1;
            if (index >= 0 && index < categories.length) {
                Category selectedCategory = categories[index];
                
                userCategories.remove(selectedCategory);
                currentUser.setPreferredCategories(userCategories);
                userService.updateUser(currentUser);
                System.out.println("Category removed successfully.");
            } else {
                System.out.println("Invalid category selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("Error removing category: " + e.getMessage());
        }
    }
    
    /**
     * Views saved articles
     */
    private static void viewSavedArticles() {
        System.out.println("\n==== Saved Articles ====");
        
        try {
            List<Article> savedArticles = userService.getSavedArticles(currentUser);
            
            if (savedArticles.isEmpty()) {
                System.out.println("You don't have any saved articles.");
                return;
            }
            
            displayArticles(savedArticles);
            
            System.out.print("\nDo you want to unsave an article? (y/n): ");
            String choice = scanner.nextLine().toLowerCase();
            
            if (choice.equals("y")) {
                System.out.print("Enter the number of the article to unsave: ");
                String articleIndexStr = scanner.nextLine();
                
                try {
                    int articleIndex = Integer.parseInt(articleIndexStr) - 1;
                    if (articleIndex >= 0 && articleIndex < savedArticles.size()) {
                        Article articleToUnsave = savedArticles.get(articleIndex);
                        userService.unsaveArticle(currentUser, articleToUnsave);
                        System.out.println("Article unsaved successfully.");
                    } else {
                        System.out.println("Invalid article number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error viewing saved articles: " + e.getMessage());
        }
    }
    
    /**
     * Views all users (admin only)
     */
    private static void viewAllUsers() {
        System.out.println("\n==== All Users ====");
        
        try {
            List<User> allUsers = userService.getAllUsers();
            
            if (allUsers.isEmpty()) {
                System.out.println("No users found.");
                return;
            }
            
            for (int i = 0; i < allUsers.size(); i++) {
                User user = allUsers.get(i);
                System.out.println((i+1) + ". " + user.getUsername() + 
                        " (" + user.getEmail() + ")" + 
                        (user.isAdmin() ? " [ADMIN]" : ""));
            }
            
            System.out.print("\nDo you want to delete a user? (y/n): ");
            String choice = scanner.nextLine().toLowerCase();
            
            if (choice.equals("y")) {
                System.out.print("Enter the number of the user to delete: ");
                String userIndexStr = scanner.nextLine();
                
                try {
                    int userIndex = Integer.parseInt(userIndexStr) - 1;
                    if (userIndex >= 0 && userIndex < allUsers.size()) {
                        User userToDelete = allUsers.get(userIndex);
                        
                        // Don't allow deleting yourself
                        if (userToDelete.getId() == currentUser.getId()) {
                            System.out.println("You cannot delete yourself.");
                            return;
                        }
                        
                        userService.deleteUser(userToDelete.getId());
                        System.out.println("User deleted successfully.");
                    } else {
                        System.out.println("Invalid user number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error viewing users: " + e.getMessage());
        }
    }
    
    /**
     * Displays a list of articles
     * 
     * @param articles the articles to display
     */
    private static void displayArticles(List<Article> articles) {
        if (articles.isEmpty()) {
            System.out.println("No articles found.");
            return;
        }
        
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            System.out.println("\n" + (i+1) + ". " + article.getTitle());
            System.out.println("   Source: " + article.getSource());
            if (article.getAuthor() != null && !article.getAuthor().isEmpty()) {
                System.out.println("   Author: " + article.getAuthor());
            }
            if (article.getDescription() != null && !article.getDescription().isEmpty()) {
                System.out.println("   " + article.getDescription());
            }
            System.out.println("   Published: " + article.getPublishedAt());
            System.out.println("   URL: " + article.getUrl());
        }
        
        System.out.print("\nDo you want to save an article? (y/n): ");
        String choice = scanner.nextLine().toLowerCase();
        
        if (choice.equals("y")) {
            System.out.print("Enter the number of the article to save: ");
            String articleIndexStr = scanner.nextLine();
            
            try {
                int articleIndex = Integer.parseInt(articleIndexStr) - 1;
                if (articleIndex >= 0 && articleIndex < articles.size()) {
                    Article articleToSave = articles.get(articleIndex);
                    userService.saveArticle(currentUser, articleToSave);
                    System.out.println("Article saved successfully.");
                } else {
                    System.out.println("Invalid article number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    /**
     * Validates an email address using a simple pattern
     * 
     * @param email the email to validate
     * @return true if the email is valid, false otherwise
     */
    private static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}