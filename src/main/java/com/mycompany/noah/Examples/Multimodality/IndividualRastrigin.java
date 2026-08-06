/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.Examples.Multimodality;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Variables;
import java.util.Random;

/**
 *
 * @author willi
 */
public class IndividualRastrigin extends Individual<Variables> {

    // Limites do domínio da função Rosenbrock (pode ajustar)
    private static final double MIN_VAL = -5.12;
    private static final double MAX_VAL = 5.12;

    private int genotypeSize;   // bits por variável
    private int numVariables;   // dimensão do problema

    public IndividualRastrigin(double mutationTax, int genotypeSize, int numVariables) {
        super(mutationTax);
        this.genotypeSize = genotypeSize;
        this.numVariables = numVariables;
        this.genotypeInitialization(genotypeSize, numVariables);
        this.setFitness(this.fitnessEvaluate());
    }

    public IndividualRastrigin(double mutationTax, Variables genotype) {
        super(mutationTax);
        this.genotypeSize = genotype.getX()[0].length();  // presume strings de mesmo tamanho
        this.numVariables = genotype.getX().length;
        this.setGenotype(genotype);
        this.setFitness(this.fitnessEvaluate());
    }

    @Override
    protected double fitnessEvaluate() {
        Variables genotype = this.getGenotype();
        String[] genes = genotype.getX();
        double result = 10 * numVariables;

        // Rastringin
        for (int i = 0; i < numVariables; i++) {
            double xi = decode(genes[i]);
            result +=  xi*xi -10 * Math.cos(2.0 * Math.PI * xi);
        }
        return result;
    }

    /**
     * Converte uma string binária em um valor real dentro de [MIN_VAL,
     * MAX_VAL].
     */
    private double decode(String binary) {
        long intValue = Long.parseLong(binary, 2);                     // 0 a 2^L - 1
        double maxInt = Math.pow(2, binary.length()) - 1;
        return MIN_VAL + (MAX_VAL - MIN_VAL) * (intValue / maxInt);
    }

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
                    // flip
                    sb.append(bit == '1' ? '0' : '1');
                } else {
                    sb.append(bit);
                }
            }
            mutated[i] = sb.toString();
        }
        return new Variables(mutated);

    }

    @Override
    public Individual[] crossover(Individual parent) {
        Variables father = this.getGenotype();
        Variables mother = (Variables) parent.getGenotype();
        Random r = new Random();

        String[] son = new String[numVariables];
        String[] daughter = new String[numVariables];

        // Crossover de um ponto por variável
        for (int i = 0; i < numVariables; i++) {
            String fGene = father.getX()[i];
            String mGene = mother.getX()[i];
            int point = r.nextInt(fGene.length());  // ponto de corte
            son[i] = fGene.substring(0, point) + mGene.substring(point);
            daughter[i] = mGene.substring(0, point) + fGene.substring(point);
        }

        // Cria os filhos (já com fitness avaliado sobre o genótipo crossover)
        // Depois:
        IndividualRastrigin child1 = this.createIndividual(new Variables(son));
        IndividualRastrigin child2 = this.createIndividual(new Variables(daughter));

        // Aplica mutação e ATUALIZA fitness
        Variables mutSon = this.mutation((Variables) child1.getGenotype());
        child1.setGenotype(mutSon);
        child1.setFitness(child1.fitnessEvaluate());   // ← corrigido

        Variables mutDaughter = this.mutation((Variables) child2.getGenotype());
        child2.setGenotype(mutDaughter);
        child2.setFitness(child2.fitnessEvaluate());   // ← corrigido

        return new Individual[]{child1, child2};

    }

    /**
     * Método de fábrica para criar um novo indivíduo do mesmo tipo a partir de
     * um genótipo. Subclasses devem sobrescrever para retornar instâncias da
     * classe correta.
     */
    protected IndividualRastrigin createIndividual(Variables genotype) {
        return new IndividualRastrigin(this.getMutationTax(), genotype);
    }

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
