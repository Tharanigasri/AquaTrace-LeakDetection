package com.aquatrace.ui;

import com.aquatrace.dao.BillDAO;
import com.aquatrace.model.Bill;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class BillEntryView {
    private static final String[] MONTHS =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final BillDAO dao = new BillDAO();
    private final TableView<Bill> table = new TableView<>();
    private final TextField yearField = new TextField();
    private final ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(MONTHS));
    private final TextField litersField = new TextField();
    private final TextField noteField = new TextField();
    private final Label formMsg = new Label();
    private final Button saveBtn = new Button("Add bill");
    private Bill editing = null;

    private final TextField[] cells = new TextField[12];
    private final TextField addYearField = new TextField();
    private final Label yearMsg = new Label();

    public Parent build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color:#f1f8f9;");
        root.getChildren().addAll(formCard(), tableCard(), addYearCard());
        reload();
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#f1f8f9;");
        return sp;
    }

    private VBox box(javafx.scene.Node... children) {
        VBox v = new VBox(10, children);
        v.setPadding(new Insets(14));
        v.setStyle("-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,2);");
        return v;
    }

    private Label heading(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0d47a1;");
        return l;
    }

    // ---------- single bill form ----------
    private VBox formCard() {
        yearField.setPromptText("Year (e.g. 2026)");
        monthBox.setPromptText("Month");
        litersField.setPromptText("Liters");
        noteField.setPromptText("Note (optional)");
        noteField.setPrefWidth(220);
        formMsg.setWrapText(true);

        saveBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;");
        saveBtn.setOnAction(e -> saveForm());
        Button clear = new Button("Clear");
        clear.setOnAction(e -> clearForm());

        FlowPane row = new FlowPane(10, 10, yearField, monthBox, litersField, noteField, saveBtn, clear);
        return box(heading("Add or edit a bill"), row, formMsg);
    }

    private void clearForm() {
        editing = null;
        yearField.clear();
        monthBox.getSelectionModel().clearSelection();
        litersField.clear();
        noteField.clear();
        saveBtn.setText("Add bill");
        formMsg.setText("");
    }

    private void err(Label l, String s) {
        l.setStyle("-fx-text-fill:#c62828;");
        l.setText(s);
    }

    private void ok(Label l, String s) {
        l.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;");
        l.setText(s);
    }

    private Integer parseYear(String s) {
        try {
            int y = Integer.parseInt(s.trim());
            return (y >= 2000 && y <= 2100) ? y : null;
        } catch (Exception e) { return null; }
    }

    private Double parseLiters(String s) {
        try {
            double d = Double.parseDouble(s.trim().replace(",", ""));
            return d >= 0 ? d : null;
        } catch (Exception e) { return null; }
    }

    private void saveForm() {
        Integer y = parseYear(yearField.getText());
        if (y == null) { err(formMsg, "Enter a valid year between 2000 and 2100."); return; }
        int mi = monthBox.getSelectionModel().getSelectedIndex();
        if (mi < 0) { err(formMsg, "Choose a month."); return; }
        Double l = parseLiters(litersField.getText());
        if (l == null) { err(formMsg, "Enter consumption in liters (a number, 0 or more)."); return; }

        if (editing != null && (editing.getYear() != y || editing.getMonth() != mi + 1)) {
            dao.delete(editing.getId());
        }
        dao.save(new Bill(0, y, mi + 1, l, noteField.getText().trim()));
        reload();
        clearForm();
        ok(formMsg, "Saved " + MONTHS[mi] + " " + y + ".");
    }

    // ---------- table ----------
    @SuppressWarnings("unchecked")
    private VBox tableCard() {
        TableColumn<Bill, Integer> yc = new TableColumn<>("Year");
        yc.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getYear()));
        yc.setPrefWidth(80);

        TableColumn<Bill, Integer> mc = new TableColumn<>("Month");
        mc.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getMonth()));
        mc.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : MONTHS[v - 1]);
            }
        });
        mc.setPrefWidth(80);

        TableColumn<Bill, Double> lc = new TableColumn<>("Liters");
        lc.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getConsumptionLiters()));
        lc.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%,.0f", v));
            }
        });
        lc.setPrefWidth(100);

        TableColumn<Bill, String> nc = new TableColumn<>("Note");
        nc.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNote()));
        nc.setPrefWidth(240);

        TableColumn<Bill, Void> ac = new TableColumn<>("Actions");
        ac.setSortable(false);
        ac.setPrefWidth(150);
        ac.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            private final HBox actionBox = new HBox(6, edit, del);
            {
                edit.setOnAction(e -> editBill(getTableView().getItems().get(getIndex())));
                del.setStyle("-fx-text-fill:#c62828;");
                del.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        table.getColumns().addAll(yc, mc, lc, nc, ac);
        table.getSortOrder().add(yc);
        table.getSortOrder().add(mc);
        table.setPrefHeight(280);
        table.setPlaceholder(new Label("No bills yet."));
        Label hint = new Label("Click a column header to sort.");
        hint.setStyle("-fx-text-fill:#607d8b;");
        return box(heading("All bills"), table, hint);
    }

    private void reload() {
        table.setItems(FXCollections.observableArrayList(dao.findAll()));
        table.sort();
    }

    private void editBill(Bill b) {
        editing = b;
        yearField.setText(String.valueOf(b.getYear()));
        monthBox.getSelectionModel().select(b.getMonth() - 1);
        litersField.setText(String.valueOf((long) b.getConsumptionLiters()));
        noteField.setText(b.getNote());
        saveBtn.setText("Update bill");
        ok(formMsg, "Editing " + MONTHS[b.getMonth() - 1] + " " + b.getYear() + ". Change values and click Update.");
    }

    private void confirmDelete(Bill b) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete the bill for " + MONTHS[b.getMonth() - 1] + " " + b.getYear() + "?",
                ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            dao.delete(b.getId());
            reload();
            ok(formMsg, "Deleted.");
        }
    }

    // ---------- add whole year ----------
    private VBox addYearCard() {
        addYearField.setPromptText("Year");
        Button load = new Button("Load year");
        load.setOnAction(e -> loadYear());
        Button saveAll = new Button("Save all 12 months");
        saveAll.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-font-weight:bold;");
        saveAll.setOnAction(e -> saveYear());

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        for (int m = 0; m < 12; m++) {
            cells[m] = new TextField();
            cells[m].setPromptText("Liters");
            cells[m].setPrefWidth(110);
            int col = (m % 4) * 2;
            int row = m / 4;
            g.add(new Label(MONTHS[m]), col, row);
            g.add(cells[m], col + 1, row);
        }
        yearMsg.setWrapText(true);
        HBox top = new HBox(10, addYearField, load, saveAll);
        Label hint = new Label("Fill in any months you have. Empty months are skipped. "
                + "Existing notes are kept.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill:#607d8b;");
        return box(heading("Add a whole year"), hint, top, g, yearMsg);
    }

    private void loadYear() {
        Integer y = parseYear(addYearField.getText());
        if (y == null) { err(yearMsg, "Enter a valid year between 2000 and 2100."); return; }
        for (TextField t : cells) t.clear();
        List<Bill> list = dao.findByYear(y);
        for (Bill b : list) cells[b.getMonth() - 1].setText(String.valueOf((long) b.getConsumptionLiters()));
        ok(yearMsg, list.isEmpty() ? "No bills for " + y + " yet. Enter them below."
                : "Loaded " + list.size() + " bills for " + y + ".");
    }

    private void saveYear() {
        Integer y = parseYear(addYearField.getText());
        if (y == null) { err(yearMsg, "Enter a valid year between 2000 and 2100."); return; }
        Map<Integer, Bill> existing = new HashMap<>();
        for (Bill b : dao.findByYear(y)) existing.put(b.getMonth(), b);

        double[] vals = new double[12];
        boolean[] has = new boolean[12];
        for (int m = 0; m < 12; m++) {
            String s = cells[m].getText().trim();
            if (s.isEmpty()) continue;
            Double d = parseLiters(s);
            if (d == null) { err(yearMsg, "Invalid number for " + MONTHS[m] + "."); return; }
            vals[m] = d;
            has[m] = true;
        }
        int count = 0;
        for (int m = 0; m < 12; m++) {
            if (!has[m]) continue;
            Bill old = existing.get(m + 1);
            dao.save(new Bill(0, y, m + 1, vals[m], old == null ? "" : old.getNote()));
            count++;
        }
        if (count == 0) { err(yearMsg, "Enter at least one month."); return; }
        reload();
        ok(yearMsg, "Saved " + count + " months for " + y + ".");
    }
}