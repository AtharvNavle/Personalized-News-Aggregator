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
        primaryStage = stage;
        primaryStage.setTitle("News Aggregator");
        
        // Initialize database connection
        DatabaseService.getInstance().initializeDatabase();
        
        // Set up login view as the first screen
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getRoot(), WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/com/newsaggregator/css/styles.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
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
