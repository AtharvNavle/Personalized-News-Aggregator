package com.newsaggregator.model;

import java.time.LocalDateTime;

/**
 * Represents a news article created by a user.
 */
public class UserCreatedArticle {
    private int id;
    private int authorId;
    private String authorUsername;
    private String title;
    private String description;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Category category;
    private boolean approved;

    /**
     * Constructor for UserCreatedArticle with all fields.
     *
     * @param id             the unique ID
     * @param authorId       the ID of the user who created the article
     * @param authorUsername the username of the author
     * @param title          the article title
     * @param description    the article description
     * @param content        the article content
     * @param imageUrl       the URL to the article's image
     * @param createdAt      the date and time the article was created
     * @param category       the category of the article
     * @param approved       whether the article is approved for display
     */
    public UserCreatedArticle(int id, int authorId, String authorUsername, String title, 
                             String description, String content, String imageUrl, 
                             LocalDateTime createdAt, Category category, boolean approved) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.description = description;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.category = category;
        this.approved = approved;
    }
    
    /**
     * Constructor for creating a new UserCreatedArticle.
     * This is used for new user submissions where ID and some fields are set later.
     *
     * @param authorId       the ID of the user who created the article
     * @param authorUsername the username of the author
     * @param title          the article title
     * @param description    the article description
     * @param content        the article content
     * @param imageUrl       the URL to the article's image
     * @param category       the category of the article
     */
    public UserCreatedArticle(int authorId, String authorUsername, String title, 
                             String description, String content, String imageUrl, 
                             Category category) {
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.description = description;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
        this.category = category;
        this.approved = false; // New articles are not approved by default
    }

    /**
     * Gets the ID of the article.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the article.
     *
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the ID of the author.
     *
     * @return the author ID
     */
    public int getAuthorId() {
        return authorId;
    }

    /**
     * Sets the ID of the author.
     *
     * @param authorId the author ID to set
     */
    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    /**
     * Gets the username of the author.
     *
     * @return the author's username
     */
    public String getAuthorUsername() {
        return authorUsername;
    }

    /**
     * Sets the username of the author.
     *
     * @param authorUsername the author's username to set
     */
    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    /**
     * Gets the title of the article.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the article.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description of the article.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the article.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the content of the article.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the article.
     *
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the URL to the article's image.
     *
     * @return the image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL to the article's image.
     *
     * @param imageUrl the image URL to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the date and time the article was created.
     *
     * @return the created date and time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the date and time the article was created.
     *
     * @param createdAt the created date and time to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the category of the article.
     *
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets the category of the article.
     *
     * @param category the category to set
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Checks if the article is approved for display.
     *
     * @return true if approved, false otherwise
     */
    public boolean isApproved() {
        return approved;
    }

    /**
     * Sets whether the article is approved for display.
     *
     * @param approved true to approve, false otherwise
     */
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return "UserCreatedArticle{" +
                "id=" + id +
                ", authorId=" + authorId +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", approved=" + approved +
                '}';
    }
}