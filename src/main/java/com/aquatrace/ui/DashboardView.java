package com.aquatrace.ui;

import com.aquatrace.dao.BillDAO;
import com.aquatrace.model.Bill;
import com.aquatrace.model.RiskLevel;
import com.aquatrace.service.LeakDetectionService;
import com.aquatrace.service.MonthAnalysis;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;
import java.util.function.Consumer;

public class DashboardView {
    private static final String[] MONTHS =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final BillDAO billDAO = new BillDAO();
    private final LeakDetectionService service = new LeakDetectionService();
    private final VBox root = new VBox(16);
    private final VBox detailBox = new VBox(12);
    private final Map<Integer, MonthAnalysis> byMonth = new HashMap<>();
    private int curYear;
    private String rateText = "0.05";
    private String co2Text = "0.0003";

    public Parent build() {
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color:#f1f8f9;");
        refresh(null, null);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#f1f8f9;");
        return sp;
    }

    private static String fmt(double v) { return String.format("%,.0f", v); }

    private static void whenReady(ObjectProperty<Node> p, Consumer<Node> c) {
        if (p.get() != null) c.accept(p.get());
        p.addListener((o, a, n) -> { if (n != null) c.accept(n); });
    }

    private static VBox card(Node... children) {
        VBox v = new VBox(10, children);
        v.setPadding(new Insets(14));
        v.setStyle("-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,2);");
        return v;
    }

    public void refresh(Integer selectMonth, String banner) {
        List<Bill> bills = billDAO.findAll();
        root.getChildren().clear();
        detailBox.getChildren().clear();
        if (bills.isEmpty()) {
            root.getChildren().add(new Label("No bills yet. Add some in the Bill Entry tab."));
            return;
        }

        curYear = bills.stream().mapToInt(Bill::getYear).max().getAsInt();
        int prevYear = curYear - 1;
        List<MonthAnalysis> list = service.analyze(bills, curYear);
        byMonth.clear();
        for (MonthAnalysis a : list) byMonth.put(a.bill.getMonth(), a);

        Map<Integer, Double> prevMap = new HashMap<>();
        Map<Integer, Double> curMap = new HashMap<>();
        for (Bill b : bills) {
            if (b.getYear() == prevYear) prevMap.put(b.getMonth(), b.getConsumptionLiters());
            if (b.getYear() == curYear) curMap.put(b.getMonth(), b.getConsumptionLiters());
        }

        // ---- chart ----
        CategoryAxis x = new CategoryAxis(FXCollections.observableArrayList(MONTHS));
        NumberAxis y = new NumberAxis();
        y.setLabel("Liters");
        y.setForceZeroInRange(false);
        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setAnimated(false);
        chart.setPrefHeight(380);
        chart.setMinHeight(300);

        XYChart.Series<String, Number> prevSeries = new XYChart.Series<>();
        prevSeries.setName(String.valueOf(prevYear));
        XYChart.Series<String, Number> curSeries = new XYChart.Series<>();
        curSeries.setName(String.valueOf(curYear));

        for (int m = 1; m <= 12; m++) {
            final int mm = m;
            StringBuilder tip = new StringBuilder(MONTHS[m - 1]);
            if (curMap.containsKey(m)) tip.append("\n").append(curYear).append(": ").append(fmt(curMap.get(m))).append(" L");
            if (prevMap.containsKey(m)) tip.append("\n").append(prevYear).append(": ").append(fmt(prevMap.get(m))).append(" L");
            String tipText = tip.toString();

            if (prevMap.containsKey(m)) {
                XYChart.Data<String, Number> d = new XYChart.Data<>(MONTHS[m - 1], prevMap.get(m));
                whenReady(d.nodeProperty(), n -> Tooltip.install(n, new Tooltip(tipText)));
                prevSeries.getData().add(d);
            }
            if (curMap.containsKey(m)) {
                XYChart.Data<String, Number> d = new XYChart.Data<>(MONTHS[m - 1], curMap.get(m));
                MonthAnalysis a = byMonth.get(m);
                boolean flagged = a != null && a.flagged();
                String tip2 = flagged ? tipText + "\nRisk: " + a.risk : tipText;
                whenReady(d.nodeProperty(), n -> {
                    Tooltip.install(n, new Tooltip(tip2));
                    if (flagged) {
                        n.setStyle("-fx-background-color:#e53935, white;-fx-padding:7px;");
                        n.setOnMouseClicked(e -> showDetail(byMonth.get(mm), null));
                    }
                });
                curSeries.getData().add(d);
            }
        }

        if (!prevSeries.getData().isEmpty()) {
            chart.setStyle("CHART_COLOR_1:#80cbc4; CHART_COLOR_2:#1565c0;");
            whenReady(prevSeries.nodeProperty(),
                    n -> n.setStyle("-fx-stroke-dash-array:8 6;-fx-stroke-width:2px;"));
            chart.getData().add(prevSeries);
        } else {
            chart.setStyle("CHART_COLOR_1:#1565c0;");
        }
        whenReady(curSeries.nodeProperty(), n -> n.setStyle("-fx-stroke-width:4px;"));
        chart.getData().add(curSeries);

        Label chartTitle = new Label("Monthly water use: " + prevYear + " vs " + curYear);
        chartTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0d47a1;");
        Label chartHint = new Label("Red dots are flagged months. Click one for details.");
        chartHint.setStyle("-fx-text-fill:#607d8b;");
        root.getChildren().add(card(chartTitle, chart, chartHint));

        // ---- benchmark notice ----
        boolean anyBenchmark = list.stream().anyMatch(a -> a.usedBenchmark);
        if (anyBenchmark) {
            Label bm = new Label("Estimated using regional average. Add last year's bills for more accurate comparison.");
            bm.setWrapText(true);
            bm.setStyle("-fx-background-color:#fff8e1;-fx-padding:10;-fx-background-radius:8;-fx-text-fill:#6d4c00;");
            root.getChildren().add(bm);
        }

        // ---- flagged months ----
        Label flagTitle = new Label("Flagged months");
        flagTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0d47a1;");
        FlowPane flow = new FlowPane(8, 8);
        for (MonthAnalysis a : list) {
            if (!a.flagged()) continue;
            Button b = new Button(MONTHS[a.bill.getMonth() - 1] + " " + curYear + "  |  " + a.risk
                    + "  (" + String.format("%+.0f", a.devLastYearPct) + "%)");
            String col = a.risk == RiskLevel.HIGH ? "#c62828" : "#ef6c00";
            b.setStyle("-fx-background-color:" + col + ";-fx-text-fill:white;-fx-font-weight:bold;");
            b.setOnAction(e -> showDetail(a, null));
            flow.getChildren().add(b);
        }
        if (flow.getChildren().isEmpty()) {
            Label ok = new Label("No months flagged. Usage looks normal.");
            ok.setStyle("-fx-text-fill:#2e7d32;");
            flow.getChildren().add(ok);
        }
        root.getChildren().add(card(flagTitle, flow));
        root.getChildren().add(detailBox);

        // ---- detail panel ----
        if (selectMonth != null && byMonth.containsKey(selectMonth)) {
            MonthAnalysis a = byMonth.get(selectMonth);
            if (a.flagged()) {
                showDetail(a, banner);
            } else if (banner != null) {
                Label b = new Label(banner);
                b.setWrapText(true);
                b.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;");
                detailBox.getChildren().add(card(b));
            }
        } else {
            Label hint = new Label("Select a flagged month to see its impact and answer a few questions.");
            hint.setStyle("-fx-text-fill:#607d8b;");
            detailBox.getChildren().add(hint);
        }
    }

    private static double parse(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }

    private void showDetail(MonthAnalysis a, String banner) {
        detailBox.getChildren().clear();
        if (banner != null) {
            Label b = new Label(banner);
            b.setWrapText(true);
            b.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;");
            detailBox.getChildren().add(b);
        }

        String col = a.risk == RiskLevel.HIGH ? "#c62828" : "#ef6c00";
        Label title = new Label(MONTHS[a.bill.getMonth() - 1] + " " + a.bill.getYear() + ": " + a.risk + " risk");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + col + ";");
        String ref = a.usedBenchmark ? "the regional average" : "the same month last year";
        Label why = new Label(String.format(
                "Used %s L, which is %+.1f%% vs %s (%s L) and %+.1f%% vs the trailing 3-month average.",
                fmt(a.bill.getConsumptionLiters()), a.devLastYearPct, ref, fmt(a.baseline), a.devTrailingPct));
        why.setWrapText(true);

        // ---- impact card ----
        TextField rate = new TextField(rateText);
        rate.setPrefWidth(100);
        TextField co2 = new TextField(co2Text);
        co2.setPrefWidth(100);
        Label extraL = new Label(String.format("%s L", fmt(Math.max(0, a.extraLiters()))));
        Label extraCost = new Label();
        Label extraCo2 = new Label();
        for (Label l : new Label[]{extraL, extraCost, extraCo2}) l.setStyle("-fx-font-weight:bold;");

        Runnable recalc = () -> {
            rateText = rate.getText();
            co2Text = co2.getText();
            double extra = Math.max(0, a.extraLiters());
            extraCost.setText(String.format("₹%,.2f", extra * parse(rate.getText())));
            extraCo2.setText(String.format("%,.2f kg (estimated)", extra * parse(co2.getText())));
        };
        rate.textProperty().addListener((o, p, n) -> recalc.run());
        co2.textProperty().addListener((o, p, n) -> recalc.run());
        recalc.run();

        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(8);
        g.addRow(0, new Label("Extra water vs expected"), extraL);
        g.addRow(1, new Label("Cost rate (₹ per liter)"), rate);
        g.addRow(2, new Label("Estimated extra cost"), extraCost);
        g.addRow(3, new Label("CO₂ factor (kg per liter)"), co2);
        g.addRow(4, new Label("Extra CO₂ (estimated)"), extraCo2);
        Label impactTitle = new Label("Impact");
        impactTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0d47a1;");
        VBox impact = card(impactTitle, g);

        // ---- clarifying questions ----
        Label qTitle = new Label("Help us understand this month");
        qTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0d47a1;");
        CheckBox q1 = new CheckBox("Yes: new occupants this month");
        CheckBox q2 = new CheckBox("Yes: new appliances (pool, irrigation)");
        CheckBox q3 = new CheckBox("Yes: I was away during this period");
        Label msg = new Label();
        msg.setWrapText(true);
        msg.setStyle("-fx-text-fill:#c62828;");
        Button save = new Button("Save answers and re-score");
        save.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-font-weight:bold;");
        save.setOnAction(e -> {
            List<String> parts = new ArrayList<>();
            if (q1.isSelected()) parts.add("New occupants");
            if (q2.isSelected()) parts.add("New appliance (pool/irrigation)");
            if (q3.isSelected()) parts.add("Away during this period");
            if (parts.isEmpty()) {
                msg.setText("Tick at least one answer, or leave the month flagged.");
                return;
            }
            String note = String.join(", ", parts);
            String old = a.bill.getNote();
            if (old != null && !old.trim().isEmpty()) note = old + "; " + note;
            billDAO.updateNote(a.bill.getYear(), a.bill.getMonth(), note);

            RiskLevel after = a.risk;
            for (MonthAnalysis m : service.analyze(billDAO.findAll(), curYear)) {
                if (m.bill.getMonth() == a.bill.getMonth()) after = m.risk;
            }
            refresh(a.bill.getMonth(), "Saved. Risk re-scored from " + a.risk + " to " + after + ".");
        });
        VBox questions = card(qTitle, q1, q2, q3, save, msg);

        detailBox.getChildren().addAll(title, why, impact, questions);
    }
}