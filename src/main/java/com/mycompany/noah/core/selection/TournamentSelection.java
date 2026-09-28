package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;

import java.util.Random;

/**
 * Implements tournament selection strategy for genetic algorithms.
 * <p>
 * In tournament selection, a fixed number of individuals (the tournament size)
 * are randomly chosen from the population, and the best individual among them
 * (according to the optimization mode) is selected as a parent. This process
 * is repeated until the desired number of parents is obtained.
 * </p>
 * <p>
 * Tournament selection provides a good balance between selection pressure
 * and genetic diversity. The tournament size controls this balance:
 * larger tournaments increase selection pressure (favoring the best individuals),
 * while smaller tournaments maintain more diversity by giving weaker individuals
 * a better chance of being selected.
 * </p>
 * <p>
 * After selecting all parents, crossover is applied to each consecutive pair,
 * and the resulting children replace the parents in the population.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class TournamentSelection implements SelectionStrategy {

    /** Number of individuals competing in each tournament. */
    private final int tournamentSize;
    
    /** Random number generator. */
    private final Random r = new Random();

    /**
     * Constructs a {@code TournamentSelection} strategy with the specified
     * tournament size.
     *
     * @param tournamentSize the number of individuals participating in each tournament
     */
    public TournamentSelection(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    /**
     * Selects parents using tournament selection and applies crossover.
     * <p>
     * For each parent to be selected, a tournament of {@code tournamentSize}
     * randomly chosen individuals is held. The winner is the one with the
     * best fitness: lowest fitness for {@code MINIMIZATION}, highest for
     * {@code MAXIMIZATION}. After selection, crossover is applied to each
     * consecutive pair of parents.
     * </p>
     *
     * @param population the current population of individuals
     * @param popSize the size of the population
     * @param numParents the number of parents to select (must be even)
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @return an array of indices of the selected parents
     */
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
        
        // Apply crossover to each consecutive pair of selected parents
        for (int i = 0; i < numParents - 1; i += 2) {
            Individual[] children = population[parents[i]].crossover(population[parents[i + 1]]);
            population[parents[i]] = children[0];
            population[parents[i + 1]] = children[1];
        }
        return parents;
    }
}