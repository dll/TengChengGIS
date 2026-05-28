package com.tingchenggis.tingcheng.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TspSolverTest {

    private double[][] symmetricDist(int n) {
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                d[i][j] = d[j][i] = 10.0;
        return d;
    }

    @Test
    void improveCyclic_shortTour() {
        int[] tour = {0, 1, 2};
        double[][] dist = symmetricDist(3);
        assertArrayEquals(new int[]{0, 1, 2}, TspSolver.improveCyclic(tour, dist));
    }

    @Test
    void improveCyclic_singleSwap() {
        double[][] dist = {
            {0, 1, 10, 10},
            {1, 0, 10, 10},
            {10, 10, 0, 1},
            {10, 10, 1, 0}
        };
        int[] tour = {0, 2, 3, 1};
        int[] result = TspSolver.improveCyclic(tour, dist);
        double origDist = tourDistanceCyclic(tour, dist);
        double newDist = tourDistanceCyclic(result, dist);
        assertTrue(newDist <= origDist, "2-opt should not increase distance");
    }

    @Test
    void improveCyclic_noDegradation() {
        double[][] dist = new double[5][5];
        for (int i = 0; i < 5; i++)
            for (int j = i + 1; j < 5; j++)
                dist[i][j] = dist[j][i] = Math.abs(i - j) * 3.0 + 1.0;
        int[] tour = {0, 2, 4, 1, 3};
        int[] result = TspSolver.improveCyclic(tour.clone(), dist);
        double origDist = tourDistanceCyclic(tour, dist);
        double newDist = tourDistanceCyclic(result, dist);
        assertTrue(newDist <= origDist + 1e-9, "2-opt should not increase distance");
    }

    @Test
    void improveOpen_shortTour() {
        int[] tour = {0, 1, 2};
        double[][] dist = symmetricDist(3);
        assertArrayEquals(new int[]{0, 1, 2}, TspSolver.improveOpen(tour, dist));
    }

    @Test
    void improveOpen_improvesCrossing() {
        double[][] dist = {
            {0, 1, 100, 100},
            {1, 0, 100, 100},
            {100, 100, 0, 1},
            {100, 100, 1, 0}
        };
        int[] original = {0, 2, 1, 3};
        double origDist = tourDistanceOpen(original, dist);
        int[] result = TspSolver.improveOpen(original, dist);
        double newDist = tourDistanceOpen(result, dist);
        assertTrue(newDist < origDist, "2-opt should improve crossing");
    }

    @Test
    void improveOpen_twoElement() {
        int[] tour = {5, 9};
        double[][] dist = {{0, 0}, {0, 0}};
        assertArrayEquals(new int[]{5, 9}, TspSolver.improveOpen(tour, dist));
    }

    private double tourDistanceCyclic(int[] tour, double[][] dist) {
        double total = 0;
        for (int i = 0; i < tour.length; i++)
            total += dist[tour[i]][tour[(i + 1) % tour.length]];
        return total;
    }

    private double tourDistanceOpen(int[] tour, double[][] dist) {
        double total = 0;
        for (int i = 0; i < tour.length - 1; i++)
            total += dist[tour[i]][tour[i + 1]];
        return total;
    }
}
