/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.examples.multimodality;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Variables;
import java.util.Random;

/**
 * Represents an individual solution for the Rastrigin function optimization problem.
 * The genotype is composed of binary strings, one per variable. Each binary string is
 * decoded to a real value within the range [MIN_VAL, MAX_VAL]. The fitness is the value
 * of the Rastrigin function.
 *
 * @author MAUKOSKI, W. X.
 */
public class IndividualRastrigin extends Individual<Variables> {

    /**
     * Lower bound of the domain for each variable.
     */
    private static final double MIN_VAL = -5.12;

    /**
     * Upper bound of the domain for each variable.
     */
    private static final double MAX_VAL = 5.12;

    /**
     * Number of bits used to represent each variable.
     */
    private int genotypeSize;

    /**
     * Number of variables (dimension of the problem).
     */
    private int numVariables;

    /**
     * Constructs an individual with a randomly initialized genotype.
     *
     * @param mutationTax the mutation rate
     * @param genotypeSize the number of bits per variable
     * @param numVariables the number of variables (dimension)
     */
    public IndividualRastrigin(double mutationTax, int genotypeSize, int numVariables) {
        super(mutationTax);
        this.genotypeSize = genotypeSize;
        this.numVariables = numVariables;
        this.genotypeInitialization(genotypeSize, numVariables);
        this.setFitness(this.fitnessEvaluate());
    }

    /**
     * Constructs an individual from a given genotype.
     *
     * @param mutationTax the mutation rate
     * @param genotype the genotype to assign
     */
    public IndividualRastrigin(double mutationTax, Variables genotype) {
        super(mutationTax);
        this.genotypeSize = genotype.getX()[0].length();
        this.numVariables = genotype.getX().length;
        this.setGenotype(genotype);
        this.setFitness(this.fitnessEvaluate());
    }

    /**
     * Evaluates the fitness of this individual using the Rastrigin function.
     * The Rastrigin function is defined as 10 * n + sum(x_i^2 - 10 * cos(2 * pi * x_i)).
     *
     * @return the fitness value
     */
    @Override
    protected double fitnessEvaluate() {
        Variables genotype = this.getGenotype();
        String[] genes = genotype.getX();
        double result = 10 * numVariables;

        for (int i = 0; i < numVariables; i++) {
            double xi = decode(genes[i]);
            result += xi * xi - 10 * Math.cos(2.0 * Math.PI * xi);
        }
        return result;
    }

    /**
     * Decodes a binary string into a real value within the range [MIN_VAL, MAX_VAL].
     *
     * @param binary the binary string to decode
     * @return the decoded real value
     */
    private double decode(String binary) {
        long intValue = Long.parseLong(binary, 2);
        double maxInt = Math.pow(2, binary.length()) - 1;
        return MIN_VAL + (MAX_VAL - MIN_VAL) * (intValue / maxInt);
    }

    /**
     * Applies mutation to the given genotype. Each bit is flipped with a probability
     * equal to the mutation rate.
     *
     * @param genotype the genotype to mutate
     * @return the mutated genotype
     */
    @Override
    protected Variables mutation(Variables genotype) {
        String[] original = genotype.getX();
        String[] mutated = new String[original.length];
        Random r = new Random();

        for (int i = 0; i < original.length; i++) {
            StringBuilder sb = new StringBuilder();
            String gene = original[i];
            for (int j = 0; j < gene.length(); j++) {
                char bit = gene.charAt(j);
                if (r.nextDouble() < this.getMutationTax()) {
                    sb.append(bit == '1' ? '0' : '1');
                } else {
                    sb.append(bit);
                }
            }
            mutated[i] = sb.toString();
        }
        return new Variables(mutated);
    }

    /**
     * Performs a single-point crossover for each variable between this individual and
     * the given parent. The resulting children are then mutated and their fitness is
     * evaluated.
     *
     * @param parent the other parent individual
     * @return an array containing the two offspring individuals
     */
    @Override
    public Individual[] crossover(Individual parent) {
        Variables father = this.getGenotype();
        Variables mother = (Variables) parent.getGenotype();
        Random r = new Random();

        String[] son = new String[numVariables];
        String[] daughter = new String[numVariables];

        for (int i = 0; i < numVariables; i++) {
            String fGene = father.getX()[i];
            String mGene = mother.getX()[i];
            int point = r.nextInt(fGene.length());
            son[i] = fGene.substring(0, point) + mGene.substring(point);
            daughter[i] = mGene.substring(0, point) + fGene.substring(point);
        }

        IndividualRastrigin child1 = this.createIndividual(new Variables(son));
        IndividualRastrigin child2 = this.createIndividual(new Variables(daughter));

        Variables mutSon = this.mutation((Variables) child1.getGenotype());
        child1.setGenotype(mutSon);
        child1.setFitness(child1.fitnessEvaluate());

        Variables mutDaughter = this.mutation((Variables) child2.getGenotype());
        child2.setGenotype(mutDaughter);
        child2.setFitness(child2.fitnessEvaluate());

        return new Individual[]{child1, child2};
    }

    /**
     * Factory method to create a new individual of the same type from a given genotype.
     * Subclasses should override this method to return instances of the correct class.
     *
     * @param genotype the genotype for the new individual
     * @return a new instance of IndividualRastrigin
     */
    protected IndividualRastrigin createIndividual(Variables genotype) {
        return new IndividualRastrigin(this.getMutationTax(), genotype);
    }

    /**
     * Initializes the genotype with random binary strings for each variable.
     *
     * @param geneLength the number of bits per variable
     * @param numVars the number of variables
     */
    private void genotypeInitialization(int geneLength, int numVars) {
        Random r = new Random();
        String[] x = new String[numVars];
        for (int i = 0; i < numVars; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < geneLength; j++) {
                sb.append(r.nextInt(2));
            }
            x[i] = sb.toString();
        }
        this.setGenotype(new Variables(x));
    }

}
