/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.examples.unimodality.Sphere;

import com.mycompany.noah.core.Individual;
import java.util.Random;

/**
 * Represents an individual with a binary genotype for use in a genetic
 * algorithm. The genotype is initialized as a binary string, and the fitness is
 * evaluated by interpreting the genotype as a binary number. This class extends
 * the abstract {@link Individual} class.
 *
 * @author willi
 */
public class IndividualSphere extends Individual<String> {

    /**
     * Constructs an IndividualSphere with a specified mutation rate and
     * genotype size.
     *
     * @param mutationTax the mutation rate for this individual
     * @param genotypeSize the size of the binary genotype (number of bits)
     */
    public IndividualSphere(double mutationTax, int genotypeSize) {
        super(mutationTax);
        this.genotypeInitialization(genotypeSize);
        this.setFitness(this.fitnessEvaluate());
    }

    /**
     * Constructs an IndividualSphere from a given genotype string.
     *
     * @param mutationTax the mutation rate for this individual
     * @param children the genotype (as a binary string) for the child
     * individual
     */
    private IndividualSphere(double mutationTax, String children) {
        super(mutationTax);
        this.setGenotype(children);
        this.setFitness(this.fitnessEvaluate());
    }

    /**
     * Initializes the genotype of the individual as a random binary string with
     * the specified number of bits.
     *
     * @param genotypeSize the size of the genotype in bits
     */
    private void genotypeInitialization(int genotypeSize) {
        Random r = new Random();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < genotypeSize; i++) {
            result.append(r.nextInt(2));
        }
        this.setGenotype(result.toString());
    }

    /**
     * Evaluates the fitness of this individual by interpreting the binary
     * genotype as a decimal value.
     *
     * @return the fitness value, which is the decimal equivalent of the binary
     * genotype
     */
    @Override
    protected double fitnessEvaluate() {
        return  Math.pow(Long.parseLong(this.getGenotype(), 2),2);
    }

    /**
     * Applies mutation to the given genotype of this individual. Each gene has
     * a chance to mutate based on the mutation rate.
     *
     * @param genotype the binary string representing the genotype
     * @return the mutated genotype
     */
    @Override
    protected String mutation(String genotype) {
        StringBuilder result = new StringBuilder();
        Random r = new Random();

        for (int i = 0; i < genotype.length(); i++) {
            if (r.nextDouble() < this.getMutationTax()) {
                // Flip the gene: '1' becomes '0' and '0' becomes '1'
                result.append(genotype.charAt(i) == '1' ? '0' : '1');
            } else {
                result.append(genotype.charAt(i)); // Keep the gene unchanged
            }
        }
        return result.toString();
    }

    /**
     * Prints the genotype and fitness of this individual to the console.
     */
    public void print() {
        for (int i = 0; i < this.getGenotype().length(); i++) {
            System.out.print(this.getGenotype().charAt(i));
        }
        System.out.println(" Fitness: " + this.getFitness());
    }

    /**
     * Performs the crossover operation between this individual and another
     * parent. Generates two offspring by combining parts of the genotypes from
     * both parents.
     *
     * @param parent the second individual (the other parent) for crossover
     * @return an array containing two offspring individuals
     */
    @Override
    public Individual[] crossover(Individual parent) {
        String father = this.getGenotype();
        String mother = parent.getGenotype().toString();
        IndividualSphere[] children = new IndividualSphere[2];

        StringBuilder son = new StringBuilder();
        StringBuilder daughter = new StringBuilder();

        Random random = new Random();

        // Gerar ponto de corte aleatório entre 1 e o comprimento do genótipo - 1
        int crossoverPoint = random.nextInt(father.length() - 1) + 1;

        // Combine partes do genótipo de ambos os pais com base no ponto de corte
        son.append(father.substring(0, crossoverPoint));
        son.append(mother.substring(crossoverPoint, mother.length()));

        daughter.append(mother.substring(0, crossoverPoint));
        daughter.append(father.substring(crossoverPoint, father.length()));

        // Aplicar mutação aos genótipos dos filhos
        String mutatedSon = this.mutation(son.toString());
        String mutatedDaughter = this.mutation(daughter.toString());

        // Criar os novos indivíduos filhos com o genótipo mutado
        children[0] = new IndividualSphere(this.getMutationTax(), mutatedSon);
        children[1] = new IndividualSphere(this.getMutationTax(), mutatedDaughter);

        return children;
    }

}
