package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SelectionStrategyTest {

    /**
     * Minimal concrete Individual used exclusively for testing.
     */
    private static class TestIndividual extends Individual<String> {

        public TestIndividual(String genotype) {
            super(genotype, 0.02);
        }

        @Override
        protected double fitnessEvaluate() {
            return 0.0;
        }

        @Override
        protected String mutation(String genotype) {
            return genotype;
        }

        @Override
        public Individual[] crossover(Individual parent) {

            TestIndividual other = (TestIndividual) parent;

            return new Individual[]{
                new TestIndividual(
                        this.getGenotype() + "-" + other.getGenotype() + "-child1"
                ),
                new TestIndividual(
                        this.getGenotype() + "-" + other.getGenotype() + "-child2"
                )
            };
        }
    }

    /**
     * Test implementation of SelectionStrategy.
     * It records the parameters received and always selects
     * the same two individuals.
     */
    private static class TestSelectionStrategy implements SelectionStrategy {

        int receivedPopulationSize;
        int receivedNumParents;
        String receivedOptimizationMode;

        @Override
        public int[] selectParents(
                Individual<?>[] population,
                int populationSize,
                int numParents,
                String optimizationMode) {

            receivedPopulationSize = populationSize;
            receivedNumParents = numParents;
            receivedOptimizationMode = optimizationMode;

            return new int[]{0, 1};
        }
    }

    @Test
    void shouldPassCorrectArgumentsToSelectParents() {

        Individual<?>[] population = {
            new TestIndividual("A"),
            new TestIndividual("B"),
            new TestIndividual("C"),
            new TestIndividual("D")
        };

        TestSelectionStrategy strategy = new TestSelectionStrategy();

        strategy.evolvePopulation(
                population,
                2,
                "MINIMIZATION"
        );

        assertEquals(4, strategy.receivedPopulationSize);
        assertEquals(2, strategy.receivedNumParents);
        assertEquals("MINIMIZATION", strategy.receivedOptimizationMode);
    }

    @Test
    void shouldReplaceSelectedParentsWithCrossoverChildren() {

        TestIndividual first = new TestIndividual("A");
        TestIndividual second = new TestIndividual("B");
        TestIndividual third = new TestIndividual("C");

        Individual<?>[] population = {
            first,
            second,
            third
        };

        TestSelectionStrategy strategy = new TestSelectionStrategy();

        Individual<?>[] evolvedPopulation =
                strategy.evolvePopulation(
                        population,
                        2,
                        "MINIMIZATION"
                );

        assertEquals(
                "A-B-child1",
                evolvedPopulation[0].getGenotype()
        );

        assertEquals(
                "A-B-child2",
                evolvedPopulation[1].getGenotype()
        );
    }

    @Test
    void shouldKeepUnselectedIndividualsUnchanged() {

        TestIndividual third = new TestIndividual("C");

        Individual<?>[] population = {
            new TestIndividual("A"),
            new TestIndividual("B"),
            third
        };

        TestSelectionStrategy strategy = new TestSelectionStrategy();

        Individual<?>[] evolvedPopulation =
                strategy.evolvePopulation(
                        population,
                        2,
                        "MINIMIZATION"
                );

        assertSame(third, evolvedPopulation[2]);
        assertEquals("C", evolvedPopulation[2].getGenotype());
    }

    @Test
    void shouldReturnTheSamePopulationArray() {

        Individual<?>[] population = {
            new TestIndividual("A"),
            new TestIndividual("B")
        };

        TestSelectionStrategy strategy = new TestSelectionStrategy();

        Individual<?>[] evolvedPopulation =
                strategy.evolvePopulation(
                        population,
                        2,
                        "MAXIMIZATION"
                );

        assertSame(population, evolvedPopulation);
    }
}