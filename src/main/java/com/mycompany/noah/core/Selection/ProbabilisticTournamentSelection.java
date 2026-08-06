package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

/**
 * Implements probabilistic tournament selection strategy for genetic algorithms.
 * <p>
 * In each tournament, a fixed number of individuals are randomly chosen from
 * the population. The winner is selected probabilistically: with probability
 * {@code p} the best individual (according to the optimization mode) is chosen,
 * and with probability {@code 1 - p} the worst individual is chosen.
 * This provides a balance between selection pressure and genetic diversity.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class ProbabilisticTournamentSelection implements SelectionStrategy {

    /** Number of individuals competing in each tournament. */
    private final int tournamentSize;
    
    /** Probability of selecting the best individual in the tournament (between 0 and 1). */
    private final double p;
    
    /** Random number generator. */
    private final Random r = new Random();

    /**
     * Constructs a {@code ProbabilisticTournamentSelection} strategy.
     *
     * @param tournamentSize the number of individuals participating in each tournament
     * @param p the probability of selecting the best individual (must be between 0.0 and 1.0)
     * @throws IllegalArgumentException if {@code p} is not between 0.0 and 1.0
     */
    public ProbabilisticTournamentSelection(int tournamentSize, double p) {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("p must be between 0.0 and 1.0");
        }
        this.tournamentSize = tournamentSize;
        this.p = p;
    }

    /**
     * Selects parents using probabilistic tournament selection.
     * <p>
     * For each parent to be selected, a tournament of {@code tournamentSize}
     * individuals is randomly drawn. Each comparison has probability {@code p}
     * of selecting the better individual (lower fitness for minimization,
     * higher for maximization) and probability {@code 1 - p} of selecting
     * the worse one.
     * </p>
     *
     * @param population the current population of individuals
     * @param popSize the size of the population
     * @param numParents the number of parents to select
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @return an array of indices of the selected parents
     */
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
                        // better = lower fitness
                        if (population[candidate].getFitness() < population[bestIdx].getFitness()) {
                            bestIdx = candidate;
                        }
                    } else {
                        // worse = higher fitness
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
        return parents; // only indices, no crossover
    }
}