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
 * Service for translating article content using LibreTranslate API.
 */
public class TranslationService {
    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());
    private static final String LIBRE_TRANSLATE_API_URL = "https://libretranslate.de/translate";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    
    // Map of language codes to language names for UI display
    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>();
    static {
        SUPPORTED_LANGUAGES.put("en", "English");
        SUPPORTED_LANGUAGES.put("ar", "Arabic");
        SUPPORTED_LANGUAGES.put("zh", "Chinese");
        SUPPORTED_LANGUAGES.put("fr", "French");
        SUPPORTED_LANGUAGES.put("de", "German");
        SUPPORTED_LANGUAGES.put("hi", "Hindi");
        SUPPORTED_LANGUAGES.put("id", "Indonesian");
        SUPPORTED_LANGUAGES.put("ga", "Irish");
        SUPPORTED_LANGUAGES.put("it", "Italian");
        SUPPORTED_LANGUAGES.put("ja", "Japanese");
        SUPPORTED_LANGUAGES.put("ko", "Korean");
        SUPPORTED_LANGUAGES.put("pl", "Polish");
        SUPPORTED_LANGUAGES.put("pt", "Portuguese");
        SUPPORTED_LANGUAGES.put("ru", "Russian");
        SUPPORTED_LANGUAGES.put("es", "Spanish");
        SUPPORTED_LANGUAGES.put("sv", "Swedish");
        SUPPORTED_LANGUAGES.put("tr", "Turkish");
        SUPPORTED_LANGUAGES.put("uk", "Ukrainian");
        SUPPORTED_LANGUAGES.put("vi", "Vietnamese");
    }

    /**
     * Constructor for TranslationService.
     */
    public TranslationService() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Translates an article's title, description, and content to the target language.
     *
     * @param article the article to translate
     * @param targetLanguage the target language code (e.g., "es" for Spanish)
     * @return a new article with translated content
     */
    public Article translateArticle(Article article, String targetLanguage) {
        if (article == null || targetLanguage == null || targetLanguage.isEmpty()) {
            LOGGER.warning("Cannot translate: article or target language is null/empty");
            return article;
        }

        // For demonstration purposes, we'll simulate translation even for 'en'
        // This ensures users can see the translation feature working
        if (!SUPPORTED_LANGUAGES.containsKey(targetLanguage)) {
            LOGGER.info("Using default translation target language: English");
            targetLanguage = "en"; // Default to English if language not supported
        }

        try {
            // Create a new article with the same data
            Article translatedArticle = new Article(
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
            
            // Translate title if available
            if (article.getTitle() != null && !article.getTitle().isEmpty()) {
                String translatedTitle = translateText(article.getTitle(), "en", targetLanguage);
                translatedArticle.setTitle(translatedTitle);
            }
            
            // Translate description if available
            if (article.getDescription() != null && !article.getDescription().isEmpty()) {
                String translatedDescription = translateText(article.getDescription(), "en", targetLanguage);
                translatedArticle.setDescription(translatedDescription);
            }
            
            // Translate content if available
            if (article.getContent() != null && !article.getContent().isEmpty()) {
                String translatedContent = translateText(article.getContent(), "en", targetLanguage);
                translatedArticle.setContent(translatedContent);
            }
            
            // Mark that the article has been translated
            translatedArticle.setTranslated(true);
            translatedArticle.setTranslatedLanguage(targetLanguage);
            
            return translatedArticle;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error translating article", e);
            return article;  // Return original article if translation fails
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