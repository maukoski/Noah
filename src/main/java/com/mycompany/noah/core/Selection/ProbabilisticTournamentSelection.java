package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

public class ProbabilisticTournamentSelection implements SelectionStrategy {

    private final int tournamentSize;
    private final double p;      // probabilidade de escolher o melhor (entre 0 e 1)
    private final Random r = new Random();

    public ProbabilisticTournamentSelection(int tournamentSize, double p) {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("p must be between 0.0 and 1.0");
        }
        this.tournamentSize = tournamentSize;
        this.p = p;
    }

    @Override
    public int[] selectParents(Individual<?>[] population, int popSize,
                               int numParents, String optimizationMode) {
        int[] parents = new int[numParents];
        for (int i = 0; i < numParents; i++) {
            int bestIdx = r.nextInt(popSize);
            for (int j = 1; j < tournamentSize; j++) {
                int candidate = r.nextInt(popSize);
                boolean chooseBetter = r.nextDouble() < p;

                if (optimizationMode.equals("MINIMIZATION")) {
                    if (chooseBetter) {
                        // melhor = menor fitness
                        if (population[candidate].getFitness() < population[bestIdx].getFitness()) {
                            bestIdx = candidate;
                        }
                    } else {
                        // pior = maior fitness
                        if (population[candidate].getFitness() > population[bestIdx].getFitness()) {
                            bestIdx = candidate;
                        }
                    }
                } else { // MAXIMIZATION
                    if (chooseBetter) {
                        if (population[candidate].getFitness() > population[bestIdx].getFitness()) {
                            bestIdx = candidate;
                        }
                    } else {
                        if (population[candidate].getFitness() < population[bestIdx].getFitness()) {
                            bestIdx = candidate;
                        }
                    }
                }
            }
            parents[i] = bestIdx;
        }
        return parents; // somente índices, sem crossover
    }
}