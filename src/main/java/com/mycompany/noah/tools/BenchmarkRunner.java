package com.mycompany.noah.tools;

import com.mycompany.noah.core.Agent;
import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.Selection.BoltzmanSelection;
import com.mycompany.noah.core.Selection.ProbabilisticTournamentSelection;
import com.mycompany.noah.core.Selection.SelectionStrategy;
import com.mycompany.noah.core.Selection.StochasticUniversalSamplingSelection;
import com.mycompany.noah.core.Selection.TruncationSelection;
import com.mycompany.noah.core.selection.RankingSelection;
import com.mycompany.noah.core.selection.RouletteSelection;
import com.mycompany.noah.core.selection.TournamentSelection;
import java.util.Arrays;
import java.util.function.Supplier;

public class BenchmarkRunner {

    private static Supplier<Individual> individualSupplier;
    private static int populationSize;
    private static int maxGenerations;
    private static int numReplacements;

    public static void evaluation(Agent a, double perfectFitness, int epoch, String optimizationMode,
            int tournamentSize, int trunc, double probab, double initialTemperature) {
        // Armazena os parâmetros do agente original para criar novas populações idênticas
        individualSupplier = a.getIndividualSupplier();
        populationSize = a.getPopulationSize();
        maxGenerations = a.getEpoch();
        numReplacements = a.getSizeSelection();

        rouleteEvaluation(perfectFitness, epoch, optimizationMode);
        rankingEvaluation(perfectFitness, epoch, optimizationMode);
        tournamentEvaluation(perfectFitness, epoch, optimizationMode, tournamentSize);
        truncationEvaluation(perfectFitness, epoch, optimizationMode, trunc);
        StochasticUniversalEvaluation(perfectFitness, epoch, optimizationMode);
        ProbabilisticTournamentSelectionEvaluation(perfectFitness, epoch, optimizationMode, tournamentSize, probab);
        BoltzmanEvaluation(perfectFitness, epoch, optimizationMode, initialTemperature);
    }

    private static void rouleteEvaluation(double perfectFitness, int epoch, String optimizationMode) {
        double[] errors = new double[epoch];
        int[] generations = new int[epoch];
        SelectionStrategy selection = new RouletteSelection();

        for (int i = 0; i < epoch; i++) {
            Agent a = populationReset(selection);
            a.run(optimizationMode, selection);
            recordStats(a, perfectFitness, i, errors, generations);
        }
        showStats("ROULETTE", errors, generations);
    }

    private static void rankingEvaluation(double perfectFitness, int epoch, String optimizationMode) {
        double[] errors = new double[epoch];
        int[] generations = new int[epoch];

        SelectionStrategy selection = new RankingSelection();
        for (int i = 0; i < epoch; i++) {
            Agent a = populationReset(selection);
            a.run(optimizationMode, selection);
            recordStats(a, perfectFitness, i, errors, generations);
        }
        showStats("RANKING", errors, generations);
    }

    private static void tournamentEvaluation(double perfectFitness, int epoch, String optimizationMode, int tournamentSize) {
        double[] errors = new double[epoch];
        int[] generations = new int[epoch];

        SelectionStrategy selection = new TournamentSelection(tournamentSize);
        for (int i = 0; i < epoch; i++) {
            Agent a = populationReset(selection);
            a.run(optimizationMode, selection);
            recordStats(a, perfectFitness, i, errors, generations);
        }
        showStats("TOURNAMENT", errors, generations);
    }

    private static void truncationEvaluation(double perfectFitness, int epoch, String optimizationMode, int trunc) {
        double[] errors = new double[epoch];
        int[] generations = new int[epoch];

        SelectionStrategy selection = new TruncationSelection(trunc);
        for (int i = 0; i < epoch; i++) {
            Agent a = populationReset(selection);
            a.run(optimizationMode, selection);
            recordStats(a, perfectFitness, i, errors, generations);
        }
        showStats("Truncation", errors, generations);
    }

    private static void StochasticUniversalEvaluation(double perfectFitness, int epoch, String optimizationMode) {
        double[] errors = new double[epoch];
        int[] generations = new int[epoch];

        SelectionStrategy selection = new StochasticUniversalSamplingSelection();
        for (int i = 0; i < epoch; i++) {
            Agent a = populationReset(selection);
            a.run(optimizationMode, selection);
            recordStats(a, perfectFitness, i, errors, generations);
        }
        showStats("Stocastic", errors, generations);
    }

    private static void ProbabilisticTournamentSelectionEvaluation(double perfectFitness, int epoch, String optimizationMode,
            int tournamentSize, double probab) {
        double[] errors = new double[epoch];
        int[] generations = new int[epoch];

        SelectionStrategy selection = new ProbabilisticTournamentSelection(tournamentSize, probab);
        for (int i = 0; i < epoch; i++) {
            Agent a = populationReset(selection);
            a.run(optimizationMode, selection);
            recordStats(a, perfectFitness, i, errors, generations);
        }
        showStats("PROBABILISTIC TOURNAMENT", errors, generations);
    }

    private static void BoltzmanEvaluation(double perfectFitness, int epoch, String optimizationMode, double initialtemperature) {
        double[] errors = new double[epoch];
        int[] generations = new int[epoch];

        SelectionStrategy selection = new BoltzmanSelection(initialtemperature);
        for (int i = 0; i < epoch; i++) {
            Agent a = populationReset(selection);
            a.run(optimizationMode, selection);
            recordStats(a, perfectFitness, i, errors, generations);
        }
        showStats("BOLTZMAN", errors, generations);
    }

    private static void recordStats(Agent a, double perfectFitness, int index, double[] errors, int[] generations) {
        double bestFitness = a.getBest().getIndividual().getFitness();
        double error = Math.abs(bestFitness - perfectFitness);
        int gen = a.getBest().getGeneration();
        errors[index] = error;
        generations[index] = gen;
        System.out.printf("Epoch: %d  generation: %d  Fitness: %f  Error: %f%n",
                index + 1, gen, bestFitness, error);
    }

    private static void showStats(String methodName, double[] errors, int[] generations) {
        System.out.println("=====================================================================");
        System.out.println(">>> Statistics for " + methodName + " <<<");

        double avgError = Arrays.stream(errors).average().orElse(0.0);
        double stdError = calculateStdDev(errors, avgError);

        double avgGen = Arrays.stream(generations).average().orElse(0.0);
        double stdGen = calculateStdDev(generations, avgGen);

        System.out.printf("Average Error:      %.6f ± %.6f%n", avgError, stdError);
        System.out.printf("Average Generation: %.2f ± %.2f%n", avgGen, stdGen);
        System.out.println("=====================================================================");
    }

    private static double calculateStdDev(double[] values, double mean) {
        double sumSq = 0.0;
        for (double v : values) {
            sumSq += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

    private static double calculateStdDev(int[] values, double mean) {
        double sumSq = 0.0;
        for (int v : values) {
            sumSq += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

    private static Agent populationReset(SelectionStrategy selection) {
        Individual[] freshPop = new Individual[populationSize];
        for (int i = 0; i < populationSize; i++) {
            freshPop[i] = individualSupplier.get();
        }
        return new Agent(populationSize, maxGenerations, numReplacements, selection, freshPop);
    }
}
