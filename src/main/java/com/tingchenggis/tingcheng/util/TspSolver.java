package com.tingchenggis.tingcheng.util;

public final class TspSolver {

    private TspSolver() {}

    public static int[] improveCyclic(int[] tour, double[][] dist) {
        int n = tour.length;
        boolean improved = true;
        int maxIter = 100;
        int iter = 0;
        while (improved && iter < maxIter) {
            improved = false;
            iter++;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double oldDist = dist[tour[i]][tour[(i + 1) % n]]
                        + dist[tour[j]][tour[(j + 1) % n]];
                    double newDist = dist[tour[i]][tour[j]]
                        + dist[tour[(i + 1) % n]][tour[(j + 1) % n]];
                    if (newDist < oldDist - 1e-10) {
                        reverse(tour, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }

    public static int[] improveOpen(int[] tour, double[][] dist) {
        int n = tour.length;
        if (n <= 3) return tour;
        boolean improved = true;
        int maxIter = 1000;
        int iter = 0;
        while (improved && iter < maxIter) {
            improved = false;
            iter++;
            for (int i = 1; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double oldDist = dist[tour[i - 1]][tour[i]]
                        + (j + 1 < n ? dist[tour[j]][tour[j + 1]] : 0);
                    double newDist = dist[tour[i - 1]][tour[j]]
                        + (j + 1 < n ? dist[tour[i]][tour[j + 1]] : 0);
                    if (newDist < oldDist - 1e-10) {
                        reverse(tour, i, j);
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }

    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int tmp = arr[start];
            arr[start] = arr[end];
            arr[end] = tmp;
            start++;
            end--;
        }
    }
}
