package com.newsaggregator.model;

/**
 * Represents news categories used for categorizing articles.
 */
public enum Category {
    BUSINESS("business", "Business"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    GENERAL("general", "General"),
    HEALTH("health", "Health"),
    SCIENCE("science", "Science"),
    SPORTS("sports", "Sports"),
    TECHNOLOGY("technology", "Technology");

    private final String apiName;
    private final String displayName;

    /**
     * Constructor for Category enum.
     *
     * @param apiName     The name used in API requests
     * @param displayName The name displayed to users
     */
    Category(String apiName, String displayName) {
        this.apiName = apiName;
        this.displayName = displayName;
    }

    /**
     * Gets the API name of the category.
     *
     * @return The API name
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * Gets the display name of the category.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Finds a category by its API name.
     *
     * @param apiName The API name to search for
     * @return The matching Category or GENERAL if none is found
     */
    public static Category fromApiName(String apiName) {
        for (Category category : Category.values()) {
            if (category.apiName.equals(apiName)) {
                return category;
            }
        }
        return GENERAL; // Default category if not found
    }

    @Override
    public String toString() {
        return displayName;
    }
}
