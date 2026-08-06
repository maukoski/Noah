package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Selection.SelectionStrategy;
import java.util.Random;

public class RouletteSelection implements SelectionStrategy {

    private final Random r = new Random();

    @Override
    public int[]selectParents(Individual[] population, int popSize, int numParents, String optimizationMode) {
        double[] adjustedFitness = new double[popSize];
        if (optimizationMode.equals("MAXIMIZATION")) {
            double minFit = -Double.MIN_VALUE;
            for (int i = 0; i < popSize; i++) {
                double f = population[i].getFitness();
                if (f < minFit) {
                    minFit = f;
                }
            }
            double offset = (minFit < 0) ? -minFit : 0;
            for (int i = 0; i < popSize; i++) {
                adjustedFitness[i] = population[i].getFitness() + offset;
            }
        } else {
            double maxFit = -Double.MAX_VALUE;;
            for (int i = 0; i < popSize; i++) {
                double f = population[i].getFitness();
                if (f > maxFit) {
                    maxFit = f;
                }
            }
            for (int i = 0; i < popSize; i++) {
                adjustedFitness[i] = maxFit - population[i].getFitness() + 1;
            }
        }
        double total = 0;
        for (double w : adjustedFitness) {
            total += w;
        }

        
        int[] parents = new int[numParents];
        for (int i = 0; i < numParents; i++) {
            double point = r.nextDouble() * total;
            double sum = 0;
            int idx = 0;
            while (idx < popSize - 1 && sum < point) {
                sum += adjustedFitness[idx++];
            }
            parents[i] = idx;
        }

        return parents;
    }

   
}
