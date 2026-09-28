package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StochasticUniversalSamplingSelectionTest {

    private static final String MAX = "MAXIMIZATION";
    private static final String MIN = "MINIMIZATION";

    /**
     * Individual mínimo para testes.
     * <p>
     * O crossover devolve novos indivíduos preservando o fitness dos pais
     * por posição, de modo que a distribuição de fitness da população não
     * muda após o crossover aplicado pelo SUS.
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
            return new Individual[] {
                    new TestIndividual(this.getFitness()),
                    new TestIndividual(parent.getFitness())
            };
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

    private static Individual[] freshPopulation(double... fitnesses) {
        Individual[] population = new Individual[fitnesses.length];
        for (int i = 0; i < fitnesses.length; i++) {
            population[i] = new TestIndividual(fitnesses[i]);
        }
        return population;
    }

    // -------------------------------------------------------------------------
    // Testes determinísticos
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnRequestedNumberOfParents() {
        Individual[] population = freshPopulation(100.0, 50.0, 10.0);

        int[] parents = new StochasticUniversalSamplingSelection()
                .selectParents(population, population.length, 100, MAX);

        assertEquals(100, parents.length);
    }

    @Test
    void shouldReturnOnlyValidPopulationIndices() {
        Individual[] population = freshPopulation(100.0, 50.0, 10.0);

        int[] parents = new StochasticUniversalSamplingSelection()
                .selectParents(population, population.length, 1000, MAX);

        for (int parent : parents) {
            assertTrue(
                    parent >= 0 && parent < population.length,
                    "Índice selecionado fora da população: " + parent
            );
        }
    }

    @Test
    void shouldKeepPopulationSizeAfterCrossover() {
        Individual[] population = freshPopulation(100.0, 50.0, 10.0);
        int sizeBefore = population.length;

        new StochasticUniversalSamplingSelection()
                .selectParents(population, population.length, 100, MAX);

        assertEquals(sizeBefore, population.length);

        for (Individual<?> individual : population) {
            assertNotNull(individual, "Crossover deixou posição nula na população");
        }
    }

    // -------------------------------------------------------------------------
    // Propriedade característica do SUS — baixa variância
    //
    // SUS garante que, em uma única chamada com N pais, cada indivíduo i
    // é selecionado floor(N·pᵢ) ou ceil(N·pᵢ) vezes.
    //
    // Quando N·pᵢ é inteiro para todo i, a contagem é EXATA — independente
    // do ponto de partida aleatório. Isso distingue o SUS de uma roleta,
    // que daria contagens binomiais ao redor da média.
    // -------------------------------------------------------------------------

    @Test
    void susShouldProduceExactCountsWhenExpectedCountsAreIntegers() {
        /*
         * Pesos: A=6, B=3, C=1, total=10.
         * N = 10 pais -> A=6, B=3, C=1 exatos.
         *
         * Repetimos várias vezes com populações novas porque o start é
         * aleatório: o SUS DEVE produzir a mesma contagem em todas as
         * execuções. Se alguém substituir por roleta, este teste quebra.
         */
        for (int trial = 0; trial < 200; trial++) {
            Individual[] population = freshPopulation(6.0, 3.0, 1.0);

            int[] selected = new StochasticUniversalSamplingSelection()
                    .selectParents(population, population.length, 10, MAX);

            int[] counts = countSelections(selected, population.length);

            assertEquals(6, counts[0], "A deveria ser selecionado exatamente 6 vezes");
            assertEquals(3, counts[1], "B deveria ser selecionado exatamente 3 vezes");
            assertEquals(1, counts[2], "C deveria ser selecionado exatamente 1 vez");
        }
    }

    @Test
    void susShouldProduceExactCountsWithLargerIntegerExpectations() {
        /*
         * Pesos: A=50, B=30, C=20, total=100.
         * N = 100 pais -> A=50, B=30, C=20 exatos.
         */
        for (int trial = 0; trial < 50; trial++) {
            Individual[] population = freshPopulation(50.0, 30.0, 20.0);

            int[] selected = new StochasticUniversalSamplingSelection()
                    .selectParents(population, population.length, 100, MAX);

            int[] counts = countSelections(selected, population.length);

            assertEquals(50, counts[0]);
            assertEquals(30, counts[1]);
            assertEquals(20, counts[2]);
        }
    }

    @Test
    void susShouldConfineCountsToFloorOrCeilWhenExpectationsAreFractional() {
        /*
         * Pesos: A=1, B=1, C=1 (uniforme), total=3.
         * N = 10 pais -> N·pᵢ = 10/3 ≈ 3,33.
         *
         * SUS garante count_i ∈ {3, 4}. A soma dos counts é 10.
         */
        for (int trial = 0; trial < 200; trial++) {
            Individual[] population = freshPopulation(1.0, 1.0, 1.0);

            int[] selected = new StochasticUniversalSamplingSelection()
                    .selectParents(population, population.length, 10, MAX);

            int[] counts = countSelections(selected, population.length);

            int sum = 0;
            for (int c : counts) {
                assertTrue(
                        c == 3 || c == 4,
                        "Cada count deveria ser floor(10/3)=3 ou ceil(10/3)=4. counts="
                                + counts[0] + ", " + counts[1] + ", " + counts[2]
                );
                sum += c;
            }
            assertEquals(10, sum);
        }
    }

    @Test
    void susShouldConfineCountsToFloorOrCeilWithNonUniformWeights() {
        /*
         * Pesos: A=6, B=3, C=1, total=10.
         * N = 11 pais -> N·pᵢ = 6,6 / 3,3 / 1,1.
         *
         * Cada count_i deve estar em {floor, ceil}:
         *   A ∈ {6, 7}
         *   B ∈ {3, 4}
         *   C ∈ {1, 2}
         */
        for (int trial = 0; trial < 200; trial++) {
            Individual[] population = freshPopulation(6.0, 3.0, 1.0);

            int[] selected = new StochasticUniversalSamplingSelection()
                    .selectParents(population, population.length, 11, MAX);

            int[] counts = countSelections(selected, population.length);

            assertTrue(counts[0] == 6 || counts[0] == 7, "A count=" + counts[0]);
            assertTrue(counts[1] == 3 || counts[1] == 4, "B count=" + counts[1]);
            assertTrue(counts[2] == 1 || counts[2] == 2, "C count=" + counts[2]);
            assertEquals(11, counts[0] + counts[1] + counts[2]);
        }
    }

    // -------------------------------------------------------------------------
    // Testes probabilísticos / de tendência
    // -------------------------------------------------------------------------

    @Test
    void shouldFavorHigherFitnessInMaximization() {
        Individual[] population = freshPopulation(100.0, 50.0, 10.0);

        int[] selected = new StochasticUniversalSamplingSelection()
                .selectParents(population, population.length, 10_000, MAX);

        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 300, "C deveria aparecer. counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "A não deveria dominar. counts=" + counts[0]);
    }

    @Test
    void shouldFavorLowerFitnessInMinimization() {
        Individual[] population = freshPopulation(10.0, 50.0, 100.0);

        int[] selected = new StochasticUniversalSamplingSelection()
                .selectParents(population, population.length, 10_000, MIN);

        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 30, "counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "counts=" + counts[0]);
    }

    // -------------------------------------------------------------------------
    // Fitness negativos
    // -------------------------------------------------------------------------

    @Test
    void shouldFavorHigherFitnessInMaximizationWithNegativeFitness() {
        /*
         * Pesos após shift: A=9, B=5, C=0, total=14.
         * C tem peso zero e nunca deve ser selecionado.
         *
         * N=14 -> contagem exata: A=9, B=5, C=0.
         */
        for (int trial = 0; trial < 100; trial++) {
            Individual[] population = freshPopulation(-1.0, -5.0, -10.0);

            int[] selected = new StochasticUniversalSamplingSelection()
                    .selectParents(population, population.length, 14, MAX);

            int[] counts = countSelections(selected, population.length);

            assertEquals(9, counts[0], "A count=" + counts[0]);
            assertEquals(5, counts[1], "B count=" + counts[1]);
            assertEquals(0, counts[2], "C tem peso zero e não deveria ser selecionado");
        }
    }

    @Test
    void shouldFavorLowerFitnessInMinimizationWithNegativeFitness() {
        /*
         * Minimização com fitness negativos, ordenado crescentemente:
         * A=-10 (melhor), B=-5, C=-1 (pior). maxFit=-1.
         *
         * Pesos: A=10, B=6, C=1, total=17.
         * N=17 -> contagem exata: A=10, B=6, C=1.
         */
        for (int trial = 0; trial < 100; trial++) {
            Individual[] population = freshPopulation(-10.0, -5.0, -1.0);

            int[] selected = new StochasticUniversalSamplingSelection()
                    .selectParents(population, population.length, 16, MIN);

            int[] counts = countSelections(selected, population.length);

            assertEquals(10, counts[0], "A count=" + counts[0]);
            assertEquals(5, counts[1], "B count=" + counts[1]);
            assertEquals(1, counts[2], "C count=" + counts[2]);
        }
    }

    @Test
    void shouldFavorLowerFitnessInMinimizationWithPositiveFitness() {
        /*
         * Pesos: A=91, B=51, C=1, total=143 (maxFit=100).
         * N=143 -> contagem exata: A=91, B=51, C=1.
         */
        for (int trial = 0; trial < 50; trial++) {
            Individual[] population = freshPopulation(10.0, 50.0, 100.0);

            int[] selected = new StochasticUniversalSamplingSelection()
                    .selectParents(population, population.length, 143, MIN);

            int[] counts = countSelections(selected, population.length);

            assertEquals(91, counts[0], "A count=" + counts[0]);
            assertEquals(51, counts[1], "B count=" + counts[1]);
            assertEquals(1, counts[2], "C count=" + counts[2]);
        }
    }
}