package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;

import java.util.Random;

/**
 * Implements roulette wheel (fitness proportionate) selection strategy for
 * genetic algorithms.
 * <p>
 * In roulette selection, each individual's probability of being selected is
 * proportional to its fitness. For maximization problems, higher fitness values
 * receive larger slices of the roulette wheel. For minimization problems,
 * fitness values are inverted so that lower fitness individuals have higher
 * selection probability.
 * </p>
 * <p>
 * If negative fitness values are present in maximization mode, all fitness
 * values are shifted by a constant offset to make them non-negative.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class RouletteSelection implements SelectionStrategy {

    /**
     * Random number generator.
     */
    private final Random r = new Random();

    /**
     * Selects parents using roulette wheel selection.
     * <p>
     * Fitness values are first adjusted to ensure non-negative probabilities:
     * <ul>
     * <li>For {@code MAXIMIZATION}: shifts all fitness values so the minimum
     * becomes zero.</li>
     * <li>For {@code MINIMIZATION}: inverts fitness using
     * {@code maxFit - fitness + 1}.</li>
     * </ul>
     * Parents are then selected with probability proportional to their adjusted
     * fitness.
     * </p>
     *
     * @param population the current population of individuals
     * @param popSize the size of the population
     * @param numParents the number of parents to select
     * @param optimizationMode {@code "MAXIMIZATION"} or {@code "MINIMIZATION"}
     * @return an array of indices of the selected parents
     */
    @Override
    public int[] selectParents(Individual[] population, int popSize, int numParents, String optimizationMode) {
        double[] adjustedFitness = new double[popSize];
        if (optimizationMode.equals("MAXIMIZATION")) {
            double minFit = Double.MAX_VALUE;
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
            double maxFit = -Double.MAX_VALUE;
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
            int idx = 0;
            double sum = adjustedFitness[idx];
            while (idx < popSize - 1 && sum < point) {
                idx++;
                sum += adjustedFitness[idx];
            }
            parents[i] = idx;
        }

        return parents;
    }

}
