/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

/**
 * Implements Boltzmann selection strategy for genetic algorithms.
 * <p>
 * This selection method uses a temperature-based probability distribution
 * derived from the Boltzmann distribution of statistical mechanics. The
 * temperature decreases over generations (cooling schedule), gradually
 * shifting the selection pressure from exploration to exploitation.
 * Higher fitness individuals have exponentially higher probability of
 * being selected, controlled by the current temperature.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class BoltzmanSelection implements SelectionStrategy {

    /** Generation counter used for the cooling schedule. */
    private int count;
    
    /** Initial temperature for the Boltzmann distribution. */
    private double initialTemperature;

    /**
     * Constructs a {@code BoltzmanSelection} strategy with the given
     * initial temperature.
     *
     * @param initialTemperature the starting temperature value
     */
    public BoltzmanSelection(double initialTemperature) {
        this.count = 0;
        this.initialTemperature = initialTemperature;
    }

    /**
     * Selects parents using the Boltzmann probability distribution.
     * <p>
     * The selection probability for each individual is proportional to
     * {@code exp(fitness / temperature)} for maximization or
     * {@code exp(-fitness / temperature)} for minimization. The temperature
     * decreases according to the cooling schedule {@code T = T0 / (1 + generation)}.
     * </p>
     *
     * @param population the current population of individuals
     * @param populationSize the size of the population
     * @param numParents the number of parents to select
     * @param optimizationMode {@code "MAXIMIZATION"} or {@code "MINIMIZATION"}
     * @return an array of indices of the selected parents
     */
    @Override
    public int[] selectParents(Individual<?>[] population, int populationSize,
            int numParents, String optimizationMode) {
        int[] parents = new int[numParents];
        double[] probabilityWeight = new double[populationSize];

        // Updates temperature with cooling: T = T0 / (1 + generation)
        double temperature = this.initialTemperature / (1.0 + this.count);

        // Calculates unnormalized weights
        double maxWeight = 0.0;
        for (int i = 0; i < populationSize; i++) {
            double fitness = population[i].getFitness();
            double exponent;
            if (optimizationMode.equals("MAXIMIZATION")) {
                exponent = fitness / temperature;
            } else { // MINIMIZATION
                exponent = -fitness / temperature;
            }
            // To avoid overflow, limit the exponent to a safe value (~700)
            if (exponent > 700.0) {
                exponent = 700.0;
            }
            if (exponent < -700.0) {
                exponent = -700.0;
            }
            probabilityWeight[i] = Math.exp(exponent);
            if (probabilityWeight[i] > maxWeight) {
                maxWeight = probabilityWeight[i];
            }
        }

        // (Optional) Subtract the maximum to avoid loss of precision with very large values,
        // but not strictly necessary if the exponent is already limited.
        // Calculates the total sum of weights
        double totalWeight = 0.0;
        for (double w : probabilityWeight) {
            totalWeight += w;
        }

        Random r = new Random();  // or use a class field

        // Selects numParents parents using roulette
        for (int p = 0; p < numParents; p++) {
            double point = r.nextDouble() * totalWeight;
            double sum = 0.0;
            int idx = 0;
            while (idx < populationSize - 1 && sum + probabilityWeight[idx] < point) {
                sum += probabilityWeight[idx];
                idx++;
            }
            parents[p] = idx;
        }

        this.count++;  // increments the generation counter for the next call
        return parents;
    }

}