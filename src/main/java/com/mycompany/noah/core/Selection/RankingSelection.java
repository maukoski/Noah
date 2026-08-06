package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Selection.SelectionStrategy;
import java.util.Random;

/**
 * Implements ranking selection strategy for genetic algorithms.
 * <p>
 * In ranking selection, individuals are first sorted by fitness. Each
 * individual is assigned a weight based on its rank (position in the sorted
 * population) rather than its raw fitness value. The best individual (rank 1)
 * receives the highest weight, and weights decrease linearly for lower ranks.
 * This prevents highly fit individuals from dominating the selection process
 * early on, maintaining genetic diversity.
 * </p>
 * <p>
 * After selecting parents using the rank-based probability distribution,
 * crossover is applied to each pair of selected parents, and the resulting
 * children replace the parents in the population.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class RankingSelection implements SelectionStrategy {

    /** Random number generator. */
    private final Random r = new Random();

    /**
     * Selects parents using rank-based selection and applies crossover.
     * <p>
     * The population must be pre-sorted according to the optimization mode
     * before calling this method (best individual at index 0). Weights are
     * assigned as {@code popSize - rank}, so the best individual gets the
     * highest weight. Parents are then selected using roulette wheel
     * selection based on these rank weights.
     * </p>
     *
     * @param population the current population, pre-sorted by fitness
     * @param popSize the size of the population
     * @param numParents the number of parents to select
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @return an array of indices of the selected parents
     */
    @Override
    public int[] selectParents(Individual[] population, int popSize, int numParents, String optimizationMode) {
        double[] weights = new double[popSize];
        double total = 0;
        for (int i = 0; i < popSize; i++) {
            weights[i] = popSize - i;  // best (index 0) gets higher weight
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
        
        // Apply crossover to each pair of selected parents
        for (int i = 0; i < numParents - 1; i += 2) {
            Individual[] children = population[parents[i]].crossover(population[parents[i + 1]]);
            population[parents[i]] = children[0];
            population[parents[i + 1]] = children[1];
        }
        return parents;
    }
}