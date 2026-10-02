package com.mycompany.noah.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Best} class.
 *
 * <p>
 * These tests verify that a {@code Best} object correctly stores and returns
 * the best individual and the generation in which that individual was found.
 * </p>
 *
 * @author MAUKOSKI, W. X.
 */
public class BestTest {

    /**
     * Verifies that the {@link Best} object stores and returns the individual
     * assigned to it.
     */
    @Test
    void shouldStoreAndReturnIndividual() {

        Individual<Double> individual = new Individual<Double>(10.0) {

            @Override
            protected double fitnessEvaluate() {
                return 0.0;
            }

            @Override
            protected Double mutation(Double genotype) {
                return genotype;
            }

            @Override
            public Individual[] crossover(Individual parent) {
                return new Individual[]{this, parent};
            }
        };

        Best<Double> best = new Best<>();

        best.setIndividual(individual);

        assertSame(individual, best.getIndividual());
    }

    /**
     * Verifies that the {@link Best} object stores and returns the generation
     * associated with the best individual.
     */
    @Test
    void shouldStoreAndReturnGeneration() {

        Best<Double> best = new Best<>();

        best.setGeneration(42);

        assertEquals(42, best.getGeneration());
    }
}