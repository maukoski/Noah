package com.mycompany.noah.Examples.Unimodality.rosenbrock;

import com.mycompany.noah.core.Variables;
import com.mycompany.noah.core.Individual;
import java.util.Random;

public class IndividualRosenbrock extends Individual<Variables> {

    // Limites do domínio da função Rosenbrock (pode ajustar)
    private static final double MIN_VAL = -5.0;
    private static final double MAX_VAL = 10.0;

    private int genotypeSize;   // bits por variável
    private int numVariables;   // dimensão do problema

    /**
     * Construtor para inicialização aleatória.
     *
     * @param mutationTax taxa de mutação
     * @param genotypeSize número de bits por variável
     * @param numVariables quantidade de variáveis (dimensão)
     */
    public IndividualRosenbrock(double mutationTax, int genotypeSize, int numVariables) {
        super(mutationTax);
        this.genotypeSize = genotypeSize;
        this.numVariables = numVariables;
        this.genotypeInitialization(genotypeSize, numVariables);
        this.setFitness(this.fitnessEvaluate());
    }

    /**
     * Construtor a partir de um genótipo pronto (usado em crossover).
     */
    public IndividualRosenbrock(double mutationTax, Variables genotype) {
        super(mutationTax);
        this.genotypeSize = genotype.getX()[0].length();  // presume strings de mesmo tamanho
        this.numVariables = genotype.getX().length;
        this.setGenotype(genotype);
        this.setFitness(this.fitnessEvaluate());
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
    protected double fitnessEvaluate() {
        Variables genotype = this.getGenotype();
        String[] genes = genotype.getX();
        double result = 0.0;

        // Rosenbrock: soma sobre i de 100*(x_{i+1} - x_i^2)^2 + (1 - x_i)^2
        for (int i = 0; i < numVariables - 1; i++) {
            double xi = decode(genes[i]);
            double xi1 = decode(genes[i + 1]);
            result += 100.0 * Math.pow(xi1 - xi * xi, 2) + Math.pow(1.0 - xi, 2);
        }
        return result;
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
        IndividualRosenbrock child1 = this.createIndividual(new Variables(son));
        IndividualRosenbrock child2 = this.createIndividual(new Variables(daughter));

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
    protected IndividualRosenbrock createIndividual(Variables genotype) {
        return new IndividualRosenbrock(this.getMutationTax(), genotype);
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
