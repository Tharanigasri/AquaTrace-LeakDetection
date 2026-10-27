package com.aquatrace.util;

import com.aquatrace.dao.*;
import com.aquatrace.service.*;

public class ServiceTest {
    public static void main(String[] args) {
        DBConnection.init();
        var list = new LeakDetectionService().analyze(new BillDAO().findAll(), 2026);
        for (MonthAnalysis a : list) {
            System.out.printf("2026-%d  vsLastYear=%.1f%%  vsTrailing=%.1f%%  risk=%s%n",
                    a.bill.getMonth(), a.devLastYearPct, a.devTrailingPct, a.risk);
        }
    }
}