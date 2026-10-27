package com.aquatrace.ui;

import com.aquatrace.dao.BillDAO;
import com.aquatrace.model.Bill;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.*;

public class HistoryView {
    private static final String[] MONTHS =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final BillDAO dao = new BillDAO();
    private final VBox root = new VBox(16);

    public Parent build() {
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color:#f1f8f9;");
        refresh();
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#f1f8f9;");
        return sp;
    }

    private static VBox card(Node... n) {
        VBox v = new VBox(10, n);
        v.setPadding(new Insets(14));
        v.setStyle("-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,2);");
        return v;
    }

    private static Label heading(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0d47a1;");
        return l;
    }

    public void refresh() {
        root.getChildren().clear();
        List<Bill> bills = dao.findAll();
        if (bills.isEmpty()) {
            root.getChildren().add(new Label("No bills yet. Add some in the Bill Entry tab."));
            return;
        }

        TreeMap<Integer, Map<Integer, Double>> years = new TreeMap<>();
        for (Bill b : bills) {
            years.computeIfAbsent(b.getYear(), k -> new HashMap<>())
                    .put(b.getMonth(), b.getConsumptionLiters());
        }

        // ---- table: one row per year, with annual total ----
        TableView<Integer> table = new TableView<>(FXCollections.observableArrayList(years.keySet()));
        TableColumn<Integer, String> yc = new TableColumn<>("Year");
        yc.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue())));
        yc.setPrefWidth(60);
        table.getColumns().add(yc);
        for (int m = 1; m <= 12; m++) {
            final int mm = m;
            TableColumn<Integer, String> col = new TableColumn<>(MONTHS[m - 1]);
            col.setPrefWidth(62);
            col.setSortable(false);
            col.setCellValueFactory(c -> {
                Double v = years.get(c.getValue()).get(mm);
                return new SimpleStringProperty(v == null ? "-" : String.format("%,.0f", v));
            });
            table.getColumns().add(col);
        }
        TableColumn<Integer, String> cnt = new TableColumn<>("Months");
        cnt.setPrefWidth(65);
        cnt.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(years.get(c.getValue()).size())));
        TableColumn<Integer, String> tot = new TableColumn<>("Total (L)");
        tot.setPrefWidth(90);
        tot.setCellValueFactory(c -> {
            double sum = 0;
            for (double v : years.get(c.getValue()).values()) sum += v;
            return new SimpleStringProperty(String.format("%,.0f", sum));
        });
        tot.setStyle("-fx-font-weight:bold;");
        table.getColumns().addAll(cnt, tot);
        table.setFixedCellSize(28);
        table.setPrefHeight(years.size() * 28 + 34);
        table.setMinHeight(years.size() * 28 + 34);

        Label note = new Label(years.size() == 1
                ? "Only one year of data so far. Add another year to enable comparison."
                : years.size() + " years of data.");
        note.setStyle("-fx-text-fill:#607d8b;");
        root.getChildren().add(card(heading("Yearly history"), table, note));

        // ---- chart: one line per year ----
        CategoryAxis x = new CategoryAxis(FXCollections.observableArrayList(MONTHS));
        NumberAxis y = new NumberAxis();
        y.setLabel("Liters");
        y.setForceZeroInRange(false);
        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setAnimated(false);
        chart.setPrefHeight(360);
        for (Map.Entry<Integer, Map<Integer, Double>> e : years.entrySet()) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName(String.valueOf(e.getKey()));
            for (int m = 1; m <= 12; m++) {
                Double v = e.getValue().get(m);
                if (v != null) s.getData().add(new XYChart.Data<>(MONTHS[m - 1], v));
            }
            chart.getData().add(s);
        }
        root.getChildren().add(card(heading("All years compared"), chart));
    }
}