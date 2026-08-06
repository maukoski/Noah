/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Individual;

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