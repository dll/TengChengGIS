package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.Pavilion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ObjectiveTest {

    @Test
    void distance_weight() {
        Pavilion p = new Pavilion();
        double w = Objective.DISTANCE.weight(100, 50, 10, 2, p);
        assertEquals(100, w, 1e-10);
    }

    @Test
    void time_weight() {
        double w = Objective.TIME.weight(100, 50, 10, 2, null);
        assertEquals(2.0, w, 1e-10);
    }

    @Test
    void cost_weight_withTicket() {
        Pavilion p = new Pavilion();
        p.setTicketPrice(20.0);
        double w = Objective.COST.weight(100, 50, 10, 2, p);
        assertEquals(10 + 200 + 20, w, 1e-10);
    }

    @Test
    void cost_weight_noTicket() {
        double w = Objective.COST.weight(100, 50, 10, 2, null);
        assertEquals(10 + 200, w, 1e-10);
    }

    @Test
    void parse_nullReturnsDistance() {
        assertEquals(Objective.DISTANCE, Objective.parse(null));
    }

    @Test
    void parse_invalidReturnsDistance() {
        assertEquals(Objective.DISTANCE, Objective.parse("invalid"));
    }

    @Test
    void parse_valid() {
        assertEquals(Objective.DISTANCE, Objective.parse("DISTANCE"));
        assertEquals(Objective.TIME, Objective.parse("time"));
        assertEquals(Objective.COST, Objective.parse("Cost"));
    }
}
