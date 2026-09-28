package com.mycompany.noah.core;

import com.mycompany.noah.core.selection.RouletteSelection;
import com.mycompany.noah.core.selection.SelectionStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    /**
     * Individual mínimo para testes.
     * <p>
     * O crossover devolve referências idênticas para os pais, o que faz
     * a população permanecer com os mesmos indivíduos após a substituição.
     * Bom para testes de smoke onde só interessa que nada exploda.
     */
    private static class TestIndividual extends Individual<Double> {

        TestIndividual(double fitness) {
            super(0.02);
            setFitnessForTest(fitness);
        }

        @Override
        protected double fitnessEvaluate() {
            return getFitness();
        }

        @Override
        protected Double mutation(Double genotype) {
            return genotype;
        }

        @Override
        public Individual[] crossover(Individual parent) {
            return new Individual[] { this, parent };
        }

        private void setFitnessForTest(double fitness) {
            setFitness(fitness);
        }
    }

    private static Individual[] freshPopulation(double... fitnesses) {
        Individual[] population = new Individual[fitnesses.length];
        for (int i = 0; i < fitnesses.length; i++) {
            population[i] = new TestIndividual(fitnesses[i]);
        }
        return population;
    }

    /**
     * SelectionStrategy que apenas grava as chamadas feitas pelo Agent
     * e devolve a mesma população (não altera nada).
     */
    private static class RecordingSelection implements SelectionStrategy {

        int callCount = 0;
        int lastNumParents = -1;
        String lastOptimizationMode = null;
        Individual<?>[] lastPopulation = null;

        @Override
        public int[] selectParents(Individual<?>[] population, int popSize,
                                   int numParents, String optimizationMode) {
            return new int[0];
        }

        @Override
        public Individual<?>[] evolvePopulation(Individual<?>[] population,
                                                int numParents, String optimizationMode) {
            callCount++;
            lastNumParents = numParents;
            lastOptimizationMode = optimizationMode;
            lastPopulation = population;
            return population;
        }
    }

    /**
     * SelectionStrategy que devolve populações pré-programadas em sequência.
     * Útil para testar a atualização do "best-so-far" ao longo das gerações.
     */
    private static class ScriptedSelection implements SelectionStrategy {

        private final Iterator<Individual<?>[]> script;
        int callCount = 0;

        ScriptedSelection(Individual<?>[]... populations) {
            this.script = Arrays.asList(populations).iterator();
        }

        @Override
        public int[] selectParents(Individual<?>[] population, int popSize,
                                   int numParents, String optimizationMode) {
            return new int[0];
        }

        @Override
        public Individual<?>[] evolvePopulation(Individual<?>[] population,
                                                int numParents, String optimizationMode) {
            callCount++;
            if (script.hasNext()) {
                return script.next();
            }
            return population;
        }
    }

    // -------------------------------------------------------------------------
    // Construtores e getters
    // -------------------------------------------------------------------------

    @Test
    void supplierConstructorShouldPopulateWithRequestedSize() {
        AtomicInteger counter = new AtomicInteger(0);
        Supplier<Individual> supplier = () -> new TestIndividual(counter.incrementAndGet());

        Agent agent = new Agent(supplier, 5, 10, 3);

        assertEquals(5, agent.getPopulation().length);
        assertEquals(5, agent.getPopulationSize());
        assertEquals(10, agent.getEpoch());
        assertEquals(3, agent.getSizeSelection());
        assertNotNull(agent.getIndividualSupplier());
    }

    @Test
    void arrayConstructorShouldUseGivenArray() {
        Individual[] population = freshPopulation(10.0, 20.0, 30.0);

        Agent agent = new Agent(population.length, 5, 2, population);

        assertSame(population, agent.getPopulation());
        assertEquals(3, agent.getPopulationSize());
        assertEquals(5, agent.getEpoch());
        assertEquals(2, agent.getSizeSelection());
        assertNull(agent.getIndividualSupplier());
    }

    @Test
    void selectionConstructorShouldStoreSelectionAndPopulation() {
        Individual[] population = freshPopulation(10.0, 20.0);
        RecordingSelection selection = new RecordingSelection();

        Agent agent = new Agent(population.length, 5, 2, selection, population);

        assertSame(population, agent.getPopulation());
        assertNull(agent.getIndividualSupplier());
    }

    @Test
    void getBestShouldBeNullBeforeRun() {
        Individual[] population = freshPopulation(10.0, 20.0, 30.0);
        Agent agent = new Agent(population.length, 5, 2, population);

        assertNull(agent.getBest());
    }

    // -------------------------------------------------------------------------
    // run() — comportamento determinístico
    // -------------------------------------------------------------------------

    @Test
    void runShouldCallEvolvePopulationOncePerEpoch() {
        Individual[] population = freshPopulation(10.0, 20.0, 30.0);
        RecordingSelection selection = new RecordingSelection();
        Agent agent = new Agent(population.length, 7, 2, population);

        agent.run(Agent.MAXIMIZATION, selection);

        assertEquals(7, selection.callCount);
    }

    @Test
    void runShouldPassSizeSelectionAsNumParents() {
        Individual[] population = freshPopulation(10.0, 20.0, 30.0);
        RecordingSelection selection = new RecordingSelection();
        int sizeSelection = 4;
        Agent agent = new Agent(population.length, 3, sizeSelection, population);

        agent.run(Agent.MAXIMIZATION, selection);

        assertEquals(sizeSelection, selection.lastNumParents);
    }

    @Test
    void runShouldPassOptimizationModeToSelection() {
        Individual[] population = freshPopulation(10.0, 20.0, 30.0);
        RecordingSelection selection = new RecordingSelection();
        Agent agent = new Agent(population.length, 3, 2, population);

        agent.run(Agent.MINIMIZATION, selection);

        assertEquals(Agent.MINIMIZATION, selection.lastOptimizationMode);
    }

    @Test
    void runShouldSortDescendingForMaximizationBeforeSelection() {
        /*
         * O Agent ordena a população antes de chamar a SelectionStrategy.
         * Como a estratégia recebe o mesmo array (referência), é possível
         * inspecionar a ordenação através dela.
         *
         * Requer que Individual implemente Comparable (o que o
         * Arrays.sort(population) no run exige).
         */
        Individual[] population = freshPopulation(10.0, 100.0, 30.0, 50.0);
        RecordingSelection selection = new RecordingSelection();
        Agent agent = new Agent(population.length, 1, 2, population);

        agent.run(Agent.MAXIMIZATION, selection);

        Individual<?>[] passed = selection.lastPopulation;
        for (int i = 1; i < passed.length; i++) {
            assertTrue(
                    passed[i - 1].getFitness() >= passed[i].getFitness(),
                    "População deveria estar em ordem decrescente. i=" + i
            );
        }
    }

    @Test
    void runShouldSortAscendingForMinimizationBeforeSelection() {
        Individual[] population = freshPopulation(10.0, 100.0, 30.0, 50.0);
        RecordingSelection selection = new RecordingSelection();
        Agent agent = new Agent(population.length, 1, 2, population);

        agent.run(Agent.MINIMIZATION, selection);

        Individual<?>[] passed = selection.lastPopulation;
        for (int i = 1; i < passed.length; i++) {
            assertTrue(
                    passed[i - 1].getFitness() <= passed[i].getFitness(),
                    "População deveria estar em ordem crescente. i=" + i
            );
        }
    }

    // -------------------------------------------------------------------------
    // run() — best-so-far
    // -------------------------------------------------------------------------

    @Test
    void runShouldFindBestIndividualForMaximization() {
        Individual[] population = freshPopulation(10.0, 50.0, 100.0, 30.0);
        RecordingSelection selection = new RecordingSelection();
        Agent agent = new Agent(population.length, 5, 4, population);

        agent.run(Agent.MAXIMIZATION, selection);

        assertNotNull(agent.getBest());
        assertEquals(100.0, agent.getBest().getIndividual().getFitness(), 0.001);
        assertEquals(0, agent.getBest().getGeneration());
    }

    @Test
    void runShouldFindBestIndividualForMinimization() {
        Individual[] population = freshPopulation(10.0, 50.0, 100.0, 30.0);
        RecordingSelection selection = new RecordingSelection();
        Agent agent = new Agent(population.length, 5, 4, population);

        agent.run(Agent.MINIMIZATION, selection);

        assertNotNull(agent.getBest());
        assertEquals(10.0, agent.getBest().getIndividual().getFitness(), 0.001);
        assertEquals(0, agent.getBest().getGeneration());
    }

    @Test
    void runShouldRememberBestAcrossEpochsWhenPopulationChanges() {
        /*
         * Geração 0 -> fitness 100
         * Geração 1 -> fitness 200   (novo melhor)
         * Geração 2 -> fitness 150   (não supera)
         *
         * O best deve ficar em 200, na geração 1.
         */
        Individual<?>[] gen0 = { new TestIndividual(100.0) };
        Individual<?>[] gen1 = { new TestIndividual(200.0) };
        Individual<?>[] gen2 = { new TestIndividual(150.0) };

        ScriptedSelection selection = new ScriptedSelection(gen1, gen2);
        Agent agent = new Agent(1, 3, 2, (Individual[]) gen0);

        agent.run(Agent.MAXIMIZATION, selection);

        assertEquals(200.0, agent.getBest().getIndividual().getFitness(), 0.001);
        assertEquals(1, agent.getBest().getGeneration());
    }

    @Test
    void runShouldKeepInitialBestForMinimizationWhenNoImprovementOccurs() {
        /*
         * Todos os fitness pioram a cada geração — o best inicial
         * permanece inalterado.
         */
        Individual<?>[] gen0 = { new TestIndividual(10.0) };
        Individual<?>[] gen1 = { new TestIndividual(50.0) };
        Individual<?>[] gen2 = { new TestIndividual(100.0) };

        ScriptedSelection selection = new ScriptedSelection(gen1, gen2);
        Agent agent = new Agent(1, 3, 2, (Individual[]) gen0);

        agent.run(Agent.MINIMIZATION, selection);

        assertEquals(10.0, agent.getBest().getIndividual().getFitness(), 0.001);
        assertEquals(0, agent.getBest().getGeneration());
    }

    @Test
    void runShouldResetBestOnEachCall() {
        /*
         * run() cria um novo Best no início. Uma segunda chamada não deve
         * "carregar" o best da primeira.
         */
        Individual[] population = freshPopulation(10.0, 50.0, 100.0);
        RecordingSelection selection = new RecordingSelection();
        Agent agent = new Agent(population.length, 3, 2, population);

        agent.run(Agent.MAXIMIZATION, selection);
        Best firstBest = agent.getBest();

        agent.run(Agent.MAXIMIZATION, selection);
        Best secondBest = agent.getBest();

        assertNotNull(secondBest);
        assertNotSame(firstBest, secondBest);
        assertEquals(100.0, secondBest.getIndividual().getFitness(), 0.001);
    }

    // -------------------------------------------------------------------------
    // reset()
    // -------------------------------------------------------------------------

    @Test
    void resetShouldRecreatePopulationWhenSupplierWasProvided() {
        AtomicInteger counter = new AtomicInteger(0);
        Supplier<Individual> supplier = () -> new TestIndividual(counter.incrementAndGet());

        Agent agent = new Agent(supplier, 3, 5, 2);
        Individual[] before = agent.getPopulation().clone();

        agent.reset();

        Individual[] after = agent.getPopulation();
        assertEquals(3, after.length);
        assertNotSame(before[0], after[0]);
        assertNotSame(before[1], after[1]);
        assertNotSame(before[2], after[2]);
        assertNull(agent.getBest());
    }

    @Test
    void resetShouldThrowWhenSupplierWasNotProvided() {
        /*
         * Este comportamento é uma limitação da classe atual:
         * construtores que recebem um array não definem individualSupplier,
         * então reset() cai em NPE ao tentar chamar .get() no supplier nulo.
         */
        Individual[] population = freshPopulation(10.0, 20.0);
        Agent agent = new Agent(population.length, 5, 2, population);

        assertThrows(NullPointerException.class, agent::reset);
    }

    // -------------------------------------------------------------------------
    // Teste de integração leve
    // -------------------------------------------------------------------------

    @Test
    void runShouldWorkEndToEndWithRealSelectionStrategy() {
        /*
         * Smoke test: verifica que o pipeline Agent + SelectionStrategy real
         * não lança exceção e produz um best não nulo.
         */
        Individual[] population = freshPopulation(100.0, 50.0, 10.0, 70.0, 30.0);
        Agent agent = new Agent(population.length, 5, 4, population);

        agent.run(Agent.MAXIMIZATION, new RouletteSelection());

        assertNotNull(agent.getBest());
        assertNotNull(agent.getBest().getIndividual());
        assertTrue(agent.getBest().getGeneration() >= 0);
        assertTrue(agent.getBest().getGeneration() < 5);
    }

    // -------------------------------------------------------------------------
    // Constantes públicas
    // -------------------------------------------------------------------------

    @Test
    void publicConstantsShouldHaveExpectedValues() {
        assertEquals("MINIMIZATION", Agent.MINIMIZATION);
        assertEquals("MAXIMIZATION", Agent.MAXIMIZATION);
        assertEquals("TOURNAMENT", Agent.TOURNAMENT);
        assertEquals("ROULETTE", Agent.ROULETTE);
        assertEquals("RANKING", Agent.RANKING);
    }
}