package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Selection.SelectionStrategy;
import java.util.Random;

public class RankingSelection implements SelectionStrategy {
    private final Random r = new Random();

    @Override
    public int[] selectParents(Individual[] population, int popSize, int numParents, String optimizationMode) {
        double[] weights = new double[popSize];
        double total = 0;
        for (int i = 0; i < popSize; i++) {
            weights[i] = popSize - i;  // melhor (índice 0) peso maior
            total += weights[i];
        }
        int[] parents = new int[numParents];
        for (int i = 0; i < numParents; i++) {
            double point = r.nextDouble() * total;
            double sum = 0;
            int idx = 0;
            while (idx < popSize - 1 && sum < point) sum += weights[idx++];
            parents[i] = idx;
        }
        
        for (int i = 0; i < numParents - 1; i += 2) {
            Individual[] children = population[parents[i]].crossover(population[parents[i + 1]]);
            population[parents[i]] = children[0];
            population[parents[i + 1]] = children[1];
        }
        return parents;
    }
}