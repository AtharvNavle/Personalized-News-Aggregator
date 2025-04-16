package com.newsaggregator.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a news article with all its details.
 */
public class Article {
    private String id;
    private String title;
    private String description;
    private String content;
    private String author;
    private String url;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private String source;
    private Category category;
    private boolean saved;
    private int userId;
    private boolean translated;
    private String translatedLanguage;

    /**
     * Default constructor.
     */
    public Article() {
    }
    
    /**
     * Constructor for creating a translated copy of an article.
     *
     * @param id                The unique identifier of the article
     * @param source            The source of the article
     * @param author            The author of the article
     * @param title             The title of the article
     * @param description       The description of the article
     * @param url               The URL to the full article
     * @param imageUrl          The URL to the article's image
     * @param content           The content of the article
     * @param publishedAt       The date and time when the article was published
     * @param category          The category of the article
     */
    public Article(String id, String source, String author, String title, String description,
                   String url, String imageUrl, String content, LocalDateTime publishedAt, 
                   Category category) {
        this.id = id;
        this.source = source;
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.content = content;
        this.publishedAt = publishedAt;
        this.category = category;
        this.saved = false;
        this.translated = false;
        this.translatedLanguage = null;
    }

    /**
     * Creates a new Article with the specified parameters.
     *
     * @param id          The unique identifier of the article
     * @param title       The title of the article
     * @param description The description of the article
     * @param content     The content of the article
     * @param author      The author of the article
     * @param url         The URL to the full article
     * @param imageUrl    The URL to the article's image
     * @param publishedAt The date and time when the article was published
     * @param source      The source of the article
     * @param category    The category of the article
     */
    public Article(String id, String title, String description, String content, String author,
                  String url, String imageUrl, LocalDateTime publishedAt, String source, Category category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.author = author;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
        this.source = source;
        this.category = category;
        this.saved = false;
        this.translated = false;
        this.translatedLanguage = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public void setSaved(boolean saved) {
        this.saved = saved;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public boolean isTranslated() {
        return translated;
    }
    
    public void setTranslated(boolean translated) {
        this.translated = translated;
    }
    
    public String getTranslatedLanguage() {
        return translatedLanguage;
    }
    
    public void setTranslatedLanguage(String translatedLanguage) {
        this.translatedLanguage = translatedLanguage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", category=" + category +
                '}';
    }
}
