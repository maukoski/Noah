package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;

/**
 * Defines the contract for selection strategies used in the genetic algorithm.
 * <p>
 * Implementations of this interface determine how individuals are selected
 * from the population to serve as parents for the next generation. Different
 * strategies (such as roulette, tournament, ranking, or Boltzmann selection)
 * provide different balances between selection pressure and genetic diversity.
 * </p>
 * <p>
 * The interface also provides a default method {@link #evolvePopulation} that
 * performs the selection and immediately applies crossover to the selected
 * parent pairs, replacing them with their offspring in the population.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public interface SelectionStrategy {

    /**
     * Selects a set of parent indices for reproduction.
     *
     * @param population       current population (already sorted according to optimization mode)
     * @param populationSize   total number of individuals
     * @param numParents       number of parents to select (must be even)
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @return array of parent indices, where each consecutive pair (i, i+1) forms a couple
     */
    int[] selectParents(Individual<?>[] population, int populationSize, int numParents, String optimizationMode);

    /**
     * Evolves the population by selecting parents and applying crossover.
     * <p>
     * This default method first calls {@link #selectParents} to obtain the
     * parent indices, then performs crossover on each consecutive pair.
     * The resulting children replace their respective parents in the population.
     * </p>
     *
     * @param population       the current population to evolve
     * @param numParents       number of parents to select (must be even)
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @return the evolved population with children replacing the selected parents
     */
    default Individual<?>[] evolvePopulation(Individual<?>[] population, int numParents,
                                             String optimizationMode) {
        int[] parents = selectParents(population, population.length,
                                            numParents, optimizationMode);
        for (int i = 0; i < parents.length - 1; i += 2) {
            Individual<?>[] children = population[parents[i]].crossover(population[parents[i + 1]]);
            population[parents[i]] = children[0];
            population[parents[i + 1]] = children[1];
        }
        return population;
    }
}