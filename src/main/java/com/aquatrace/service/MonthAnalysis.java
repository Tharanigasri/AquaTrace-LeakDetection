package com.aquatrace.service;

import com.aquatrace.model.Bill;
import com.aquatrace.model.RiskLevel;

public class MonthAnalysis {
    public final Bill bill;
    public final double baseline;        // expected liters
    public final double devLastYearPct;  // % vs same month last year (or benchmark)
    public final double devTrailingPct;  // % vs trailing 3-month average
    public final RiskLevel risk;
    public final boolean usedBenchmark;

    public MonthAnalysis(Bill bill, double baseline, double devLastYearPct,
                         double devTrailingPct, RiskLevel risk, boolean usedBenchmark) {
        this.bill = bill;
        this.baseline = baseline;
        this.devLastYearPct = devLastYearPct;
        this.devTrailingPct = devTrailingPct;
        this.risk = risk;
        this.usedBenchmark = usedBenchmark;
    }

    public double extraLiters() { return bill.getConsumptionLiters() - baseline; }
    public boolean flagged() { return risk == RiskLevel.MEDIUM || risk == RiskLevel.HIGH; }
}