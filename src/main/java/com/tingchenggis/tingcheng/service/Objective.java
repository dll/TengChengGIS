package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.Pavilion;

public enum Objective {
    DISTANCE {
        @Override
        public double weight(double distKm, double speedKmh, double fareBase, double farePerKm, Pavilion to) {
            return distKm;
        }
    },
    TIME {
        @Override
        public double weight(double distKm, double speedKmh, double fareBase, double farePerKm, Pavilion to) {
            return distKm / speedKmh;
        }
    },
    COST {
        @Override
        public double weight(double distKm, double speedKmh, double fareBase, double farePerKm, Pavilion to) {
            double fare = fareBase + distKm * farePerKm;
            double ticket = to != null && to.getTicketPrice() != null ? to.getTicketPrice() : 0.0;
            return fare + ticket;
        }
    };

    public abstract double weight(double distKm, double speedKmh, double fareBase, double farePerKm, Pavilion to);

    public static Objective parse(String raw) {
        if (raw == null) return DISTANCE;
        try { return Objective.valueOf(raw.toUpperCase()); }
        catch (IllegalArgumentException e) { return DISTANCE; }
    }
}
