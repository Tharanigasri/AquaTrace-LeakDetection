package com.aquatrace.ui;

import com.aquatrace.dao.DBConnection;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static Stage stage;
    public static String currentUser;

    @Override
    public void start(Stage primaryStage) {
        DBConnection.init();
        stage = primaryStage;
        stage.setTitle("AquaTrace");
        showLogin();
        stage.show();
    }

    public static Node dropIcon(double scale) {
        SVGPath p = new SVGPath();
        p.setContent("M12 2 C12 2 5 10 5 15 A7 7 0 0 0 19 15 C19 10 12 2 12 2 Z");
        p.setFill(Color.web("#1e88e5"));
        p.setScaleX(scale);
        p.setScaleY(scale);
        return new Group(p);
    }

    private static void setRoot(Parent root, double w, double h) {
        stage.setScene(new Scene(root, w, h));
        stage.centerOnScreen();
    }

    public static void showLogin() {
        currentUser = null;
        setRoot(new LoginView().build(), 480, 620);
    }

    public static void showMain() {
        Label title = new Label("AquaTrace");
        title.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:white;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label user = new Label(currentUser);
        user.setStyle("-fx-text-fill:white;");
        Button logout = new Button("Log out");
        logout.setOnAction(e -> showLogin());

        HBox header = new HBox(10, dropIcon(1.0), title, spacer, user, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setStyle("-fx-background-color:linear-gradient(to right,#1565c0,#2e7d32);");

        DashboardView dashView = new DashboardView();
        HistoryView historyView = new HistoryView();
        Tab dash = new Tab("Dashboard", dashView.build());
        Tab entry = new Tab("Bill Entry", new BillEntryView().build());
        Tab hist = new Tab("History", historyView.build());
        dash.setClosable(false);
        entry.setClosable(false);
        hist.setClosable(false);
        TabPane tabs = new TabPane(dash, entry, hist);
        tabs.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> {
            if (n == dash) dashView.refresh(null, null);
            else if (n == hist) historyView.refresh();
        });
        // Chunk D will add the Bill Entry and History tabs here

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(tabs);
        setRoot(root, 1000, 720);
    }

    public static void main(String[] args) {
        launch(args);
    }
}