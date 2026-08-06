package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

/**
 * Implements truncation selection strategy for genetic algorithms.
 * <p>
 * Truncation selection keeps only a fixed percentage of the best individuals
 * (the elite) and discards the rest. The next generation is formed by:
 * <ol>
 *   <li>Copying the elite individuals directly to the new population
 *       (elitism, preserving the best solutions).</li>
 *   <li>Filling the remaining slots with offspring generated through
 *       crossover between randomly selected parents from the elite.</li>
 * </ol>
 * This creates strong selection pressure and converges quickly, but may
 * reduce genetic diversity and lead to premature convergence.
 * </p>
 * <p>
 * Note: This strategy overrides the default {@code evolvePopulation} method
 * instead of implementing the standard parent selection approach, as truncation
 * operates on the entire population replacement rather than individual parent
 * selection.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class TruncationSelection implements SelectionStrategy {

    /** Fraction of the population kept as elite (between 0 and 1). */
    private final double eliteFraction;

    /**
     * Constructs a {@code TruncationSelection} strategy.
     *
     * @param elitePercentage the percentage of the population to keep as elite
     *                        (between 0 and 100, e.g., 10 for 10%)
     * @throws IllegalArgumentException if {@code elitePercentage} is not between 0 and 100
     */
    public TruncationSelection(double elitePercentage) {
        // Converts percentage to fraction (e.g., 10 -> 0.10)
        if (elitePercentage < 0 || elitePercentage > 100) {
            throw new IllegalArgumentException("Elite percentage must be between 0 and 100");
        }
        this.eliteFraction = elitePercentage / 100.0;
    }

    /**
     * Evolves the population using truncation selection.
     * <p>
     * The top {@code eliteFraction} of the population (sorted by fitness)
     * is copied directly to the new generation. The remaining slots are
     * filled with offspring generated through crossover between randomly
     * selected parents from the elite group.
     * </p>
     *
     * @param population the current population, pre-sorted by fitness
     * @param numParents not used in this strategy (retained for interface compatibility)
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @return the new evolved population
     */
    @Override
    public Individual<?>[] evolvePopulation(Individual<?>[] population, int numParents,
                                            String optimizationMode) {
        int popSize = population.length;
        int eliteCount = Math.max(1, (int) (popSize * eliteFraction));
        Individual<?>[] newPopulation = new Individual<?>[popSize];
        Random r = new Random();

        // 1. Copy the elite to the beginning of the new population
        for (int i = 0; i < eliteCount; i++) {
            newPopulation[i] = population[i];
        }

        // 2. Fill the remaining slots with children from parents randomly drawn from the elite
        int writeIndex = eliteCount;
        while (writeIndex < popSize) {
            int fatherIdx = r.nextInt(eliteCount);
            int motherIdx = r.nextInt(eliteCount);
            // Avoid self-fertilization, if possible
            while (motherIdx == fatherIdx && eliteCount > 1) {
                motherIdx = r.nextInt(eliteCount);
            }
            Individual<?>[] children = population[fatherIdx].crossover(population[motherIdx]);
            newPopulation[writeIndex++] = children[0];
            if (writeIndex < popSize) {
                newPopulation[writeIndex++] = children[1];
            }
        }

        return newPopulation;
    }

    /**
     * Not used in truncation selection, as parent selection is performed
     * directly within {@link #evolvePopulation}.
     *
     * @param population the current population
     * @param populationSize the size of the population
     * @param numParents the number of parents to select
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @return an empty array (method not used in this strategy)
     */
    @Override
    public int[] selectParents(Individual<?>[] population, int populationSize, int numParents, String optimizationMode) {
       return new int[0];
    }
}