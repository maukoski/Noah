package com.mycompany.noah.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BestTest {

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

    @Test
    void shouldStoreAndReturnGeneration() {

        Best<Double> best = new Best<>();

        best.setGeneration(42);

        assertEquals(42, best.getGeneration());
    }
}