package com.mycompany.noah.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndividualTest {

    /**
     * Concrete implementation used only for testing the abstract Individual class.
     */
    private static class TestIndividual extends Individual<Double> {

        public TestIndividual(double mutationTax) {
            super(mutationTax);
        }

        public TestIndividual(Double genotype, double mutationTax) {
            super(genotype, mutationTax);
        }

        @Override
        protected double fitnessEvaluate() {
            return 42.0;
        }

        @Override
        protected Double mutation(Double genotype) {
            return genotype + 1.0;
        }

        @Override
        public Individual[] crossover(Individual parent) {
            return new Individual[]{
                this,
                parent
            };
        }

        /**
         * Exposes setFitness() for testing purposes.
         */
        public void setTestFitness(double fitness) {
            setFitness(fitness);
        }
    }

    @Test
    void shouldStoreGenotypeFromConstructor() {

        TestIndividual individual = new TestIndividual(10.0, 0.02);

        assertEquals(10.0, individual.getGenotype());
    }

    @Test
    void shouldSetAndReturnGenotype() {

        TestIndividual individual = new TestIndividual(0.02);

        individual.setGenotype(25.0);

        assertEquals(25.0, individual.getGenotype());
    }

    @Test
    void shouldStoreMutationTaxFromConstructor() {

        TestIndividual individual = new TestIndividual(0.02);

        assertEquals(0.02, individual.getMutationTax());
    }

    @Test
    void shouldSetAndReturnMutationTax() {

        TestIndividual individual = new TestIndividual(0.02);

        individual.setMutationTax(0.15);

        assertEquals(0.15, individual.getMutationTax());
    }

    @Test
    void shouldSetAndReturnFitness() {

        TestIndividual individual = new TestIndividual(0.02);

        individual.setTestFitness(100.0);

        assertEquals(100.0, individual.getFitness());
    }

    @Test
    void shouldCompareIndividualsByFitness() {

        TestIndividual better = new TestIndividual(0.02);
        TestIndividual worse = new TestIndividual(0.02);

        better.setTestFitness(10.0);
        worse.setTestFitness(5.0);

        assertTrue(better.compareTo(worse) > 0);
        assertTrue(worse.compareTo(better) < 0);
    }

    @Test
    void shouldReturnZeroWhenFitnessIsEqual() {

        TestIndividual first = new TestIndividual(0.02);
        TestIndividual second = new TestIndividual(0.02);

        first.setTestFitness(10.0);
        second.setTestFitness(10.0);

        assertEquals(0, first.compareTo(second));
    }
}