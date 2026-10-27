package com.aquatrace.model;

public class Bill {
    private int id;
    private int year;
    private int month;
    private double consumptionLiters;
    private String note;

    public Bill(int id, int year, int month, double consumptionLiters, String note) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.consumptionLiters = consumptionLiters;
        this.note = note == null ? "" : note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public double getConsumptionLiters() { return consumptionLiters; }
    public void setConsumptionLiters(double c) { this.consumptionLiters = c; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note == null ? "" : note; }
}