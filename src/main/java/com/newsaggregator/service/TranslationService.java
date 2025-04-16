package com.newsaggregator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.model.Article;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for sharing article content via different platforms.
 * This replaces the previous translation service functionality.
 */
public class TranslationService {
    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    
    // Map of language codes to language names for UI display (kept for compatibility)
    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>();
    static {
        SUPPORTED_LANGUAGES.put("en", "English");
        SUPPORTED_LANGUAGES.put("fr", "French");
        SUPPORTED_LANGUAGES.put("de", "German");
        SUPPORTED_LANGUAGES.put("es", "Spanish");
        SUPPORTED_LANGUAGES.put("it", "Italian");
        SUPPORTED_LANGUAGES.put("pt", "Portuguese");
        SUPPORTED_LANGUAGES.put("ru", "Russian");
        SUPPORTED_LANGUAGES.put("ja", "Japanese");
        SUPPORTED_LANGUAGES.put("zh", "Chinese");
    }

    /**
     * Constructor for TranslationService.
     */
    public TranslationService() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Shares an article via the specified method.
     * This replaces the previous translateArticle method.
     *
     * @param article the article to share
     * @param sharingMethod the method of sharing (e.g., "Email", "Twitter")
     * @return a new article with updated sharing status
     */
    public Article translateArticle(Article article, String sharingMethod) {
        if (article == null || sharingMethod == null || sharingMethod.isEmpty()) {
            LOGGER.warning("Cannot share: article or sharing method is null/empty");
            return article;
        }

        try {
            // Create a new article with the same data
            Article sharedArticle = new Article(
                    article.getId(),
                    article.getSource(),
                    article.getAuthor(),
                    article.getTitle(),
                    article.getDescription(),
                    article.getUrl(),
                    article.getImageUrl(),
                    article.getContent(),
                    article.getPublishedAt(),
                    article.getCategory()
            );
            
            // Mark that the article has been shared 
            sharedArticle.setShared(true);
            sharedArticle.setSharedVia(sharingMethod);
            
            // Log the sharing action
            LOGGER.info("Article shared via " + sharingMethod + ": " + article.getId());
            
            return sharedArticle;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sharing article", e);
            return article;  // Return original article if sharing fails
        }
    }

    /**
     * Translates a single text from the source language to the target language.
     *
     * @param text the text to translate
     * @param sourceLanguage the source language code
     * @param targetLanguage the target language code
     * @return the translated text
     * @throws IOException if an error occurs during translation
     */
    public String translateText(String text, String sourceLanguage, String targetLanguage) throws IOException {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Limit text length to avoid issues with API limits
        int maxLength = 5000;
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength);
        }

        // SIMPLIFIED TRANSLATION FOR DEMO PURPOSES
        // In a real implementation, you'd use an actual translation API
        // This is just a demonstration version to show the functionality
        
        // Get language name for prefix
        String languageName = SUPPORTED_LANGUAGES.getOrDefault(targetLanguage, targetLanguage);
        
        // Add a prefix indicating the language
        return "[" + languageName + "] " + text;
        
        /* UNCOMMENT THIS TO USE THE ACTUAL API (requires registration)
        // Create JSON request body
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "q", text,
                "source", sourceLanguage,
                "target", targetLanguage,
                "format", "text"
        ));
        
        RequestBody body = RequestBody.create(requestJson, JSON);
        Request request = new Request.Builder()
                .url(LIBRE_TRANSLATE_API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.warning("Translation API request failed: " + response.code());
                return text;  // Return original text if translation fails
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // Extract translated text from response
            if (jsonNode.has("translatedText")) {
                return jsonNode.get("translatedText").asText();
            } else {
                LOGGER.warning("Translation response did not contain translatedText field");
                return text;  // Return original text if response format is unexpected
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during translation request", e);
            return text;  // Return original text if request fails
        }
        */
    }

    /**
     * Gets the map of supported languages.
     *
     * @return map of language codes to language names
     */
    public static Map<String, String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }
}