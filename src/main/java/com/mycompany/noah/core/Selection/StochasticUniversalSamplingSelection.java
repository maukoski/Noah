package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

public class StochasticUniversalSamplingSelection implements SelectionStrategy {

    private final Random r = new Random();

    public StochasticUniversalSamplingSelection() { }

    @Override
    public int[] selectParents(Individual[] population, int populationSize, 
                                              int numParents, String optimizationMode) {
        // 1. Ajuste de fitness (idêntico ao da Roleta, sem modificar os originais)
        double[] adjustedFitness = new double[populationSize];
        if (optimizationMode.equals("MAXIMIZATION")) {
            double minFit = Double.MAX_VALUE;
            for (int i = 0; i < populationSize; i++) {
                double f = population[i].getFitness();
                if (f < minFit) minFit = f;
            }
            double offset = (minFit < 0) ? -minFit : 0;
            for (int i = 0; i < populationSize; i++) {
                adjustedFitness[i] = population[i].getFitness() + offset;
            }
        } else { // MINIMIZATION
            double maxFit = -Double.MAX_VALUE;
            for (int i = 0; i < populationSize; i++) {
                double f = population[i].getFitness();
                if (f > maxFit) maxFit = f;
            }
            for (int i = 0; i < populationSize; i++) {
                adjustedFitness[i] = maxFit - population[i].getFitness() + 1;
            }
        }

        // 2. Calcular o total F = soma dos fitness ajustados
        double total = 0.0;
        for (double w : adjustedFitness) {
            total += w;
        }

        // 3. Espaçamento passo = F / numParents
        double step = total / numParents;

        // 4. Sortear um valor inicial start uniformemente entre 0 e passo
        double start = r.nextDouble() * step;

        // 5. Para cada um dos numParents ponteiros, encontrar o indivíduo correspondente
        int[] parents = new int[numParents];
        for (int k = 0; k < numParents; k++) {
            double pointer = start + k * step;
            double sum = 0.0;
            int idx = 0;
            // Percorre a população acumulando fitness ajustados até ultrapassar o ponteiro
            while (idx < populationSize - 1 && sum + adjustedFitness[idx] < pointer) {
                sum += adjustedFitness[idx];
                idx++;
            }
            parents[k] = idx;
        }

        // 6. Crossover e substituição (mesmo padrão das suas outras implementações)
        for (int i = 0; i < numParents - 1; i += 2) {
            Individual[] children = population[parents[i]].crossover(population[parents[i + 1]]);
            population[parents[i]] = children[0];
            population[parents[i + 1]] = children[1];
        }

        return parents;
    }
}