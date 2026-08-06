/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.core.Selection;

import com.mycompany.noah.core.Individual;
import java.util.Random;

/**
 *
 * @author willi
 */
public class BoltzmanSelection implements SelectionStrategy {

    private int count;
    private double initialTemperature;

    public BoltzmanSelection(double initialTemperature) {
        this.count = 0;
        this.initialTemperature = initialTemperature;
    }

    @Override
    public int[] selectParents(Individual<?>[] population, int populationSize,
            int numParents, String optimizationMode) {
        int[] parents = new int[numParents];
        double[] probabilityWeight = new double[populationSize];

        // Atualiza a temperatura com resfriamento: T = T0 / (1 + geração)
        double temperature = this.initialTemperature / (1.0 + this.count);

        // Calcula os pesos não normalizados
        double maxWeight = 0.0;
        for (int i = 0; i < populationSize; i++) {
            double fitness = population[i].getFitness();
            double exponent;
            if (optimizationMode.equals("MAXIMIZATION")) {
                exponent = fitness / temperature;
            } else { // MINIMIZATION
                exponent = -fitness / temperature;
            }
            // Para evitar overflow, limitamos o expoente a um valor seguro (~700)
            if (exponent > 700.0) {
                exponent = 700.0;
            }
            if (exponent < -700.0) {
                exponent = -700.0;
            }
            probabilityWeight[i] = Math.exp(exponent);
            if (probabilityWeight[i] > maxWeight) {
                maxWeight = probabilityWeight[i];
            }
        }

        // (Opcional) Subtrai o máximo para evitar perda de precisão em valores muito grandes,
        // mas não é estritamente necessário se já limitamos o expoente.
        // Calcula a soma total dos pesos
        double totalWeight = 0.0;
        for (double w : probabilityWeight) {
            totalWeight += w;
        }

        Random r = new Random();  // ou use um campo da classe

        // Seleciona numParents pais usando roleta
        for (int p = 0; p < numParents; p++) {
            double point = r.nextDouble() * totalWeight;
            double sum = 0.0;
            int idx = 0;
            while (idx < populationSize - 1 && sum + probabilityWeight[idx] < point) {
                sum += probabilityWeight[idx];
                idx++;
            }
            parents[p] = idx;
        }

        this.count++;  // incrementa o contador de gerações para a próxima chamada
        return parents;
    }

}
