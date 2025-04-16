-- Drop database if exists (for clean reinstalls)
DROP DATABASE IF EXISTS news_aggregator;

-- Create database
CREATE DATABASE news_aggregator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use database
USE news_aggregator;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    preferred_language VARCHAR(5) DEFAULT 'en',
    preferred_country VARCHAR(5) DEFAULT 'us',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user preferences table
CREATE TABLE user_preferences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_category (user_id, category)
);

-- Create saved articles table
CREATE TABLE saved_articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    article_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content TEXT,
    author VARCHAR(100),
    url VARCHAR(500),
    image_url VARCHAR(500),
    published_at TIMESTAMP,
    source VARCHAR(100),
    category VARCHAR(50),
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_article (user_id, article_id)
);

-- Create default admin user with password "admin123"
-- The password is hashed using SHA-256
INSERT INTO users (username, email, password, is_admin)
VALUES ('admin', 'admin@newsaggregator.com', 'NwvCHAQPq1KtRnxD1Rw5cg==:lnrSOd6a0BQu9HJdLcHfPOIJL8MwpSJ+CuJU/LyMx0A=', TRUE);

-- Create a regular test user with password "password123"
INSERT INTO users (username, email, password, is_admin)
VALUES ('testuser', 'test@example.com', 'I6n+knPnj1Tv/aBNrG02KA==:uFQwCxrUldlcu0kdJZyGEXO7MEK7raGMc9O+JGlKhCc=', FALSE);

-- Add some preferences for the test user
INSERT INTO user_preferences (user_id, category) VALUES 
(2, 'technology'),
(2, 'business'),
(2, 'science');
