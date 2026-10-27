package com.aquatrace.model;

public enum RiskLevel {
    LOW, MEDIUM, HIGH;

    public RiskLevel downgrade() {
        if (this == HIGH) return MEDIUM;
        if (this == MEDIUM) return LOW;
        return LOW;
    }
}