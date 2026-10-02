/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.examples.unimodality.rosenbrock;

import com.mycompany.noah.core.Variables;

/**
 * Represents an individual solution for the shifted Rosenbrock function optimization problem.
 * This class extends {@link IndividualRosenbrock} and shifts the fitness value by subtracting
 * a constant, moving the global minimum to -10.
 *
 * @author willi
 */
public class IndividualShiftedRosenbrock extends IndividualRosenbrock {

    /**
     * Constructs an individual with a randomly initialized genotype.
     *
     * @param mutationTax the mutation rate
     * @param genotypeSize the number of bits per variable
     * @param numVariables the number of variables (dimension)
     */
    public IndividualShiftedRosenbrock(double mutationTax, int genotypeSize, int numVariables) {
        super(mutationTax, genotypeSize, numVariables);
    }

    /**
     * Constructs an individual from a given genotype.
     *
     * @param mutationTax the mutation rate
     * @param genotype the genotype to assign
     */
    public IndividualShiftedRosenbrock(double mutationTax, Variables genotype) {
        super(mutationTax, genotype);
    }

    /**
     * Evaluates the fitness of this individual using the shifted Rosenbrock function.
     * The value is computed as the original Rosenbrock fitness minus 10.0, so that
     * the minimum becomes -10.
     *
     * @return the shifted fitness value
     */
    @Override
    protected double fitnessEvaluate() {
        return super.fitnessEvaluate() - 10.0;
    }

    /**
     * Factory method to create a new individual of this type from a given genotype.
     *
     * @param genotype the genotype for the new individual
     * @return a new instance of IndividualShiftedRosenbrock
     */
    @Override
    protected IndividualRosenbrock createIndividual(Variables genotype) {
        return new IndividualShiftedRosenbrock(this.getMutationTax(), genotype);
    }
    
}