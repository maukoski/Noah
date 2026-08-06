/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.core;

/**
 * Represents an abstract individual in a genetic algorithm framework. The
 * genotype can be of any type, as specified by the generic parameter {@code T}.
 * This class also handles fitness evaluation, mutation, and crossover
 * mechanisms.
 *
 * @param <T> The type of the genotype.
 *
 * @author MAUKOSKI, W. X.
 */
public abstract class Individual<T> implements Comparable<Individual<T>> {

    private T genotype; // The genotype of the individual, which can be any type
    private double fitness; // The fitness score of the individual
    private double mutationTax; // The mutation rate (typically between 0 and 1)

    /**
     * Constructs an Individual with the specified mutation tax.
     *
     * @param mutationTax the mutation rate for this individual
     */
    public Individual(double mutationTax) {
        this.mutationTax = mutationTax;
    }

    public Individual(T genotype, double mutationTax) {
        this.genotype = genotype;
        this.mutationTax = mutationTax;
    }

    /**
     * Evaluates the fitness of this individual. This method must be implemented
     * by any concrete subclass to define how the fitness is calculated.
     *
     * @return the calculated fitness score of this individual
     */
    protected abstract double fitnessEvaluate();

    /**
     * Applies mutation to this individual. This method must be implemented by
     * any concrete subclass to define the mutation logic.
     *
     * @param genotype
     * @return
     */
    protected abstract T mutation(T genotype);

    /**
     * Performs the crossover operation and returns an array of offspring
     * individuals. This method must be implemented by any concrete subclass to
     * define the crossover logic.
     *
     * @param parent
     * @return an array of offspring individuals resulting from the crossover
     */
    public abstract Individual[] crossover(Individual parent);

    /**
     * Sets the genotype of this individual.
     *
     * @param genotype the genotype to be assigned to this individual
     */
    public void setGenotype(T genotype) {
        this.genotype = genotype;
    }

    /**
     * Returns the genotype of this individual.
     *
     * @return the current genotype of this individual
     */
    public T getGenotype() {
        return genotype;
    }

    /**
     * Returns the fitness score of this individual.
     *
     * @return the current fitness score
     */
    public double getFitness() {
        return fitness;
    }

    /**
     * Sets the fitness score for this individual.
     *
     * @param fitness the fitness score to be assigned to this individual
     */
    protected void setFitness(double fitness) {
        this.fitness = fitness;
    }

     /**
     * Returns the mutation rate applied to this individual.
     *
     * @return the mutation rate (between 0 and 1)
     */
    public double getMutationTax() {
        return mutationTax;
    }

    /**
     * Sets the mutation rate for this individual.
     *
     * @param mutationTax the mutation rate (between 0 and 1)
     */
    public void setMutationTax(double mutationTax) {
        this.mutationTax = mutationTax;
    }

    /**
     * Implementa a comparação entre indivíduos com base no valor de fitness.
     * Indivíduos com fitness maior serão considerados "maiores" para fins de
     * ordenação.
     *
     * @param other o outro indivíduo para comparar
     * @return um valor negativo se este indivíduo for menor, zero se forem
     * iguais, e um valor positivo se este indivíduo for maior
     */
    @Override
    public int compareTo(Individual<T> other) {
        return Double.compare(this.fitness, other.fitness);
    }


}
