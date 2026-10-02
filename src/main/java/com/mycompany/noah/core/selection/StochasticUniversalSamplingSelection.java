package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

/**
 * Implements Stochastic Universal Sampling (SUS) selection strategy for
 * genetic algorithms.
 * <p>
 * SUS is an improved version of roulette wheel selection that reduces
 * the variance in the number of times each individual is selected.
 * Instead of spinning the roulette wheel once per parent, SUS places
 * {@code N} equally spaced pointers around the wheel and selects all
 * parents in a single pass. This ensures that the actual number of
 * selections is closer to the expected value, reducing genetic drift.
 * </p>
 * <p>
 * The process works as follows:
 * <ol>
 *   <li>Fitness values are adjusted to be non-negative (same as roulette).</li>
 *   <li>The total adjusted fitness {@code F} is calculated.</li>
 *   <li>The pointer spacing is {@code step = F / numParents}.</li>
 *   <li>A random starting point is chosen within {@code [0, step)}.</li>
 *   <li>{@code numParents} pointers are placed at {@code start + k * step}
 *       and the corresponding individuals are selected.</li>
 *   <li>Crossover is applied to each consecutive pair of selected parents.</li>
 * </ol>
 *
 * @author MAUKOSKI, W. X.
 */
public class StochasticUniversalSamplingSelection implements SelectionStrategy {

    /** Random number generator. */
    private final Random r = new Random();

    /**
     * Constructs a {@code StochasticUniversalSamplingSelection} strategy.
     */
    public StochasticUniversalSamplingSelection() { }

    /**
     * Selects parents using Stochastic Universal Sampling.
     * <p>
     * Fitness values are first adjusted to ensure non-negative probabilities
     * (same adjustment as roulette selection). Then equally spaced pointers
     * are used to select all parents in a single pass around the wheel.
     * </p>
     *
     * @param population the current population of individuals
     * @param populationSize the size of the population
     * @param numParents the number of parents to select
     * @param optimizationMode {@code "MAXIMIZATION"} or {@code "MINIMIZATION"}
     * @return an array of indices of the selected parents
     */
    @Override
    public int[] selectParents(Individual[] population, int populationSize, 
                                              int numParents, String optimizationMode) {
        // 1. Fitness adjustment (identical to roulette, without modifying the originals)
        double[] adjustedFitness = new double[populationSize];
        if (optimizationMode.equals("MAXIMIZATION")) {
            double minFit = Double.MAX_VALUE;
            for (int i = 0; i < populationSize; i++) {
                double f = population[i].getFitness();
                if (f < minFit) minFit = f;
            }
            double offset = (minFit < 0) ? -minFit : 0;
            for (int i = 0; i < populationSize; i++) {
                adjustedFitness[i] = population[i].getFitness() + offset;
            }
        } else { // MINIMIZATION
            double maxFit = -Double.MAX_VALUE;
            for (int i = 0; i < populationSize; i++) {
                double f = population[i].getFitness();
                if (f > maxFit) maxFit = f;
            }
            for (int i = 0; i < populationSize; i++) {
                adjustedFitness[i] = maxFit - population[i].getFitness() + 1;
            }
        }

        // 2. Calculate total F = sum of adjusted fitness
        double total = 0.0;
        for (double w : adjustedFitness) {
            total += w;
        }

        // 3. Spacing step = F / numParents
        double step = total / numParents;

        // 4. Draw a random initial start value uniformly between 0 and step
        double start = r.nextDouble() * step;

        // 5. For each of the numParents pointers, find the corresponding individual
        int[] parents = new int[numParents];
        for (int k = 0; k < numParents; k++) {
            double pointer = start + k * step;
            double sum = 0.0;
            int idx = 0;
            // Traverse the population accumulating adjusted fitness until exceeding the pointer
            while (idx < populationSize - 1 && sum + adjustedFitness[idx] < pointer) {
                sum += adjustedFitness[idx];
                idx++;
            }
            parents[k] = idx;
        }

        // 6. Crossover and replacement (same pattern as other implementations)
        for (int i = 0; i < numParents - 1; i += 2) {
            Individual[] children = population[parents[i]].crossover(population[parents[i + 1]]);
            population[parents[i]] = children[0];
            population[parents[i + 1]] = children[1];
        }

        return parents;
    }
}