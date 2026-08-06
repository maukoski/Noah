package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Selection.SelectionStrategy;
import java.util.Random;

public class TournamentSelection implements SelectionStrategy {
    private final int tournamentSize;
    private final Random r = new Random();

    public TournamentSelection(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    @Override
    public int[] selectParents(Individual[] population, int popSize, int numParents, String optimizationMode) {
        int[] parents = new int[numParents];
        for (int i = 0; i < numParents; i++) {
            int bestIdx = r.nextInt(popSize);
            for (int j = 1; j < tournamentSize; j++) {
                int candidate = r.nextInt(popSize);
                if (optimizationMode.equals("MINIMIZATION")) {
                    if (population[candidate].getFitness() < population[bestIdx].getFitness())
                        bestIdx = candidate;
                } else {
                    if (population[candidate].getFitness() > population[bestIdx].getFitness())
                        bestIdx = candidate;
                }
            }
            parents[i] = bestIdx;
        }
        
        for (int i = 0; i < numParents - 1; i += 2) {
            Individual[] children = population[parents[i]].crossover(population[parents[i + 1]]);
            population[parents[i]] = children[0];
            population[parents[i + 1]] = children[1];
        }
        return parents;
    }
}