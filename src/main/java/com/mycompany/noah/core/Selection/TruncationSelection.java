package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

public class TruncationSelection implements SelectionStrategy {

    private final double eliteFraction; // valor entre 0 e 1 (ex.: 0.10 = 10%)

    public TruncationSelection(double elitePercentage) {
        // Converte porcentagem para fração (ex.: 10 -> 0.10)
        if (elitePercentage < 0 || elitePercentage > 100) {
            throw new IllegalArgumentException("Elite percentage must be between 0 and 100");
        }
        this.eliteFraction = elitePercentage / 100.0;
    }

    /**
     * O truncamento não usa seleção de índices baseada em fitness,
     * pois a escolha dos pais é aleatória dentro da elite.
     * Retornamos um array vazio para cumprir o contrato.
     */
    

    /**
     * Substitui a lógica padrão: copia a elite e gera o restante
     * por crossover entre indivíduos escolhidos aleatoriamente na elite.
     */
    @Override
    public Individual<?>[] evolvePopulation(Individual<?>[] population, int numParents,
                                            String optimizationMode) {
        int popSize = population.length;
        int eliteCount = Math.max(1, (int) (popSize * eliteFraction));
        Individual<?>[] newPopulation = new Individual<?>[popSize];
        Random r = new Random();

        // 1. Copia a elite para o início da nova população
        for (int i = 0; i < eliteCount; i++) {
            newPopulation[i] = population[i];
        }

        // 2. Preenche os slots restantes com filhos de pais sorteados entre a elite
        int writeIndex = eliteCount;
        while (writeIndex < popSize) {
            int fatherIdx = r.nextInt(eliteCount);
            int motherIdx = r.nextInt(eliteCount);
            // Evita autofecundação, se possível
            while (motherIdx == fatherIdx && eliteCount > 1) {
                motherIdx = r.nextInt(eliteCount);
            }
            Individual<?>[] children = population[fatherIdx].crossover(population[motherIdx]);
            newPopulation[writeIndex++] = children[0];
            if (writeIndex < popSize) {
                newPopulation[writeIndex++] = children[1];
            }
        }

        return newPopulation;
    }

    @Override
    public int[] selectParents(Individual<?>[] population, int populationSize, int numParents, String optimizationMode) {
       return new int[0];
    }
}