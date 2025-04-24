# Java News Aggregator

A personalized news aggregator application built with Java, JavaFX, and H2 database.

## Project Overview

This application allows users to:
- Browse top headlines from multiple news sources
- Search for news articles by keywords
- Filter news by categories
- Save articles for offline reading
- Manage user preferences for customized news feeds

The project features separate user and admin interfaces, with enhanced privileges for administrators.

## Technical Details

- **Language:** Pure Java implementation
- **UI Framework:** JavaFX 
- **Database:** H2 in-memory database
- **External API:** [NewsAPI](https://newsapi.org) for fetching news
- **Architecture:** MVC (Model-View-Controller) pattern

## Features

### User Features
- Login/Registration with validation
- Personalized news feed based on preferences
- Article search with filtering
- Offline reading capability
- User preference management (language, country, categories)

### Admin Features
- All user features
- User management (view all users, make users admins)
- System monitoring and control

## Running the Application

The application can be run in two modes:

### GUI Mode (requires JavaFX support)
```
mvn clean javafx:run
```

### Headless Mode (for environments without GUI support)
```
mvn exec:java -Dexec.mainClass="com.newsaggregator.HeadlessMain"
```

## Login Credentials

### Admin User
- Username: `admin`
- Password: `admin123`

## Project Structure

- `src/main/java/com/newsaggregator/model/` - Model classes
- `src/main/java/com/newsaggregator/view/` - JavaFX view components
- `src/main/java/com/newsaggregator/controller/` - Controller classes
- `src/main/java/com/newsaggregator/service/` - Service layer
- `src/main/java/com/newsaggregator/util/` - Utility classes
- `src/main/resources/` - FXML layouts and resources

## Dependencies

- Java 17 or higher
- Maven
- JavaFX (for GUI mode)
- H2 Database
- Required Maven dependencies (see pom.xml)