package com.aquatrace.service;

import com.aquatrace.model.Bill;
import com.aquatrace.model.RiskLevel;
import java.util.*;

public class LeakDetectionService {
    public static final double REGIONAL_BENCHMARK = 5000.0;

    public List<MonthAnalysis> analyze(List<Bill> allBills, int year) {
        Map<String, Bill> map = new HashMap<>();
        for (Bill b : allBills) map.put(b.getYear() + "-" + b.getMonth(), b);

        List<MonthAnalysis> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Bill cur = map.get(year + "-" + m);
            if (cur == null) continue;

            Bill prev = map.get((year - 1) + "-" + m);
            boolean usedBenchmark = (prev == null);
            double baseline = usedBenchmark ? REGIONAL_BENCHMARK : prev.getConsumptionLiters();

            double devLast = (cur.getConsumptionLiters() - baseline) / baseline * 100.0;
            double devTrail = trailingDeviation(map, year, m, cur);

            RiskLevel risk;
            if (devLast < 15) risk = RiskLevel.LOW;
            else if (devLast <= 30) risk = RiskLevel.MEDIUM;
            else risk = RiskLevel.HIGH;

            if (cur.getNote() != null && !cur.getNote().trim().isEmpty()) {
                risk = risk.downgrade();
            }
            result.add(new MonthAnalysis(cur, baseline, devLast, devTrail, risk, usedBenchmark));
        }
        return result;
    }

    private double trailingDeviation(Map<String, Bill> map, int year, int month, Bill cur) {
        double sum = 0;
        int count = 0;
        int y = year, m = month;
        for (int i = 0; i < 3; i++) {
            m--;
            if (m == 0) { m = 12; y--; }
            Bill b = map.get(y + "-" + m);
            if (b != null) { sum += b.getConsumptionLiters(); count++; }
        }
        if (count == 0) return 0;
        double avg = sum / count;
        return (cur.getConsumptionLiters() - avg) / avg * 100.0;
    }
}