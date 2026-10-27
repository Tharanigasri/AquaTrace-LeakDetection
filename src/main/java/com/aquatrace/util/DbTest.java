package com.aquatrace.util;

import com.aquatrace.dao.*;
import com.aquatrace.model.Bill;

public class DbTest {
    public static void main(String[] args) {
        DBConnection.init();
        System.out.println("Login demo: " + (new UserDAO().login("demo", "aquatrace123") != null));
        for (Bill b : new BillDAO().findAll()) {
            System.out.println(b.getYear() + "-" + b.getMonth() + " : " + b.getConsumptionLiters());
        }
    }
}