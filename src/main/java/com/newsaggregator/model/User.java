package com.newsaggregator.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a user in the news aggregator system.
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String password; // Stores hashed password
    private boolean isAdmin;
    private Set<Category> preferredCategories;
    private List<Article> savedArticles;
    private String preferredLanguage;
    private String preferredCountry;

    /**
     * Default constructor.
     */
    public User() {
        this.preferredCategories = new HashSet<>();
        this.savedArticles = new ArrayList<>();
        this.preferredLanguage = "en";
        this.preferredCountry = "us";
    }

    /**
     * Creates a new User with the specified parameters.
     *
     * @param id       The unique identifier of the user
     * @param username The username of the user
     * @param email    The email address of the user
     * @param password The hashed password of the user
     * @param isAdmin  Whether the user is an administrator
     */
    public User(int id, String username, String email, String password, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.preferredCategories = new HashSet<>();
        this.savedArticles = new ArrayList<>();
        this.preferredLanguage = "en";
        this.preferredCountry = "us";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Set<Category> getPreferredCategories() {
        return preferredCategories;
    }

    public void setPreferredCategories(Set<Category> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }

    public void addPreferredCategory(Category category) {
        this.preferredCategories.add(category);
    }

    public void removePreferredCategory(Category category) {
        this.preferredCategories.remove(category);
    }

    public List<Article> getSavedArticles() {
        return savedArticles;
    }

    public void setSavedArticles(List<Article> savedArticles) {
        this.savedArticles = savedArticles;
    }

    public void addSavedArticle(Article article) {
        this.savedArticles.add(article);
    }

    public void removeSavedArticle(Article article) {
        this.savedArticles.remove(article);
    }
    
    public String getPreferredLanguage() {
        return preferredLanguage;
    }
    
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
    
    public String getPreferredCountry() {
        return preferredCountry;
    }
    
    public void setPreferredCountry(String preferredCountry) {
        this.preferredCountry = preferredCountry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
