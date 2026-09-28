/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.examples.unimodality.rosenbrock;

import com.mycompany.noah.core.Variables;

/**
 *
 * @author willi
 */
public class IndividualShiftedRosenbrock extends IndividualRosenbrock {

    public IndividualShiftedRosenbrock(double mutationTax, int genotypeSize, int numVariables) {
        super(mutationTax, genotypeSize, numVariables);
    }

    public IndividualShiftedRosenbrock(double mutationTax, Variables genotype) {
        super(mutationTax, genotype);
    }

    @Override
    protected double fitnessEvaluate() {
        return super.fitnessEvaluate() - 10.0;   // desloca o mínimo para -10
    }

    @Override
    protected IndividualRosenbrock createIndividual(Variables genotype) {
        return new IndividualShiftedRosenbrock(this.getMutationTax(), genotype);
    }
    
}
