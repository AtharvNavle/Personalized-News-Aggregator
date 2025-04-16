package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.User;
import com.newsaggregator.util.NewsCache;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for fetching and managing news articles from NewsAPI.
 */
public class NewsService {
    private static final Logger LOGGER = Logger.getLogger(NewsService.class.getName());
    private static final String API_KEY = "19f87027827f4224888b073e470d7bca";
    private static final String BASE_URL = "https://newsapi.org/v2/";
    private static final int ARTICLES_PER_PAGE = 20;
    
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final NewsCache newsCache;
    private final TranslationService translationService;

    /**
     * Constructor for NewsService.
     */
    public NewsService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
        this.newsCache = NewsCache.getInstance();
        this.translationService = new TranslationService();
    }

    /**
     * Gets top headlines based on the provided parameters.
     *
     * @param category the news category
     * @param query    the search query
     * @param page     the page number
     * @param user     the current user (for preferences)
     * @return a list of articles
     */
    public List<Article> getTopHeadlines(Category category, String query, int page, User user) {
        try {
            String cacheKey = generateCacheKey("top-headlines", category, query, page, user);
            List<Article> cachedArticles = newsCache.getArticles(cacheKey);
            
            if (cachedArticles != null) {
                LOGGER.info("Returning cached articles for: " + cacheKey);
                return cachedArticles;
            }
            
            URIBuilder uriBuilder = new URIBuilder(BASE_URL + "top-headlines");
            uriBuilder.addParameter("apiKey", API_KEY);
            uriBuilder.addParameter("page", String.valueOf(page));
            uriBuilder.addParameter("pageSize", String.valueOf(ARTICLES_PER_PAGE));
            
            // Add user preferences
            if (user != null) {
                uriBuilder.addParameter("language", user.getPreferredLanguage());
                uriBuilder.addParameter("country", user.getPreferredCountry());
            } else {
                uriBuilder.addParameter("language", "en");
                uriBuilder.addParameter("country", "us");
            }
            
            // Add category if specified
            if (category != null && category != Category.GENERAL) {
                uriBuilder.addParameter("category", category.getApiName());
            }
            
            // Add search query if specified
            if (query != null && !query.trim().isEmpty()) {
                uriBuilder.addParameter("q", query.trim());
            }
            
            List<Article> articles = fetchArticles(uriBuilder.build(), category, user);
            
            // Cache the results
            newsCache.cacheArticles(cacheKey, articles);
            
            return articles;
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Invalid URI for top headlines", e);
            return new ArrayList<>();
        }
    }

    /**
     * Searches for news articles based on the provided query.
     *
     * @param query    the search query
     * @param category the news category
     * @param page     the page number
     * @param user     the current user (for preferences)
     * @return a list of articles
     */
    public List<Article> searchNews(String query, Category category, int page, User user) {
        try {
            String cacheKey = generateCacheKey("everything", category, query, page, user);
            List<Article> cachedArticles = newsCache.getArticles(cacheKey);
            
            if (cachedArticles != null) {
                LOGGER.info("Returning cached articles for: " + cacheKey);
                return cachedArticles;
            }
            
            URIBuilder uriBuilder = new URIBuilder(BASE_URL + "everything");
            uriBuilder.addParameter("apiKey", API_KEY);
            uriBuilder.addParameter("q", query.trim());
            uriBuilder.addParameter("page", String.valueOf(page));
            uriBuilder.addParameter("pageSize", String.valueOf(ARTICLES_PER_PAGE));
            uriBuilder.addParameter("sortBy", "relevancy");
            
            // Add user preferences
            if (user != null) {
                uriBuilder.addParameter("language", user.getPreferredLanguage());
            } else {
                uriBuilder.addParameter("language", "en");
            }
            
            List<Article> articles = fetchArticles(uriBuilder.build(), category, user);
            
            // Cache the results
            newsCache.cacheArticles(cacheKey, articles);
            
            return articles;
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Invalid URI for search", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets news sources.
     *
     * @param category the news category
     * @param user     the current user (for preferences)
     * @return a list of sources
     */
    public List<String> getSources(Category category, User user) {
        try {
            URIBuilder uriBuilder = new URIBuilder(BASE_URL + "sources");
            uriBuilder.addParameter("apiKey", API_KEY);
            
            // Add category if specified
            if (category != null && category != Category.GENERAL) {
                uriBuilder.addParameter("category", category.getApiName());
            }
            
            // Add user preferences
            if (user != null) {
                uriBuilder.addParameter("language", user.getPreferredLanguage());
                uriBuilder.addParameter("country", user.getPreferredCountry());
            } else {
                uriBuilder.addParameter("language", "en");
            }
            
            HttpGet request = new HttpGet(uriBuilder.build());
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode root = objectMapper.readTree(jsonResponse);
                
                List<String> sources = new ArrayList<>();
                if (root.has("sources")) {
                    JsonNode sourcesNode = root.get("sources");
                    for (JsonNode sourceNode : sourcesNode) {
                        sources.add(sourceNode.get("id").asText());
                    }
                }
                
                return sources;
            }
        } catch (URISyntaxException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch sources", e);
            return new ArrayList<>();
        }
    }

    /**
     * Generates a cache key based on request parameters.
     *
     * @param endpoint the API endpoint
     * @param category the news category
     * @param query    the search query
     * @param page     the page number
     * @param user     the current user
     * @return a unique cache key
     */
    private String generateCacheKey(String endpoint, Category category, String query, int page, User user) {
        StringBuilder sb = new StringBuilder(endpoint);
        
        if (category != null) {
            sb.append("_cat-").append(category.getApiName());
        }
        
        if (query != null && !query.trim().isEmpty()) {
            sb.append("_q-").append(query.trim());
        }
        
        sb.append("_p-").append(page);
        
        if (user != null) {
            sb.append("_lang-").append(user.getPreferredLanguage());
            if (endpoint.equals("top-headlines")) {
                sb.append("_country-").append(user.getPreferredCountry());
            }
        }
        
        return sb.toString();
    }

    /**
     * Fetches articles from the NewsAPI.
     *
     * @param uri      the URI for the API request
     * @param category the default category for the articles
     * @param user     the current user (to check saved articles)
     * @return a list of articles
     */
    private List<Article> fetchArticles(URI uri, Category category, User user) {
        List<Article> articles = new ArrayList<>();
        
        try {
            HttpGet request = new HttpGet(uri);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode root = objectMapper.readTree(jsonResponse);
                
                if (root.has("articles")) {
                    JsonNode articlesNode = root.get("articles");
                    
                    for (JsonNode articleNode : articlesNode) {
                        Article article = parseArticle(articleNode, category);
                        
                        // Check if the article is saved by the user
                        if (user != null && article.getId() != null) {
                            boolean isSaved = DatabaseService.getInstance().isArticleSaved(user.getId(), article.getId());
                            article.setSaved(isSaved);
                            if (isSaved) {
                                article.setUserId(user.getId());
                            }
                        }
                        
                        articles.add(article);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch articles", e);
        }
        
        return articles;
    }

    /**
     * Parses a JSON node into an Article object.
     *
     * @param articleNode the JSON node for the article
     * @param category    the default category for the article
     * @return the parsed Article
     */
    private Article parseArticle(JsonNode articleNode, Category category) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        Article article = new Article();
        
        // Generate a unique ID for the article based on its URL and title
        String url = articleNode.has("url") ? articleNode.get("url").asText() : "";
        String title = articleNode.has("title") ? articleNode.get("title").asText() : "";
        String id = String.valueOf((url + title).hashCode());
        article.setId(id);
        
        article.setTitle(title);
        article.setDescription(articleNode.has("description") ? articleNode.get("description").asText() : "");
        article.setContent(articleNode.has("content") ? articleNode.get("content").asText() : "");
        article.setAuthor(articleNode.has("author") ? articleNode.get("author").asText() : "");
        article.setUrl(url);
        article.setImageUrl(articleNode.has("urlToImage") ? articleNode.get("urlToImage").asText() : "");
        
        if (articleNode.has("publishedAt")) {
            String publishedAtStr = articleNode.get("publishedAt").asText();
            try {
                LocalDateTime publishedAt = LocalDateTime.parse(publishedAtStr, formatter);
                article.setPublishedAt(publishedAt);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse date: " + publishedAtStr, e);
            }
        }
        
        if (articleNode.has("source") && articleNode.get("source").has("name")) {
            article.setSource(articleNode.get("source").get("name").asText());
        }
        
        article.setCategory(category);
        article.setSaved(false);
        
        return article;
    }

    /**
     * Shares an article via the specified method.
     *
     * @param article      the article to share
     * @param sharingInfo  the method of sharing and recipient info (e.g., "Email:user@example.com")
     * @return the shared article with updated sharing status
     */
    public Article shareArticle(Article article, String sharingInfo) {
        if (article == null || sharingInfo == null || sharingInfo.isEmpty()) {
            LOGGER.warning("Cannot share: article or sharing method is null/empty");
            return article;
        }
        
        String[] parts = sharingInfo.split(":", 2);
        String sharingMethod = parts[0];
        String recipient = parts.length > 1 ? parts[1] : "";
        
        // Log sharing request
        LOGGER.info("Sharing article: " + article.getId() + " via: " + sharingMethod + 
                   (recipient.isEmpty() ? "" : " to " + recipient));
        
        try {
            // In a real implementation, this would integrate with actual sharing mechanisms
            // For demonstration, we just update the article's sharing status
            
            article.setShared(true);
            
            if ("Email".equals(sharingMethod) && !recipient.isEmpty()) {
                article.setSharedVia(sharingMethod + " to " + recipient);
                
                // In a real app, we would use JavaMail or a similar API to send the email
                // For example:
                // sendEmail(recipient, "Check out this article", article.getTitle(), article.getUrl());
                
                LOGGER.info("Would send email to: " + recipient + " with article: " + article.getTitle());
            } else if ("Twitter".equals(sharingMethod) || "Facebook".equals(sharingMethod) || "LinkedIn".equals(sharingMethod)) {
                article.setSharedVia(sharingMethod);
                
                // In a real app, we would use the respective social media APIs
                // For example with Twitter:
                // twitterClient.updateStatus("Check out this article: " + article.getTitle() + " " + article.getUrl());
                
                LOGGER.info("Would post to " + sharingMethod + ": " + article.getTitle());
            } else if ("Copy Link".equals(sharingMethod)) {
                article.setSharedVia("Copied link");
                LOGGER.info("Article link copied to clipboard: " + article.getUrl());
            } else {
                article.setSharedVia(sharingMethod);
            }
            
            // Record the sharing action in the database
            // This would be implemented in a real application by storing sharing records
            
            LOGGER.info("Article shared successfully via: " + sharingMethod);
            return article;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to share article", e);
            return article;  // Return original article if sharing fails
        }
    }
    
    /**
     * Gets a map of supported languages for translation.
     *
     * @return map of language codes to language names
     */
    public Map<String, String> getSupportedLanguages() {
        return TranslationService.getSupportedLanguages();
    }

    /**
     * Closes the HttpClient.
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to close HttpClient", e);
        }
    }
}
