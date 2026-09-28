package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TruncationSelectionTest {

    private static final String MAX = "MAXIMIZATION";
    private static final String MIN = "MINIMIZATION";

    /**
     * Individual de teste.
     * <p>
     * Cada instância carrega um {@code id} e, quando criada por crossover,
     * os ids dos dois pais. Isso permite verificar deterministicamente que
     * somente membros da elite atuam como pais.
     */
    private static class TestIndividual extends Individual<Double> {

        private final int id;
        private final int parentAId;
        private final int parentBId;

        TestIndividual(int id, double fitness) {
            this(id, fitness, -1, -1);
        }

        private TestIndividual(int id, double fitness, int parentAId, int parentBId) {
            super(0.02);
            this.id = id;
            this.parentAId = parentAId;
            this.parentBId = parentBId;
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
            TestIndividual p = (TestIndividual) parent;
            return new Individual[] {
                    new TestIndividual(-1, this.getFitness(), this.id, p.id),
                    new TestIndividual(-1, p.getFitness(), p.id, this.id)
            };
        }

        int getId() { return id; }
        int getParentAId() { return parentAId; }
        int getParentBId() { return parentBId; }

        private void setFitnessForTest(double fitness) {
            setFitness(fitness);
        }
    }

    private static Individual<?>[] freshPopulation(int size) {
        Individual<?>[] population = new Individual<?>[size];
        for (int i = 0; i < size; i++) {
            // id coincide com o índice: simplifica a verificação de "quem é elite"
            population[i] = new TestIndividual(i, size - i);
        }
        return population;
    }

    private static int eliteCountFor(int popSize, double elitePercentage) {
        return Math.max(1, (int) (popSize * (elitePercentage / 100.0)));
    }

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectElitePercentageBelowZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TruncationSelection(-0.1)
        );
    }

    @Test
    void shouldRejectElitePercentageAboveHundred() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TruncationSelection(100.1)
        );
    }

    @Test
    void shouldAcceptBoundaryElitePercentages() {
        assertDoesNotThrow(() -> new TruncationSelection(0.0));
        assertDoesNotThrow(() -> new TruncationSelection(100.0));
    }

    // -------------------------------------------------------------------------
    // selectParents — não utilizado nesta estratégia
    // -------------------------------------------------------------------------

    @Test
    void selectParentsShouldReturnEmptyArray() {
        Individual<?>[] population = freshPopulation(5);

        int[] parents = new TruncationSelection(50.0)
                .selectParents(population, population.length, 4, MAX);

        assertNotNull(parents);
        assertEquals(0, parents.length);
    }

    // -------------------------------------------------------------------------
    // evolvePopulation — testes determinísticos
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnPopulationOfSameSize() {
        Individual<?>[] population = freshPopulation(10);

        Individual<?>[] evolved = new TruncationSelection(20.0)
                .evolvePopulation(population, 10, MAX);

        assertEquals(population.length, evolved.length);
    }

    @Test
    void shouldCopyEliteToFrontOfNewPopulation() {
        Individual<?>[] population = freshPopulation(10);
        int expectedElite = eliteCountFor(10, 20.0); // 2

        Individual<?>[] evolved = new TruncationSelection(20.0)
                .evolvePopulation(population, 10, MAX);

        for (int i = 0; i < expectedElite; i++) {
            assertSame(
                    population[i], evolved[i],
                    "Elite na posição " + i + " deveria ser a mesma referência"
            );
        }
    }

    @Test
    void shouldNotModifyOriginalPopulation() {
        Individual<?>[] population = freshPopulation(10);
        Individual<?>[] snapshot = population.clone();

        new TruncationSelection(20.0).evolvePopulation(population, 10, MAX);

        for (int i = 0; i < population.length; i++) {
            assertSame(
                    snapshot[i], population[i],
                    "População original foi modificada na posição " + i
            );
        }
    }

    @Test
    void shouldUseAtLeastOneEliteEvenWithZeroPercent() {
        /*
         * elitePercentage = 0 -> floor(10 * 0) = 0 -> max(1, 0) = 1.
         * Mesmo assim, pelo menos 1 indivíduo é preservado.
         */
        Individual<?>[] population = freshPopulation(10);

        Individual<?>[] evolved = new TruncationSelection(0.0)
                .evolvePopulation(population, 10, MAX);

        assertSame(population[0], evolved[0]);
    }

    @Test
    void shouldUseAtLeastOneEliteWhenFloorWouldBeZero() {
        /*
         * popSize = 5, elite = 10% -> floor(0.5) = 0 -> max(1, 0) = 1.
         */
        Individual<?>[] population = freshPopulation(5);

        Individual<?>[] evolved = new TruncationSelection(10.0)
                .evolvePopulation(population, 5, MAX);

        assertSame(population[0], evolved[0]);
    }

    @Test
    void shouldRespectEliteFractionTruncation() {
        /*
         * popSize = 7, elite = 40% -> floor(2.8) = 2.
         * Os dois primeiros são elite; o terceiro não.
         */
        Individual<?>[] population = freshPopulation(7);

        Individual<?>[] evolved = new TruncationSelection(40.0)
                .evolvePopulation(population, 7, MAX);

        assertSame(population[0], evolved[0]);
        assertSame(population[1], evolved[1]);
        assertNotSame(
                population[2], evolved[2],
                "O terceiro indivíduo não deveria ser elite"
        );
    }

    @Test
    void shouldMakeWholePopulationEliteWhenPercentageIs100() {
        Individual<?>[] population = freshPopulation(10);

        Individual<?>[] evolved = new TruncationSelection(100.0)
                .evolvePopulation(population, 10, MAX);

        for (int i = 0; i < population.length; i++) {
            assertSame(population[i], evolved[i]);
        }
    }

    @Test
    void shouldFillEverySlotWithoutNull() {
        Individual<?>[] population = freshPopulation(10);

        Individual<?>[] evolved = new TruncationSelection(20.0)
                .evolvePopulation(population, 10, MAX);

        for (int i = 0; i < evolved.length; i++) {
            assertNotNull(evolved[i], "Posição " + i + " está nula");
        }
    }

    @Test
    void shouldGenerateChildrenOnlyFromEliteParents() {
        /*
         * Verificação determinística: o código só chama crossover em
         * population[fatherIdx] e population[motherIdx] com
         * fatherIdx, motherIdx ∈ [0, eliteCount).
         *
         * Como os ids coincidem com os índices em freshPopulation,
         * todo filho tem parentAId e parentBId em [0, eliteCount).
         */
        Individual<?>[] population = freshPopulation(10);
        int eliteCount = eliteCountFor(10, 20.0); // 2

        Individual<?>[] evolved = new TruncationSelection(20.0)
                .evolvePopulation(population, 10, MAX);

        for (int i = eliteCount; i < evolved.length; i++) {
            TestIndividual child = (TestIndividual) evolved[i];
            assertTrue(
                    child.getParentAId() >= 0 && child.getParentAId() < eliteCount,
                    "Pai A do filho " + i + " fora da elite: " + child.getParentAId()
            );
            assertTrue(
                    child.getParentBId() >= 0 && child.getParentBId() < eliteCount,
                    "Pai B do filho " + i + " fora da elite: " + child.getParentBId()
            );
        }
    }

    @Test
    void shouldAvoidSelfFertilizationWhenEliteHasMultipleMembers() {
        /*
         * Quando eliteCount > 1, o código força fatherIdx != motherIdx.
         * Como os ids coincidem com os índices, todo filho deve ter
         * parentAId != parentBId.
         */
        for (int trial = 0; trial < 100; trial++) {
            Individual<?>[] population = freshPopulation(10);
            int eliteCount = eliteCountFor(10, 40.0); // 4

            Individual<?>[] evolved = new TruncationSelection(40.0)
                    .evolvePopulation(population, 10, MAX);

            for (int i = eliteCount; i < evolved.length; i++) {
                TestIndividual child = (TestIndividual) evolved[i];
                assertNotEquals(
                        child.getParentAId(), child.getParentBId(),
                        "Autofertilização detectada no filho " + i
                );
            }
        }
    }

    @Test
    void shouldHandleSingleIndividualPopulation() {
        /*
         * popSize = 1, elite = 50% -> floor(0.5) = 0 -> max(1, 0) = 1.
         * A nova população contém apenas o único elite copiado.
         */
        Individual<?>[] population = freshPopulation(1);

        Individual<?>[] evolved = new TruncationSelection(50.0)
                .evolvePopulation(population, 1, MAX);

        assertEquals(1, evolved.length);
        assertSame(population[0], evolved[0]);
    }

    @Test
    void shouldHandlePopulationOfTwoWithSingleElite() {
        /*
         * popSize = 2, elite = 50% -> floor(1.0) = 1.
         * eliteCount == 1, então o while de anti-autofertilização NÃO roda:
         * pai e mãe coincidem por design nesta situação.
         */
        Individual<?>[] population = freshPopulation(2);

        Individual<?>[] evolved = new TruncationSelection(50.0)
                .evolvePopulation(population, 2, MAX);

        assertEquals(2, evolved.length);
        assertSame(population[0], evolved[0]);

        TestIndividual child = (TestIndividual) evolved[1];
        assertEquals(0, child.getParentAId());
        assertEquals(0, child.getParentBId());
    }

    @Test
    void shouldPreserveEliteIndependentlyOfOptimizationMode() {
        /*
         * O método não consulta optimizationMode — a população já chega
         * pré-ordenada pelo agent. Verificamos que ambos os modos
         * produzem a mesma elite (mesmas referências nas primeiras posições).
         */
        Individual<?>[] populationMax = freshPopulation(10);
        Individual<?>[] populationMin = freshPopulation(10);
        int eliteCount = eliteCountFor(10, 30.0); // 3

        Individual<?>[] evolvedMax = new TruncationSelection(30.0)
                .evolvePopulation(populationMax, 10, MAX);
        Individual<?>[] evolvedMin = new TruncationSelection(30.0)
                .evolvePopulation(populationMin, 10, MIN);

        for (int i = 0; i < eliteCount; i++) {
            assertSame(populationMax[i], evolvedMax[i]);
            assertSame(populationMin[i], evolvedMin[i]);
        }
    }
}