package com.mycompany.noah.core;

import com.mycompany.noah.core.Selection.SelectionStrategy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Supplier;
import javax.swing.JFrame;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

/**
 * The Agent class implements a genetic algorithm framework for evolving a
 * population of individuals. It supports selection, crossover, mutation, and
 * optimization processes (minimization or maximization) for a specified number
 * of generations (epochs).
 *
 * @param <T> the type of Individual used in the genetic algorithm
 *
 * @author MAUKOSKI, W. X.
 */
public class Agent<T extends Individual<?>> {

    private Individual[] population;
    private final int populationSize;
    private Best best;
    private final int epoch;
    private final int sizeSelection;
    public final static String MINIMIZATION = "MINIMIZATION";
    public final static String MAXIMIZATION = "MAXIMIZATION";
    public final static String TOURNAMENT = "TOURNAMENT";
    public final static String ROULETTE = "ROULETTE";
    public final static String RANKING = "RANKING";
    private String optimazionalMode;
    private SelectionStrategy selection;
    private final ArrayList<Double> fitnessHistory;
    private Supplier<Individual> individualSupplier; // campo adicionado

    /**
     * Constructs an Agent with a specified population size and number of
     * generations (epochs) to run.
     *
     * @param individualConstructor a Supplier that provides instances of the
     * concrete Individual class
     * @param populationSize the size of the population of individuals
     * @param epoch the number of generations (epochs) to run the algorithm
     * @param sizeSelection
     */
    public Agent(Supplier<Individual> individualConstructor, int populationSize, int epoch, int sizeSelection) {
        this.individualSupplier = individualConstructor;  // guarda referência
        this.populationSize = populationSize;
        this.epoch = epoch;
        this.sizeSelection = sizeSelection;
        this.fitnessHistory = new ArrayList<>();
        this.poplationInitialization(individualConstructor);

    }

    public Agent(int populationSize, int epoch, int sizeSelection, Individual[] population) {
        this.populationSize = populationSize;
        this.epoch = epoch;
        this.sizeSelection = sizeSelection;
        this.fitnessHistory = new ArrayList<>();
        this.population = population;
    }

    public Agent(int populationSize, int epoch, int sizeSelection,
            SelectionStrategy selection, Individual[] population) {
        this.populationSize = populationSize;
        this.epoch = epoch;
        this.sizeSelection = sizeSelection;
        this.selection = selection;               // ←  store de strategy
        this.fitnessHistory = new ArrayList<>();
        this.population = population;
    }

    /**
     * Initializes the population by creating individuals using the provided
     * constructor.
     *
     * @param individualConstructor a Supplier that provides instances of the
     * concrete Individual class
     */
    private void poplationInitialization(Supplier<Individual> individualConstructor) {
        this.population = new Individual[populationSize];
        for (int i = 0; i < this.populationSize; i++) {
            this.population[i] = individualConstructor.get();
        }
    }

        /**
     * Executes the genetic algorithm for a specified number of epochs,
     * optimizing either for minimization or maximization of fitness. In each
     * epoch, the population is evaluated, and new individuals are generated
     * based on the selected optimization method and selection mode.
     *
     * @param optimization either {@code MINIMIZATION} or {@code MAXIMIZATION}
     * to define the fitness optimization goal.
     * @param selection the selection strategy to use for generating the next
     * generation, such as {@code TOURNAMENT} or {@code ROULETTE}.
     *
     */
    public void run(String optimization, SelectionStrategy selection) {
        this.best = new Best();
        this.best.setGeneration(0);
        this.best.setIndividual(this.population[0]);
        this.optimazionalMode = optimization;
        int newIndividuasl = this.sizeSelection;

        for (int i = 0; i < epoch; i++) {
            switch (optimization) {
                case MINIMIZATION -> {
                    Arrays.sort(this.population);
                    if (this.best.getIndividual().getFitness() > this.population[0].getFitness()) {
                        this.best.setIndividual(this.population[0]);
                        this.best.setGeneration(i);
                    }
                }

                case MAXIMIZATION -> {
                    Arrays.sort(this.population, Comparator.reverseOrder());
                    if (this.best.getIndividual().getFitness() < this.population[0].getFitness()) {
                        this.best.setIndividual(this.population[0]);
                        this.best.setGeneration(i);
                    }
                }

                default -> {
                    System.out.println("Please, select a valid optimization method (MINIMIZATION or MAXIMIZATION).");
                    System.exit(0);
                }
            }

            // Add the best individual's fitness to the history list
            fitnessHistory.add(this.best.getIndividual().getFitness());

            // Generate the new generation with the chosen selection mode
            this.newGeneration(newIndividuasl, selection);
        }

        // Plot the fitness evolution over generations
        //this.plotFitnessHistory();
    }
    
    
    /**
     * Creates a new generation of individuals based on the specified selection
     * mode.
     *
     * @param newIndividuasl the number of new individuals to generate
     */
    private void newGeneration(int newIndividuasl, SelectionStrategy selection) {
        this.population = selection.evolvePopulation(population, newIndividuasl, optimazionalMode);
    }

    private void tournament(int newIndividuasl) {
        for (int i = 0; i < newIndividuasl / 2; i++) {
            int father = this.tournamentSelection();
            int mother = this.tournamentSelection();

            Individual[] crossover = this.population[father].crossover(this.population[mother]);

            this.population[father] = crossover[0];
            this.population[mother] = crossover[1];

        }
    }

       /**
     * Performs tournament selection, selecting a subset of individuals to breed
     * new individuals based on their fitness.
     *
     * @return the index of the selected individual
     */
    private int tournamentSelection() {
        int[] tournament = new int[this.sizeSelection];
        Random r = new Random();

        // Fill the array with unique values
        for (int i = 0; i < tournament.length; i++) {
            int selectedIndividual;
            boolean flag;

            do {
                selectedIndividual = r.nextInt(this.populationSize);
                flag = false;

                // Check if the index has already been selected
                for (int j = 0; j < i; j++) {
                    if (tournament[j] == selectedIndividual) {
                        flag = true;
                        break;
                    }
                }

            } while (flag);  // Repeat until an unselected index is found

            tournament[i] = selectedIndividual;  // Add the selected index
        }

        Arrays.sort(tournament);

        return tournament[0];

    }

        private void roulette(int newIndividuasl) {
        Random r = new Random();
        int popSize = this.populationSize;

        // 1. Build adjusted fitness array (non-negative) for roulette
        double[] adjustedFitness = new double[popSize];

        if (this.optimazionalMode.equals(MAXIMIZATION)) {
            // For maximization, higher fitness should have higher chance.
            // If there are negatives, shift so that the lowest becomes 0.
            double minFit = Double.MAX_VALUE;
            for (int i = 0; i < popSize; i++) {
                if (population[i].getFitness() < minFit) {
                    minFit = population[i].getFitness();
                }
            }
            double offset = (minFit < 0) ? -minFit : 0;
            for (int i = 0; i < popSize; i++) {
                adjustedFitness[i] = population[i].getFitness() + offset;
            }
        } else { // MINIMIZATION
            // For minimization, lower fitness should have higher chance.
            // 1. Find maximum fitness (worst) and minimum fitness (best)
            double maxFit = Double.MIN_VALUE;
            double minFit = Double.MAX_VALUE;
            for (int i = 0; i < popSize; i++) {
                double fit = population[i].getFitness();
                if (fit > maxFit) {
                    maxFit = fit;
                }
                if (fit < minFit) {
                    minFit = fit;
                }
            }
            // 2. Create an array where the lowest fitness has the highest value.
            //    Formula: maxFit - fitness[i] + 1 (ensures all > 0)
            for (int i = 0; i < popSize; i++) {
                adjustedFitness[i] = maxFit - population[i].getFitness() + 1;
            }
        }

        // 2. Calculate total adjusted fitness
        double totalAdjustedFitness = 0;
        for (double f : adjustedFitness) {
            totalAdjustedFitness += f;
        }

        // 3. For each pair of parents (replace newIndividuasl/2 individuals)
        for (int i = 0; i < newIndividuasl / 2; i++) {
            int[] parents = new int[2];

            // Select two distinct parents
            for (int j = 0; j < 2; j++) {
                double point = r.nextDouble() * totalAdjustedFitness;
                double sum = 0;
                int selected = 0;
                while (selected < popSize - 1 && sum < point) {
                    sum += adjustedFitness[selected];
                    selected++;
                }
                parents[j] = selected;
            }

            // Crossover
            Individual[] children = population[parents[0]].crossover(population[parents[1]]);

            // Replace parents with children
            population[parents[0]] = children[0];
            population[parents[1]] = children[1];
        }
    }

    /**
     * Performs linear ranking selection to generate a new generation.
     * <p>
     * The population must be sorted beforehand (best individual at index 0).
     * Each individual receives a weight proportional to its rank position: the
     * best receives the highest weight, the worst the lowest, following a
     * decreasing linear distribution. Then, {@code newIndividuasl / 2} parent
     * pairs are drawn with probability proportional to these weights and
     * replaced by the children generated via crossover.
     * </p>
     *
     * @param newIndividuasl total number of individuals to be replaced (must be
     * even). Corresponds to the number of selected parents (each pair produces
     * two children that replace the parents).
     */
    public void ranking(int newIndividuasl) {
        double[] ranking = new double[this.population.length];

        int n = (((1 + this.population.length) * this.population.length) / 2);

        for (int i = 0; i < ranking.length; i++) {
            ranking[i] = (this.populationSize - i);
        }

        Random r = new Random();

        for (int j = 0; j < newIndividuasl / 2; j++) {

            double fatherSelectedIndex = r.nextInt(n);
            double motherSelectedIndex = r.nextInt(n);

            Individual father;

            int fatherIndex = 0;
            while (fatherSelectedIndex > 0) {
                fatherSelectedIndex -= ranking[fatherIndex];
                fatherIndex++;
            }
            father = population[fatherIndex];

            int MotherIndex = 0;
            while (motherSelectedIndex > 0) {
                motherSelectedIndex -= ranking[MotherIndex];
                MotherIndex++;
            }

            Individual[] children = father.crossover(this.population[MotherIndex]);

            population[fatherIndex] = children[0];
            population[MotherIndex] = children[1];
        }
    }

    public void reset() {
        this.poplationInitialization(this.individualSupplier);
        this.fitnessHistory.clear();
        this.best = null; // será recriado no próximo run()
    }

        /**
     * Plots the fitness history over the generations using XChart.
     */
    public void plotFitnessHistory() {
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Fitness Over Generations")
                .xAxisTitle("Generation").yAxisTitle("Fitness").build();

        // Converting the fitnessHistory list to an array
        double[] fitnessArray = fitnessHistory.stream().mapToDouble(Double::doubleValue).toArray();
        double[] generations = new double[fitnessArray.length];
        for (int i = 0; i < generations.length; i++) {
            generations[i] = i;
        }

        // Adding data to the chart
        chart.addSeries("Best Fitness", generations, fitnessArray);

        // Displaying the chart in a window
        JFrame frame = new JFrame("Fitness Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        XChartPanel<XYChart> panel = new XChartPanel<>(chart);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public Supplier<Individual> getIndividualSupplier() {
        return this.individualSupplier;
    }

    public Individual[] getPopulation() {
        return population;
    }

    public Best getBest() {
        return best;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getEpoch() {
        return epoch;
    }

    public int getSizeSelection() {
        return sizeSelection;
    }

}
