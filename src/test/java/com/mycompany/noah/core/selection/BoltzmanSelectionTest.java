package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoltzmanSelectionTest {

    private static final String MAX = "MAXIMIZATION";
    private static final String MIN = "MINIMIZATION";

    /**
     * Individual mínimo para testes.
     * <p>
     * Como esta classe NÃO aplica crossover, o método apenas devolve
     * um stub.
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

    private static Individual<?>[] freshPopulation(double... fitnesses) {
        Individual<?>[] population = new Individual<?>[fitnesses.length];
        for (int i = 0; i < fitnesses.length; i++) {
            population[i] = new TestIndividual(fitnesses[i]);
        }
        return population;
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
        Individual<?>[] population = freshPopulation(100.0, 50.0, 10.0);

        int[] parents = new BoltzmanSelection(1000.0)
                .selectParents(population, population.length, 100, MAX);

        assertEquals(100, parents.length);
    }

    @Test
    void shouldReturnOnlyValidPopulationIndices() {
        Individual<?>[] population = freshPopulation(100.0, 50.0, 10.0);

        int[] parents = new BoltzmanSelection(1000.0)
                .selectParents(population, population.length, 1000, MAX);

        for (int parent : parents) {
            assertTrue(
                    parent >= 0 && parent < population.length,
                    "Índice selecionado fora da população: " + parent
            );
        }
    }

    @Test
    void shouldNotModifyPopulation() {
        /*
         * Esta classe devolve apenas índices e NÃO aplica crossover.
         * A população deve permanecer intacta: mesmas referências,
         * mesmos valores de fitness.
         */
        Individual<?> first = new TestIndividual(100.0);
        Individual<?> second = new TestIndividual(50.0);
        Individual<?> third = new TestIndividual(10.0);

        Individual<?>[] population = { first, second, third };
        double[] fitnessBefore = new double[population.length];
        for (int i = 0; i < population.length; i++) {
            fitnessBefore[i] = population[i].getFitness();
        }

        new BoltzmanSelection(1000.0)
                .selectParents(population, population.length, 500, MAX);

        assertSame(first, population[0]);
        assertSame(second, population[1]);
        assertSame(third, population[2]);

        double[] fitnessAfter = new double[population.length];
        for (int i = 0; i < population.length; i++) {
            fitnessAfter[i] = population[i].getFitness();
        }
        assertArrayEquals(fitnessBefore, fitnessAfter, 0.0);
    }

    // -------------------------------------------------------------------------
    // Temperatura alta x temperatura baixa
    // -------------------------------------------------------------------------

    @Test
    void highTemperatureShouldProduceNearlyUniformSelection() {
        /*
         * T0 = 1.000.000 é enorme em relação aos fitness, então os pesos
         * ficam praticamente iguais:
         *   A: exp(1000 / 1e6) = exp(0.001) ≈ 1.0010
         *   B: exp( 500 / 1e6) = exp(0.0005) ≈ 1.0005
         *   C: exp( 100 / 1e6) = exp(0.0001) ≈ 1.0001
         *
         * P(A), P(B), P(C) ≈ 1/3 cada. Distribuição quase uniforme.
         *
         * Bandas ~7σ em torno de 3333 para não quebrar por azar.
         */
        Individual<?>[] population = freshPopulation(1000.0, 500.0, 100.0);

        int[] selected = new BoltzmanSelection(1_000_000.0)
                .selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        for (int c : counts) {
            assertTrue(
                    c > 3000 && c < 3666,
                    "Temperatura alta deveria ser quase uniforme. counts="
                            + counts[0] + ", " + counts[1] + ", " + counts[2]
            );
        }
    }

    @Test
    void lowTemperatureShouldStronglyFavorBestInMaximization() {
        /*
         * T0 = 10, pop=[100, 50, 10]:
         *   A: exp(10)  ≈ 22026
         *   B: exp(5)   ≈ 148
         *   C: exp(1)   ≈ 2.72
         *   P(A) ≈ 99,3%; P(B) ≈ 0,67%; P(C) ≈ 0,01%
         */
        Individual<?>[] population = freshPopulation(100.0, 50.0, 10.0);

        int[] selected = new BoltzmanSelection(10.0)
                .selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > 9700, "counts=" + counts[0]);
        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
    }

    @Test
    void lowTemperatureShouldStronglyFavorBestInMinimization() {
        /*
         * T0 = 10, pop=[10, 50, 100] (ordenado para minimização).
         * Exponents são negativos: -10/10, -50/10, -100/10.
         *   A: exp(-1)  = 0.368
         *   B: exp(-5)  = 0.0067
         *   C: exp(-10) = 4.5e-5
         *   P(A) ≈ 98,2%
         */
        Individual<?>[] population = freshPopulation(10.0, 50.0, 100.0);

        int[] selected = new BoltzmanSelection(10.0)
                .selectParents(population, population.length, 10_000, MIN);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > 9600, "counts=" + counts[0]);
        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
    }

    // -------------------------------------------------------------------------
    // Resfriamento (cooling schedule)
    //
    // Como count é privado, verificamos o resfriamento indiretamente:
    // a mesma instância deve produzir distribuições cada vez mais
    // concentradas no melhor conforme o número de chamadas cresce.
    // -------------------------------------------------------------------------

    @Test
    void coolingScheduleShouldIncreaseSelectionPressure() {
        /*
         * População com fitness "próximos" o suficiente para que a
         * diferença entre T=1000 e T=200 seja observável sem saturar.
         *
         * Em count=0 (T = 1000):
         *   exp(1000/1000)=e^1≈2.72, exp(500/1000)=e^0.5≈1.65, exp(100/1000)=e^0.1≈1.10
         *   P(A) ≈ 0.497, P(B) ≈ 0.301, P(C) ≈ 0.202
         *
         * Em count=4 (T = 200):
         *   exp(5)≈148.4, exp(2.5)≈12.18, exp(0.5)≈1.65
         *   P(A) ≈ 0.915, P(B) ≈ 0.075, P(C) ≈ 0.010
         *
         * Esperado em 10.000 seleções:
         *   Início: A ≈ 4970
         *   Depois: A ≈ 9150
         *
         * Bandas ~5-10σ para não quebrar por azar, mas detecção clara
         * da subida de pressão.
         */
        Individual<?>[] population = freshPopulation(1000.0, 500.0, 100.0);

        // Primeira chamada: T = T0 / 1 = 1000
        BoltzmanSelection early = new BoltzmanSelection(1000.0);
        int[] earlySel = early.selectParents(population, population.length, 10_000, MAX);
        int earlyBest = countSelections(earlySel, population.length)[0];

        // Avança o contador em 4 chamadas; a chamada medida usa count=4 (T=200)
        BoltzmanSelection late = new BoltzmanSelection(1000.0);
        for (int i = 0; i < 4; i++) {
            late.selectParents(population, population.length, 1, MAX);
        }
        int[] lateSel = late.selectParents(population, population.length, 10_000, MAX);
        int lateBest = countSelections(lateSel, population.length)[0];

        assertTrue(
                earlyBest > 4500 && earlyBest < 5500,
                "Fase inicial deveria estar próxima de 5000. earlyBest=" + earlyBest
        );
        assertTrue(
                lateBest > 8800 && lateBest < 9500,
                "Fase resfriada deveria estar próxima de 9150. lateBest=" + lateBest
        );
        assertTrue(
                lateBest > earlyBest + 2000,
                "Resfriamento deveria aumentar a pressão seletiva. "
                        + "earlyBest=" + earlyBest + ", lateBest=" + lateBest
        );
    }

    // -------------------------------------------------------------------------
    // Testes de tendência com temperatura moderada
    // -------------------------------------------------------------------------

    @Test
    void shouldFavorHigherFitnessInMaximization() {
        /*
         * T0 = 100 equilibra exploração e explotação:
         *   P(A) ≈ 0.497, P(B) ≈ 0.301, P(C) ≈ 0.202
         */
        Individual<?>[] population = freshPopulation(100.0, 50.0, 10.0);

        int[] selected = new BoltzmanSelection(100.0)
                .selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 1500, "C deveria aparecer. counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "A não deveria dominar. counts=" + counts[0]);
    }

    @Test
    void shouldFavorLowerFitnessInMinimization() {
        Individual<?>[] population = freshPopulation(10.0, 50.0, 100.0);

        int[] selected = new BoltzmanSelection(100.0)
                .selectParents(population, population.length, 10_000, MIN);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 1500, "counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "counts=" + counts[0]);
    }

    // -------------------------------------------------------------------------
    // Fitness negativos
    // -------------------------------------------------------------------------

    @Test
    void shouldFavorHigherFitnessWithNegativeFitnessInMaximization() {
        /*
         * T0 = 5, pop=[-1, -5, -10]:
         *   A: exp(-1/5) = 0.819
         *   B: exp(-5/5) = 0.368
         *   C: exp(-10/5) = 0.135
         *   P(A) ≈ 0.619, P(B) ≈ 0.278, P(C) ≈ 0.102
         */
        Individual<?>[] population = freshPopulation(-1.0, -5.0, -10.0);

        int[] selected = new BoltzmanSelection(5.0)
                .selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 500, "counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "counts=" + counts[0]);
    }

    @Test
    void shouldFavorLowerFitnessWithNegativeFitnessInMinimization() {
        /*
         * T0 = 5, pop=[-10, -5, -1] ordenado crescentemente:
         *   A: exp(10/5) = exp(2)  = 7.389
         *   B: exp( 5/5) = exp(1)  = 2.718
         *   C: exp( 1/5) = exp(0.2)= 1.221
         *   P(A) ≈ 0.652, P(B) ≈ 0.240, P(C) ≈ 0.108
         */
        Individual<?>[] population = freshPopulation(-10.0, -5.0, -1.0);

        int[] selected = new BoltzmanSelection(5.0)
                .selectParents(population, population.length, 10_000, MIN);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 500, "counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "counts=" + counts[0]);
    }

    // -------------------------------------------------------------------------
    // Casos de canto
    // -------------------------------------------------------------------------

    @Test
    void shouldBeUniformWhenAllFitnessValuesAreEqual() {
        /*
         * Quando todos os fitness são iguais, todos os pesos são iguais
         * (independentemente da temperatura) e a seleção é uniforme.
         *
         * Testa com temperatura muito baixa para provar que o viés de
         * "quanto mais frio, mais ganancioso" só se aplica a fitness
         * diferentes.
         */
        Individual<?>[] population = freshPopulation(50.0, 50.0, 50.0);

        int[] selected = new BoltzmanSelection(0.001)
                .selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        for (int c : counts) {
            assertTrue(
                    c > 3000 && c < 3666,
                    "Fitness iguais deveriam ser uniformes. counts="
                            + counts[0] + ", " + counts[1] + ", " + counts[2]
            );
        }
    }

    @Test
    void shouldNotCrashWithExtremeFitnessValues() {
        /*
         * Sem o clamp do expoente em [-700, 700], fitness=1e10 com T=1
         * geraria exp(1e10) = Infinity, e as comparações seguintes
         * produziriam NaN, corrompendo a roleta.
         *
         * Com o clamp, A recebe peso exp(700) e B/C ficam com exp(1),
         * então A domina praticamente 100%.
         */
        Individual<?>[] population = freshPopulation(1e10, 1.0, 1.0);

        int[] selected = new BoltzmanSelection(1.0)
                .selectParents(population, population.length, 1000, MAX);

        for (int idx : selected) {
            assertTrue(
                    idx >= 0 && idx < population.length,
                    "Índice inválido após clamp: " + idx
            );
        }

        int[] counts = countSelections(selected, population.length);
        assertTrue(
                counts[0] > 900,
                "A deveria dominar após o clamp do expoente. counts=" + counts[0]
        );
    }
}