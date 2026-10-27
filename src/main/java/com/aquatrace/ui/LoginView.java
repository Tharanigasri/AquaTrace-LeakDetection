package com.aquatrace.ui;

import com.aquatrace.dao.UserDAO;
import com.aquatrace.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView {
    public Parent build() {
        UserDAO dao = new UserDAO();

        Label title = new Label("AquaTrace");
        title.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:#0d47a1;");
        Label tag = new Label("Find water leaks from your bills");
        tag.setStyle("-fx-text-fill:#2e7d32;");

        TextField user = new TextField();
        user.setPromptText("Username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");

        Label error = new Label();
        error.setStyle("-fx-text-fill:#c62828;");
        error.setWrapText(true);

        Button action = new Button("Log in");
        action.setMaxWidth(Double.MAX_VALUE);
        action.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;");
        Hyperlink toggle = new Hyperlink("New here? Sign up");
        Label hint = new Label("Demo login: demo / aquatrace123");
        hint.setStyle("-fx-text-fill:#607d8b;-fx-font-size:11px;");

        boolean[] signup = {false};
        toggle.setOnAction(e -> {
            signup[0] = !signup[0];
            action.setText(signup[0] ? "Sign up" : "Log in");
            toggle.setText(signup[0] ? "Have an account? Log in" : "New here? Sign up");
            error.setText("");
        });

        Runnable submit = () -> {
            String u = user.getText().trim();
            String p = pass.getText();
            if (u.isEmpty() || p.isEmpty()) {
                error.setText("Please enter both username and password.");
                return;
            }
            if (signup[0]) {
                if (p.length() < 6) {
                    error.setText("Password must be at least 6 characters.");
                    return;
                }
                if (!dao.register(u, p)) {
                    error.setText("That username is already taken.");
                    return;
                }
            }
            User found = dao.login(u, p);
            if (found == null) {
                error.setText("Wrong username or password.");
                return;
            }
            MainApp.currentUser = found.getUsername();
            MainApp.showMain();
        };
        action.setOnAction(e -> submit.run());
        pass.setOnAction(e -> submit.run());

        VBox card = new VBox(12, MainApp.dropIcon(2.2), title, tag, user, pass, error, action, toggle, hint);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.setMaxWidth(360);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),16,0,0,4);");

        StackPane pane = new StackPane(card);
        pane.setPadding(new Insets(20));
        pane.setStyle("-fx-background-color:linear-gradient(to bottom,#e3f2fd,#e8f5e9);");
        return pane;
    }
}