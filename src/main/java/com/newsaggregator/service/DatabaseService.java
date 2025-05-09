package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserCreatedArticle;
import com.newsaggregator.util.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides database operations for the news aggregator application.
 * Implements the Singleton pattern.
 */
public class DatabaseService {
    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
    
    // Database configuration - MySQL settings
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/newsaggregator?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String MYSQL_USER = "root";  // Update with your MySQL username
    private static final String MYSQL_PASSWORD = "root";  // Update with your MySQL password
    
    // H2 settings (for development/testing)
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String H2_URL = "jdbc:h2:mem:newsdb;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";
    
    // Set which database to use
    private static final boolean USE_MYSQL = true;  // Set to false to use H2
    
    // Active configuration
    private final String DB_DRIVER;
    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;

    private static DatabaseService instance;
    private Connection connection;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private DatabaseService() {
        // Configure appropriate database
        if (USE_MYSQL) {
            DB_DRIVER = MYSQL_DRIVER;
            DB_URL = MYSQL_URL;
            DB_USER = MYSQL_USER;
            DB_PASSWORD = MYSQL_PASSWORD;
        } else {
            DB_DRIVER = H2_DRIVER;
            DB_URL = H2_URL;
            DB_USER = H2_USER;
            DB_PASSWORD = H2_PASSWORD;
        }
        
        try {
            // Load appropriate JDBC driver
            Class.forName(DB_DRIVER);
            System.out.println("Database driver loaded successfully: " + DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Database driver not found", e);
            throw new RuntimeException("Database driver not found", e);
        }
    }

    /**
     * Gets the singleton instance of the DatabaseService.
     *
     * @return the DatabaseService instance
     */
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Gets a database connection.
     *
     * @return the Connection object
     * @throws SQLException if a database error occurs
     */
    private Connection getConnection() throws SQLException {
        try {
            System.out.println("Attempting to connect to database at: " + DB_URL);
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection successful");
            return conn;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Initializes the database connection and creates tables if they don't exist.
     */
    public void initializeDatabase() {
        try {
            System.out.println("Initializing database connection...");
            connection = getConnection();
            System.out.println("Creating tables if they don't exist...");
            createTablesIfNotExist();
            System.out.println("Creating admin user if it doesn't exist...");
            createAdminUserIfNotExists();
            System.out.println("Database initialization completed successfully");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Creates the necessary tables if they don't exist.
     */
    private void createTablesIfNotExist() {
        try (Statement stmt = connection.createStatement()) {
            if (USE_MYSQL) {
                // MySQL-specific table creation (handles TEXT, TINYINT for BOOLEAN, ENGINE settings)
                
                // Users table
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL UNIQUE, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "is_admin TINYINT(1) DEFAULT 0, " +  // MySQL uses TINYINT for BOOLEAN
                        "preferred_language VARCHAR(5) DEFAULT 'en', " +
                        "preferred_country VARCHAR(5) DEFAULT 'us', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");  // InnoDB supports transactions and foreign keys

                // User preferences table
                stmt.execute("CREATE TABLE IF NOT EXISTS user_preferences (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT NOT NULL, " +
                        "category VARCHAR(50) NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "CONSTRAINT unique_user_category UNIQUE (user_id, category)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // Saved articles table
                stmt.execute("CREATE TABLE IF NOT EXISTS saved_articles (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT NOT NULL, " +
                        "article_id VARCHAR(255) NOT NULL, " +
                        "title VARCHAR(255) NOT NULL, " +
                        "description TEXT, " +
                        "content LONGTEXT, " +  // LONGTEXT for potentially large content
                        "author VARCHAR(100), " +
                        "url VARCHAR(500), " +
                        "image_url VARCHAR(500), " +
                        "published_at TIMESTAMP NULL, " +  // NULL allows for missing dates
                        "source VARCHAR(100), " +
                        "category VARCHAR(50), " +
                        "saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "CONSTRAINT unique_user_article UNIQUE (user_id, article_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // User-created articles table
                stmt.execute("CREATE TABLE IF NOT EXISTS user_created_articles (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "author_id INT NOT NULL, " +
                        "title VARCHAR(255) NOT NULL, " +
                        "description TEXT, " +
                        "content LONGTEXT NOT NULL, " +  // LONGTEXT for potentially large content
                        "image_url VARCHAR(500), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "category VARCHAR(50) NOT NULL, " +
                        "approved TINYINT(1) DEFAULT 0, " +  // MySQL uses TINYINT for BOOLEAN
                        "FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            } else {
                // H2 table creation (original logic for H2 database)
                
                // Users table
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL UNIQUE, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "is_admin BOOLEAN DEFAULT FALSE, " +
                        "preferred_language VARCHAR(5) DEFAULT 'en', " +
                        "preferred_country VARCHAR(5) DEFAULT 'us', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                // User preferences table
                stmt.execute("CREATE TABLE IF NOT EXISTS user_preferences (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT NOT NULL, " +
                        "category VARCHAR(50) NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "CONSTRAINT unique_user_category UNIQUE (user_id, category))");

                // Saved articles table
                stmt.execute("CREATE TABLE IF NOT EXISTS saved_articles (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT NOT NULL, " +
                        "article_id VARCHAR(255) NOT NULL, " +
                        "title VARCHAR(255) NOT NULL, " +
                        "description TEXT, " +
                        "content TEXT, " +
                        "author VARCHAR(100), " +
                        "url VARCHAR(500), " +
                        "image_url VARCHAR(500), " +
                        "published_at TIMESTAMP, " +
                        "source VARCHAR(100), " +
                        "category VARCHAR(50), " +
                        "saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "CONSTRAINT unique_user_article UNIQUE (user_id, article_id))");

                // User-created articles table
                stmt.execute("CREATE TABLE IF NOT EXISTS user_created_articles (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "author_id INT NOT NULL, " +
                        "title VARCHAR(255) NOT NULL, " +
                        "description TEXT, " +
                        "content TEXT NOT NULL, " +
                        "image_url VARCHAR(500), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "category VARCHAR(50) NOT NULL, " +
                        "approved BOOLEAN DEFAULT FALSE, " +
                        "FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE)");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create tables", e);
            throw new RuntimeException("Failed to create tables", e);
        }
    }

    /**
     * Creates an admin user if one doesn't exist.
     */
    private void createAdminUserIfNotExists() {
        try {
            String query;
            if (USE_MYSQL) {
                // In MySQL TINYINT(1) is used as BOOLEAN, so we compare with 1
                query = "SELECT COUNT(*) FROM users WHERE is_admin = 1";
            } else {
                // In H2, we can use TRUE directly
                query = "SELECT COUNT(*) FROM users WHERE is_admin = TRUE";
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // Create admin user if none exists
                    String insertQuery;
                    if (USE_MYSQL) {
                        insertQuery = "INSERT INTO users (username, email, password, is_admin) VALUES (?, ?, ?, 1)";
                    } else {
                        insertQuery = "INSERT INTO users (username, email, password, is_admin) VALUES (?, ?, ?, TRUE)";
                    }
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, "admin");
                        insertStmt.setString(2, "admin@newsaggregator.com");
                        insertStmt.setString(3, PasswordHasher.hashPassword("admin123"));
                        insertStmt.executeUpdate();
                        LOGGER.info("Admin user created successfully");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create admin user", e);
            throw new RuntimeException("Failed to create admin user", e);
        }
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }

    /**
     * Authenticates a user with the given username and password.
     *
     * @param username the username
     * @param password the password
     * @return the authenticated User or null if authentication fails
     */
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");
                if (PasswordHasher.verifyPassword(password, storedPasswordHash)) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            storedPasswordHash,
                            rs.getBoolean("is_admin")
                    );
                    user.setPreferredLanguage(rs.getString("preferred_language"));
                    user.setPreferredCountry(rs.getString("preferred_country"));
                    
                    // Load user preferences
                    loadUserPreferences(user);
                    loadSavedArticles(user);
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Authentication error", e);
        }
        return null;
    }

    /**
     * Registers a new user.
     *
     * @param username the username
     * @param email    the email
     * @param password the password
     * @param isAdmin  whether the user is an admin
     * @return the newly registered User or null if registration fails
     */
    public User registerUser(String username, String email, String password, boolean isAdmin) {
        String query = "INSERT INTO users (username, email, password, is_admin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, PasswordHasher.hashPassword(password));
            pstmt.setBoolean(4, isAdmin);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    return new User(userId, username, email, PasswordHasher.hashPassword(password), isAdmin);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "User registration error", e);
        }
        return null;
    }

    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking username existence", e);
        }
        return false;
    }

    /**
     * Checks if an email already exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking email existence", e);
        }
        return false;
    }

    /**
     * Updates a user's information.
     *
     * @param user the user to update
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUser(User user) {
        String query = "UPDATE users SET username = ?, email = ?, is_admin = ?, preferred_language = ?, preferred_country = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setBoolean(3, user.isAdmin());
            pstmt.setString(4, user.getPreferredLanguage());
            pstmt.setString(5, user.getPreferredCountry());
            pstmt.setInt(6, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            // Update user preferences
            updateUserPreferences(user);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            return false;
        }
    }

    /**
     * Updates a user's password.
     *
     * @param userId   the ID of the user
     * @param password the new password
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUserPassword(int userId, String password) {
        String query = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, PasswordHasher.hashPassword(password));
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password", e);
            return false;
        }
    }

    /**
     * Retrieves a list of all users.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("is_admin")
                );
                user.setPreferredLanguage(rs.getString("preferred_language"));
                user.setPreferredCountry(rs.getString("preferred_country"));
                
                // Load user preferences
                loadUserPreferences(user);
                
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving users", e);
        }
        return users;
    }

    /**
     * Deletes a user.
     *
     * @param userId the ID of the user to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            return false;
        }
    }

    /**
     * Saves a user's preferred categories.
     *
     * @param user the user whose preferences to save
     */
    private void updateUserPreferences(User user) {
        try {
            // First delete existing preferences
            String deleteQuery = "DELETE FROM user_preferences WHERE user_id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, user.getId());
                deleteStmt.executeUpdate();
            }
            
            // Then insert new preferences
            if (!user.getPreferredCategories().isEmpty()) {
                String insertQuery = "INSERT INTO user_preferences (user_id, category) VALUES (?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    for (Category category : user.getPreferredCategories()) {
                        insertStmt.setInt(1, user.getId());
                        insertStmt.setString(2, category.getApiName());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user preferences", e);
        }
    }

    /**
     * Loads a user's preferred categories.
     *
     * @param user the user whose preferences to load
     */
    private void loadUserPreferences(User user) {
        String query = "SELECT category FROM user_preferences WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();
            
            Set<Category> categories = new HashSet<>();
            while (rs.next()) {
                categories.add(Category.fromApiName(rs.getString("category")));
            }
            user.setPreferredCategories(categories);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading user preferences", e);
        }
    }
    
    /**
     * Creates a new user-created article.
     *
     * @param article the article to create
     * @return the created article with its ID set, or null if creation failed
     */
    public UserCreatedArticle createUserArticle(UserCreatedArticle article) {
        String query = "INSERT INTO user_created_articles (author_id, title, description, content, image_url, category, approved) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, article.getAuthorId());
            pstmt.setString(2, article.getTitle());
            pstmt.setString(3, article.getDescription());
            pstmt.setString(4, article.getContent());
            pstmt.setString(5, article.getImageUrl());
            pstmt.setString(6, article.getCategory().getApiName());
            pstmt.setBoolean(7, article.isApproved());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    article.setId(generatedKeys.getInt(1));
                    return article;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user article", e);
        }
        return null;
    }
    
    /**
     * Updates an existing user-created article.
     *
     * @param article the article to update
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUserArticle(UserCreatedArticle article) {
        String query = "UPDATE user_created_articles SET title = ?, description = ?, content = ?, " +
                "image_url = ?, category = ?, approved = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getDescription());
            pstmt.setString(3, article.getContent());
            pstmt.setString(4, article.getImageUrl());
            pstmt.setString(5, article.getCategory().getApiName());
            pstmt.setBoolean(6, article.isApproved());
            pstmt.setInt(7, article.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user article", e);
            return false;
        }
    }
    
    /**
     * Deletes a user-created article.
     *
     * @param articleId the ID of the article to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteUserArticle(int articleId) {
        String query = "DELETE FROM user_created_articles WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, articleId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user article", e);
            return false;
        }
    }
    
    /**
     * Retrieves a user-created article by its ID.
     *
     * @param articleId the ID of the article
     * @return the article, or null if not found
     */
    public UserCreatedArticle getUserArticleById(int articleId) {
        String query = "SELECT a.*, u.username FROM user_created_articles a " +
                "JOIN users u ON a.author_id = u.id WHERE a.id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, articleId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createUserArticleFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user article", e);
        }
        return null;
    }
    
    /**
     * Retrieves all user-created articles.
     *
     * @param approvedOnly whether to retrieve only approved articles
     * @return a list of user-created articles
     */
    public List<UserCreatedArticle> getAllUserArticles(boolean approvedOnly) {
        List<UserCreatedArticle> articles = new ArrayList<>();
        String query = "SELECT a.*, u.username FROM user_created_articles a " +
                "JOIN users u ON a.author_id = u.id" +
                (approvedOnly ? " WHERE a.approved = TRUE" : "") +
                " ORDER BY a.created_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                articles.add(createUserArticleFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user articles", e);
        }
        return articles;
    }
    
    /**
     * Retrieves all user-created articles by a specific user.
     *
     * @param userId the ID of the user
     * @return a list of user-created articles
     */
    public List<UserCreatedArticle> getUserArticlesByAuthor(int userId) {
        List<UserCreatedArticle> articles = new ArrayList<>();
        String query = "SELECT a.*, u.username FROM user_created_articles a " +
                "JOIN users u ON a.author_id = u.id WHERE a.author_id = ? " +
                "ORDER BY a.created_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                articles.add(createUserArticleFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user articles by author", e);
        }
        return articles;
    }
    
    /**
     * Approves or unapproves a user-created article.
     *
     * @param articleId the ID of the article
     * @param approved whether the article should be approved
     * @return true if the update was successful, false otherwise
     */
    public boolean setUserArticleApproval(int articleId, boolean approved) {
        String query = "UPDATE user_created_articles SET approved = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, approved);
            pstmt.setInt(2, articleId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating article approval status", e);
            return false;
        }
    }
    
    /**
     * Creates a UserCreatedArticle object from a ResultSet.
     *
     * @param rs the ResultSet containing article data
     * @return the created UserCreatedArticle
     * @throws SQLException if a database error occurs
     */
    private UserCreatedArticle createUserArticleFromResultSet(ResultSet rs) throws SQLException {
        return new UserCreatedArticle(
                rs.getInt("id"),
                rs.getInt("author_id"),
                rs.getString("username"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("content"),
                rs.getString("image_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                Category.fromApiName(rs.getString("category")),
                rs.getBoolean("approved")
        );
    }

    /**
     * Saves an article for a user.
     *
     * @param user    the user
     * @param article the article to save
     * @return true if the save was successful, false otherwise
     */
    public boolean saveArticle(User user, Article article) {
        String query = "INSERT INTO saved_articles (user_id, article_id, title, description, content, author, url, image_url, published_at, source, category) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, user.getId());
            pstmt.setString(2, article.getId());
            pstmt.setString(3, article.getTitle());
            pstmt.setString(4, article.getDescription());
            pstmt.setString(5, article.getContent());
            pstmt.setString(6, article.getAuthor());
            pstmt.setString(7, article.getUrl());
            pstmt.setString(8, article.getImageUrl());
            pstmt.setTimestamp(9, article.getPublishedAt() != null ? 
                    Timestamp.valueOf(article.getPublishedAt()) : null);
            pstmt.setString(10, article.getSource());
            pstmt.setString(11, article.getCategory() != null ? 
                    article.getCategory().getApiName() : null);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                article.setSaved(true);
                article.setUserId(user.getId());
                user.addSavedArticle(article);
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving article", e);
            return false;
        }
    }

    /**
     * Removes a saved article for a user.
     *
     * @param user      the user
     * @param articleId the ID of the article to remove
     * @return true if the removal was successful, false otherwise
     */
    public boolean removeSavedArticle(User user, String articleId) {
        String query = "DELETE FROM saved_articles WHERE user_id = ? AND article_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, user.getId());
            pstmt.setString(2, articleId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                user.getSavedArticles().removeIf(a -> a.getId().equals(articleId));
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing saved article", e);
            return false;
        }
    }

    /**
     * Loads all saved articles for a user.
     *
     * @param user the user whose saved articles to load
     */
    private void loadSavedArticles(User user) {
        String query = "SELECT * FROM saved_articles WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();
            
            List<Article> savedArticles = new ArrayList<>();
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getString("article_id"));
                article.setTitle(rs.getString("title"));
                article.setDescription(rs.getString("description"));
                article.setContent(rs.getString("content"));
                article.setAuthor(rs.getString("author"));
                article.setUrl(rs.getString("url"));
                article.setImageUrl(rs.getString("image_url"));
                
                Timestamp publishedAt = rs.getTimestamp("published_at");
                if (publishedAt != null) {
                    article.setPublishedAt(publishedAt.toLocalDateTime());
                }
                
                article.setSource(rs.getString("source"));
                article.setCategory(Category.fromApiName(rs.getString("category")));
                article.setSaved(true);
                article.setUserId(user.getId());
                
                savedArticles.add(article);
            }
            user.setSavedArticles(savedArticles);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading saved articles", e);
        }
    }

    /**
     * Retrieves all saved articles for a user.
     *
     * @param userId the ID of the user
     * @return a list of saved articles
     */
    public List<Article> getSavedArticles(int userId) {
        List<Article> savedArticles = new ArrayList<>();
        String query = "SELECT * FROM saved_articles WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getString("article_id"));
                article.setTitle(rs.getString("title"));
                article.setDescription(rs.getString("description"));
                article.setContent(rs.getString("content"));
                article.setAuthor(rs.getString("author"));
                article.setUrl(rs.getString("url"));
                article.setImageUrl(rs.getString("image_url"));
                
                Timestamp publishedAt = rs.getTimestamp("published_at");
                if (publishedAt != null) {
                    article.setPublishedAt(publishedAt.toLocalDateTime());
                }
                
                article.setSource(rs.getString("source"));
                article.setCategory(Category.fromApiName(rs.getString("category")));
                article.setSaved(true);
                article.setUserId(userId);
                
                savedArticles.add(article);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving saved articles", e);
        }
        return savedArticles;
    }

    /**
     * Checks if an article is saved by a user.
     *
     * @param userId    the ID of the user
     * @param articleId the ID of the article
     * @return true if the article is saved, false otherwise
     */
    public boolean isArticleSaved(int userId, String articleId) {
        String query = "SELECT COUNT(*) FROM saved_articles WHERE user_id = ? AND article_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, articleId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if article is saved", e);
        }
        return false;
    }
}
