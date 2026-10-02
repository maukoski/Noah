/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.core;

/**
 * Stores information about the best individual found during the genetic
 * algorithm execution, including the individual itself and the generation
 * in which it was discovered.
 *
 * @param <T> the type of the individual's genotype
 * @author MAUKOSKI, W. X.
 */
public class Best<T> {

    /***
     * Creates a Best instance.
     */
    public Best() {
    }
    
    

    /** The best individual found during the genetic algorithm execution. */
    private Individual<T> individual;

    /** The generation in which the best individual was found. */
    private int generation;

    /**
     * Returns the best individual found during the genetic algorithm
     * execution.
     *
     * @return the best individual
     */
    public Individual<T> getIndividual() {
        return individual;
    }

    /**
     * Sets the individual to be stored as the best individual.
     *
     * @param individual the individual to set as best
     */
    public void setIndividual(Individual<T> individual) {
        this.individual = individual;
    }

    /**
     * Returns the generation in which the best individual was found.
     *
     * @return the generation number
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Sets the generation in which the best individual was found.
     *
     * @param generation the generation number
     */
    public void setGeneration(int generation) {
        this.generation = generation;
    }
}