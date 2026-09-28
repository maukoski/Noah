package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouletteSelectionTest {

    private static final String MAX = "MAXIMIZATION";
    private static final String MIN = "MINIMIZATION";

    /**
     * Individual mínimo para testes.
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

    private static int[] countSelections(int[] selected, int populationSize) {
        int[] counts = new int[populationSize];
        for (int idx : selected) {
            counts[idx]++;
        }
        return counts;
    }

    // -------------------------------------------------------------------------
    // Testes determinísticos
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnRequestedNumberOfParents() {
        Individual[] population = {
                new TestIndividual(40.0),
                new TestIndividual(30.0),
                new TestIndividual(20.0),
                new TestIndividual(10.0)
        };

        RouletteSelection selection = new RouletteSelection();

        int[] parents = selection.selectParents(
                population,
                population.length,
                100,
                MAX
        );

        assertEquals(100, parents.length);
    }

    @Test
    void shouldReturnOnlyValidPopulationIndices() {
        Individual[] population = {
                new TestIndividual(40.0),
                new TestIndividual(30.0),
                new TestIndividual(20.0),
                new TestIndividual(10.0)
        };

        RouletteSelection selection = new RouletteSelection();

        int[] parents = selection.selectParents(
                population,
                population.length,
                1000,
                MAX
        );

        for (int parent : parents) {
            assertTrue(
                    parent >= 0 && parent < population.length,
                    "Índice selecionado fora da população: " + parent
            );
        }
    }

    @Test
    void shouldNotModifyPopulation() {
        Individual first = new TestIndividual(40.0);
        Individual second = new TestIndividual(30.0);
        Individual third = new TestIndividual(20.0);
        Individual fourth = new TestIndividual(10.0);

        Individual[] population = { first, second, third, fourth };

        double[] fitnessBefore = new double[population.length];
        for (int i = 0; i < population.length; i++) {
            fitnessBefore[i] = population[i].getFitness();
        }

        RouletteSelection selection = new RouletteSelection();

        selection.selectParents(
                population,
                population.length,
                100,
                MAX
        );

        assertSame(first, population[0]);
        assertSame(second, population[1]);
        assertSame(third, population[2]);
        assertSame(fourth, population[3]);

        double[] fitnessAfter = new double[population.length];
        for (int i = 0; i < population.length; i++) {
            fitnessAfter[i] = population[i].getFitness();
        }

        assertArrayEquals(fitnessBefore, fitnessAfter, 0.0);
    }

    // -------------------------------------------------------------------------
    // Testes probabilísticos
    // -------------------------------------------------------------------------

    @Test
    void shouldFavorHigherFitnessInMaximizationWithoutLosingVariability() {
        /*
         * População ordenada decrescentemente para maximização.
         *
         * Pesos esperados:
         * A = 100
         * B = 50
         * C = 10
         * total = 160
         *
         * Probabilidades aproximadas:
         * A ≈ 62,5%
         * B ≈ 31,25%
         * C ≈ 6,25%
         */
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        RouletteSelection selection = new RouletteSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MAX
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "A deveria ser selecionado mais que B. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > counts[2],
                "B deveria ser selecionado mais que C. counts=" + counts[1] + ", " + counts[2]
        );

        assertTrue(
                counts[2] > 300,
                "C deveria ser selecionado algumas vezes para manter variabilidade. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "A não deveria dominar totalmente a seleção. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorLowerFitnessInMinimizationWithoutLosingVariability() {
        /*
         * População ordenada crescentemente para minimização.
         *
         * Pesos esperados com maxFit = 100:
         * A = 100 - 10 + 1 = 91
         * B = 100 - 50 + 1 = 51
         * C = 100 - 100 + 1 = 1
         * total = 143
         *
         * Probabilidades aproximadas:
         * A ≈ 63,6%
         * B ≈ 35,7%
         * C ≈ 0,7%
         */
        Individual[] population = {
                new TestIndividual(10.0),
                new TestIndividual(50.0),
                new TestIndividual(100.0)
        };

        RouletteSelection selection = new RouletteSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MIN
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "A deveria ser selecionado mais que B. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > counts[2],
                "B deveria ser selecionado mais que C. counts=" + counts[1] + ", " + counts[2]
        );

        assertTrue(
                counts[2] > 10,
                "C deveria aparecer algumas vezes. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "A não deveria dominar totalmente a seleção. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorHigherFitnessInMaximizationWithNegativeFitness() {
        /*
         * Maximização com fitness negativos.
         * População ordenada decrescentemente:
         *
         * A = -1
         * B = -5
         * C = -10
         *
         * Após deslocamento por +10:
         * A = 9
         * B = 5
         * C = 0
         * total = 14
         *
         * Probabilidades:
         * A ≈ 64,3%
         * B ≈ 35,7%
         * C = 0%
         */
        Individual[] population = {
                new TestIndividual(-1.0),
                new TestIndividual(-5.0),
                new TestIndividual(-10.0)
        };

        RouletteSelection selection = new RouletteSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MAX
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "A deveria ser selecionado mais que B. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > 2000,
                "B deveria ser selecionado com frequência relevante. counts=" + counts[1]
        );

        assertEquals(
                0,
                counts[2],
                "C tem peso zero e não deveria ser selecionado. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "A não deveria dominar totalmente a seleção. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorLowerFitnessInMinimizationWithNegativeFitness() {
        /*
         * Minimização com fitness negativos.
         * População ordenada crescentemente:
         *
         * A = -10
         * B = -5
         * C = -1
         *
         * maxFit = -1
         *
         * Pesos:
         * A = -1 - (-10) + 1 = 10
         * B = -1 - (-5)  + 1 = 5
         * C = -1 - (-1)  + 1 = 1
         * total = 16
         *
         * Probabilidades:
         * A ≈ 62,5%
         * B ≈ 31,25%
         * C ≈ 6,25%
         */
        Individual[] population = {
                new TestIndividual(-10.0),
                new TestIndividual(-5.0),
                new TestIndividual(-1.0)
        };

        RouletteSelection selection = new RouletteSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MIN
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "A deveria ser selecionado mais que B. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > counts[2],
                "B deveria ser selecionado mais que C. counts=" + counts[1] + ", " + counts[2]
        );

        assertTrue(
                counts[2] > 300,
                "C deveria aparecer algumas vezes. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "A não deveria dominar totalmente a seleção. counts=" + counts[0]
        );
    }
}