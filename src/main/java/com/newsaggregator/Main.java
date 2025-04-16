package com.newsaggregator;

import com.newsaggregator.service.DatabaseService;
import com.newsaggregator.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the News Aggregator application.
 */
public class Main extends Application {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static Stage primaryStage;

    /**
     * The main entry point for all JavaFX applications.
     * 
     * @param stage The primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        try {
            System.out.println("Starting News Aggregator application...");
            primaryStage = stage;
            primaryStage.setTitle("News Aggregator");
            
            // Initialize database connection
            System.out.println("Initializing database...");
            DatabaseService.getInstance().initializeDatabase();
            System.out.println("Database initialized successfully");
            
            // Set up login view as the first screen
            System.out.println("Setting up login view...");
            LoginView loginView = new LoginView();
            new com.newsaggregator.controller.LoginController(loginView);
            
            Scene scene = new Scene(loginView.getRoot(), WIDTH, HEIGHT);
            String cssPath = "/com/newsaggregator/css/styles.css";
            System.out.println("Loading CSS from: " + cssPath);
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            System.out.println("Application UI initialized and displayed");
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the primary stage of the application.
     * 
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * The main method that launches the JavaFX application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Called when the application should stop, closes database connections.
     */
    @Override
    public void stop() {
        // Close database connection when application exits
        DatabaseService.getInstance().closeConnection();
    }
}
