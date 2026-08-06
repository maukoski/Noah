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
 * @author willi
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
        this.selection = selection;               // ← armazena a estratégia
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
     * @param selectionMode the selection mode to use for generating the next
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

            // Adicionar o fitness do melhor indivíduo na lista de histórico
            fitnessHistory.add(this.best.getIndividual().getFitness());

            // Gerar a nova geração com o modo de seleção escolhido
            this.newGeneration(newIndividuasl, selection);
        }

        // Plotar a evolução do fitness ao longo das gerações
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
     * @param newIndividuasl the number of new individuals to generate
     */
    private int tournamentSelection() {
        int[] tournament = new int[this.sizeSelection];
        Random r = new Random();

        // Preencher o array com valores únicos
        for (int i = 0; i < tournament.length; i++) {
            int selectedIndividual;
            boolean flag;

            do {
                selectedIndividual = r.nextInt(this.populationSize);
                flag = false;

                // Verificar se o índice já foi selecionado
                for (int j = 0; j < i; j++) {
                    if (tournament[j] == selectedIndividual) {
                        flag = true;
                        break;
                    }
                }

            } while (flag);  // Repete até encontrar um índice não selecionado

            tournament[i] = selectedIndividual;  // Adiciona o índice selecionado
        }

        Arrays.sort(tournament);

        return tournament[0];

    }

    private void roulette(int newIndividuasl) {
        Random r = new Random();
        int popSize = this.populationSize;

        // 1. Construir array de fitness ajustado (não negativo) para roleta
        double[] adjustedFitness = new double[popSize];

        if (this.optimazionalMode.equals(MAXIMIZATION)) {
            // Para maximização, fitness maior deve ter maior chance.
            // Se houver negativos, deslocamos para que o menor fique em 0.
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
            // Para minimização, fitness menor deve ter maior chance.
            // 1. Encontrar o fitness máximo (pior) e mínimo (melhor)
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
            // 2. Criar um array onde o menor fitness tem o maior valor.
            //    Fórmula: maxFit - fitness[i] + 1 (garante todos > 0)
            for (int i = 0; i < popSize; i++) {
                adjustedFitness[i] = maxFit - population[i].getFitness() + 1;
            }
        }

        // 2. Calcular fitness total ajustado
        double totalAdjustedFitness = 0;
        for (double f : adjustedFitness) {
            totalAdjustedFitness += f;
        }

        // 3. Para cada par de pais (substituir newIndividuasl/2 indivíduos)
        for (int i = 0; i < newIndividuasl / 2; i++) {
            int[] parents = new int[2];

            // Selecionar dois pais distintos
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

            // Substituir os pais pelos filhos
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

        // Convertendo a lista de fitnessHistory para um array
        double[] fitnessArray = fitnessHistory.stream().mapToDouble(Double::doubleValue).toArray();
        double[] generations = new double[fitnessArray.length];
        for (int i = 0; i < generations.length; i++) {
            generations[i] = i;
        }

        // Adicionando os dados ao gráfico
        chart.addSeries("Best Fitness", generations, fitnessArray);

        // Exibindo o gráfico em uma janela
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
