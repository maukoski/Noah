package com.mycompany.noah.tools;

import com.mycompany.noah.core.Agent;
import com.mycompany.noah.core.Individual;
import com.mycompany.noah.core.selection.BoltzmanSelection;
import com.mycompany.noah.core.selection.ProbabilisticTournamentSelection;
import com.mycompany.noah.core.selection.SelectionStrategy;
import com.mycompany.noah.core.selection.StochasticUniversalSamplingSelection;
import com.mycompany.noah.core.selection.TruncationSelection;
import com.mycompany.noah.core.selection.RankingSelection;
import com.mycompany.noah.core.selection.RouletteSelection;
import com.mycompany.noah.core.selection.TournamentSelection;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Utility class for benchmarking different selection strategies in the
 * genetic algorithm framework.
 * <p>
 * {@code BenchmarkRunner} runs multiple independent trials (epochs) for each
 * available selection strategy and collects statistics on the error (distance
 * from the known perfect fitness) and the number of generations needed to
 * find the best solution. This allows for comparative analysis of the
 * performance and convergence behavior of different selection methods.
 * </p>
 * <p>
 * The following selection strategies are benchmarked:
 * <ul>
 *   <li>Roulette (fitness proportionate)</li>
 *   <li>Ranking</li>
 *   <li>Tournament</li>
 *   <li>Truncation</li>
 *   <li>Stochastic Universal Sampling (SUS)</li>
 *   <li>Probabilistic Tournament</li>
 *   <li>Boltzmann</li>
 * </ul>
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class BenchmarkRunner {

    /** Supplier for creating new individual instances. */
    private static Supplier<Individual> individualSupplier;
    
    /** Size of the population for each trial. */
    private static int populationSize;
    
    /** Maximum number of generations per trial. */
    private static int maxGenerations;
    
    /** Number of individuals to replace per generation. */
    private static int numReplacements;

    /**
     * Runs the complete benchmark suite, evaluating all available selection
     * strategies over multiple independent trials.
     *
     * @param a the agent containing the configuration parameters
     * @param perfectFitness the known optimal fitness value for error calculation
     * @param epoch the number of independent trials to run per strategy
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @param tournamentSize the number of individuals per tournament (for tournament-based strategies)
     * @param trunc the percentage of elite individuals (for truncation selection)
     * @param probab the probability of selecting the best (for probabilistic tournament)
     * @param initialTemperature the starting temperature (for Boltzmann selection)
     */
    public static void evaluation(Agent a, double perfectFitness, int epoch, String optimizationMode,
            int tournamentSize, int trunc, double probab, double initialTemperature) {
        // Stores the original agent's parameters to create identical populations
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

    /**
     * Benchmarks the roulette wheel selection strategy.
     *
     * @param perfectFitness the known optimal fitness value
     * @param epoch the number of independent trials
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     */
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

    /**
     * Benchmarks the ranking selection strategy.
     *
     * @param perfectFitness the known optimal fitness value
     * @param epoch the number of independent trials
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     */
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

    /**
     * Benchmarks the tournament selection strategy.
     *
     * @param perfectFitness the known optimal fitness value
     * @param epoch the number of independent trials
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @param tournamentSize the number of individuals per tournament
     */
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

    /**
     * Benchmarks the truncation selection strategy.
     *
     * @param perfectFitness the known optimal fitness value
     * @param epoch the number of independent trials
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @param trunc the percentage of elite individuals to retain
     */
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

    /**
     * Benchmarks the Stochastic Universal Sampling selection strategy.
     *
     * @param perfectFitness the known optimal fitness value
     * @param epoch the number of independent trials
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     */
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

    /**
     * Benchmarks the probabilistic tournament selection strategy.
     *
     * @param perfectFitness the known optimal fitness value
     * @param epoch the number of independent trials
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @param tournamentSize the number of individuals per tournament
     * @param probab the probability of selecting the best individual
     */
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

    /**
     * Benchmarks the Boltzmann selection strategy.
     *
     * @param perfectFitness the known optimal fitness value
     * @param epoch the number of independent trials
     * @param optimizationMode {@code "MINIMIZATION"} or {@code "MAXIMIZATION"}
     * @param initialtemperature the starting temperature for the cooling schedule
     */
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

    /**
     * Records the statistics for a single trial.
     *
     * @param a the agent after running the genetic algorithm
     * @param perfectFitness the known optimal fitness value
     * @param index the trial index
     * @param errors array to store the fitness error for this trial
     * @param generations array to store the generation count for this trial
     */
    private static void recordStats(Agent a, double perfectFitness, int index, double[] errors, int[] generations) {
        double bestFitness = a.getBest().getIndividual().getFitness();
        double error = Math.abs(bestFitness - perfectFitness);
        int gen = a.getBest().getGeneration();
        errors[index] = error;
        generations[index] = gen;
        System.out.printf("Epoch: %d  generation: %d  Fitness: %f  Error: %f%n",
                index + 1, gen, bestFitness, error);
    }

    /**
     * Displays summary statistics for a selection strategy.
     *
     * @param methodName the name of the selection strategy
     * @param errors array of fitness errors from all trials
     * @param generations array of generation counts from all trials
     */
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

    /**
     * Calculates the sample standard deviation for an array of doubles.
     *
     * @param values the data array
     * @param mean the pre-calculated mean of the values
     * @return the sample standard deviation
     */
    private static double calculateStdDev(double[] values, double mean) {
        double sumSq = 0.0;
        for (double v : values) {
            sumSq += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

    /**
     * Calculates the sample standard deviation for an array of integers.
     *
     * @param values the data array
     * @param mean the pre-calculated mean of the values
     * @return the sample standard deviation
     */
    private static double calculateStdDev(int[] values, double mean) {
        double sumSq = 0.0;
        for (int v : values) {
            sumSq += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

    /**
     * Creates a fresh population with newly generated individuals,
     * resetting the agent to initial conditions for a new trial.
     *
     * @param selection the selection strategy to be used
     * @return a new {@code Agent} instance with a fresh population
     */
    private static Agent populationReset(SelectionStrategy selection) {
        Individual[] freshPop = new Individual[populationSize];
        for (int i = 0; i < populationSize; i++) {
            freshPop[i] = individualSupplier.get();
        }
        return new Agent(populationSize, maxGenerations, numReplacements, selection, freshPop);
    }
}