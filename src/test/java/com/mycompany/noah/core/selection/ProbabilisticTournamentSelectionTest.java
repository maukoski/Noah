package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProbabilisticTournamentSelectionTest {

    private static final String MAX = "MAXIMIZATION";
    private static final String MIN = "MINIMIZATION";

    /**
     * Individual mínimo para testes.
     * <p>
     * Como esta classe de seleção NÃO aplica crossover, o crossover
     * aqui é apenas um stub que devolve dois filhos fictícios.
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
    void shouldRejectPBelowZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProbabilisticTournamentSelection(2, -0.1)
        );
    }

    @Test
    void shouldRejectPAboveOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProbabilisticTournamentSelection(2, 1.1)
        );
    }

    @Test
    void shouldAcceptBoundaryValuesForP() {
        assertDoesNotThrow(() -> new ProbabilisticTournamentSelection(2, 0.0));
        assertDoesNotThrow(() -> new ProbabilisticTournamentSelection(2, 1.0));
    }

    @Test
    void shouldReturnRequestedNumberOfParents() {
        Individual<?>[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 0.75);

        int[] parents = selection.selectParents(population, population.length, 100, MAX);

        assertEquals(100, parents.length);
    }

    @Test
    void shouldReturnOnlyValidPopulationIndices() {
        Individual<?>[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 0.75);

        int[] parents = selection.selectParents(population, population.length, 1000, MAX);

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
         * Portanto a população deve ficar intacta: mesmas referências,
         * mesma ordem, mesmos fitness.
         */
        Individual<?> first = new TestIndividual(100.0);
        Individual<?> second = new TestIndividual(50.0);
        Individual<?> third = new TestIndividual(10.0);

        Individual<?>[] population = { first, second, third };

        double[] fitnessBefore = new double[population.length];
        for (int i = 0; i < population.length; i++) {
            fitnessBefore[i] = population[i].getFitness();
        }

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 0.75);

        selection.selectParents(population, population.length, 500, MAX);

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
    // Testes probabilísticos
    //
    // Para popSize=3 (A melhor, B meio, C pior) e tournamentSize=2,
    // a distribuição analítica de seleção é:
    //
    //   P(A) = (1 + 4p) / 9
    //   P(B) =  3       / 9  = 1/3
    //   P(C) = (5 - 4p) / 9
    //
    // Em 10.000 seleções:
    //   p = 1.0 → A ≈ 5556, B ≈ 3333, C ≈ 1111
    //   p = 0.5 → A ≈ 3333, B ≈ 3333, C ≈ 3333   (uniforme)
    //   p = 0.0 → A ≈ 1111, B ≈ 3333, C ≈ 5556   (inverso)
    //
    // Usamos limiares frouxos para tolerar variabilidade amostral.
    // -------------------------------------------------------------------------

    @Test
    void pEqualsOneShouldBehaveLikeClassicTournamentInMaximization() {
        Individual<?>[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 1.0);

        int[] selected = selection.selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 500, "Pior deveria aparecer. counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "Melhor não deveria dominar. counts=" + counts[0]);
    }

    @Test
    void pEqualsZeroShouldInvertSelectionInMaximization() {
        Individual<?>[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 0.0);

        int[] selected = selection.selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        /*
         * Esperado: P(C) ≈ 55.6%, P(B) ≈ 33.3%, P(A) ≈ 11.1%
         * Isto é o inverso exato do torneio clássico.
         */
        assertTrue(
                counts[2] > counts[1],
                "Pior deveria superar o meio. counts=" + counts[2] + ", " + counts[1]
        );
        assertTrue(
                counts[1] > counts[0],
                "Meio deveria superar o melhor. counts=" + counts[1] + ", " + counts[0]
        );
        assertTrue(counts[0] > 500, "Melhor deveria aparecer. counts=" + counts[0]);
        assertTrue(counts[2] < 8000, "Pior não deveria dominar. counts=" + counts[2]);
    }

    @Test
    void pEqualsZeroShouldInvertSelectionInMinimization() {
        /*
         * Minimização: A é o de menor fitness (10). População ordenada
         * crescentemente. p=0 deve favorecer o PIOR (maior fitness = 100).
         */
        Individual<?>[] population = {
                new TestIndividual(10.0),
                new TestIndividual(50.0),
                new TestIndividual(100.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 0.0);

        int[] selected = selection.selectParents(population, population.length, 10_000, MIN);
        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[2] > counts[1],
                "Pior deveria superar o meio. counts=" + counts[2] + ", " + counts[1]
        );
        assertTrue(
                counts[1] > counts[0],
                "Meio deveria superar o melhor. counts=" + counts[1] + ", " + counts[0]
        );
        assertTrue(counts[0] > 500, "Melhor deveria aparecer. counts=" + counts[0]);
        assertTrue(counts[2] < 8000, "Pior não deveria dominar. counts=" + counts[2]);
    }

    @Test
    void pEqualsHalfShouldProduceUniformDistribution() {
        /*
         * Com p=0.5 a distribuição analítica para popSize=3,
         * tournamentSize=2 é uniforme (1/3 cada), independentemente
         * do fitness. Este teste verifica que a seleção NÃO enviesa
         * por fitness neste caso.
         *
         * Std. dev. ≈ sqrt(10000 * 1/3 * 2/3) ≈ 47.
         * Banda [2800, 3866] ≈ ±2.5 sigma.
         */
        Individual<?>[] population = {
                new TestIndividual(1000.0),
                new TestIndividual(50.0),
                new TestIndividual(1.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 0.5);

        int[] selected = selection.selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        for (int c : counts) {
            assertTrue(
                    c > 2800 && c < 3866,
                    "Distribuição uniforme esperada; counts=" + counts[0] + ", "
                            + counts[1] + ", " + counts[2]
            );
        }
    }

    @Test
    void middleIndividualShouldAlwaysBeSelectedAroundOneThird() {
        /*
         * Propriedade invariante: com popSize=3 e tournamentSize=2,
         * P(indivíduo do meio) = 1/3 para qualquer p em [0, 1].
         *
         * Este teste é especialmente informativo porque distingue
         * esta estratégia de um torneio clássico: se alguém substituir
         * a seleção probabilística por um torneio comum, o indivíduo
         * do meio ganharia mais ou menos que 1/3 conforme p.
         */
        Individual<?>[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        int[] countsLowP = countSelections(
                new ProbabilisticTournamentSelection(2, 0.2)
                        .selectParents(population, population.length, 10_000, MAX),
                population.length
        );

        int[] countsHighP = countSelections(
                new ProbabilisticTournamentSelection(2, 0.8)
                        .selectParents(population, population.length, 10_000, MAX),
                population.length
        );

        int expected = 10_000 / 3;
        int tolerance = 400; // ~2.5 sigma

        assertTrue(
                Math.abs(countsLowP[1] - expected) < tolerance,
                "p=0.2, meio deveria estar ~1/3. counts=" + countsLowP[1]
        );

        assertTrue(
                Math.abs(countsHighP[1] - expected) < tolerance,
                "p=0.8, meio deveria estar ~1/3. counts=" + countsHighP[1]
        );
    }

    @Test
    void pEqualsOneShouldFavorBestWithNegativeFitnessInMaximization() {
        /*
         * Fitness negativos não devem confundir a comparação de melhor/pior.
         * Melhor = menos negativo (-1), pior = mais negativo (-10).
         */
        Individual<?>[] population = {
                new TestIndividual(-1.0),
                new TestIndividual(-5.0),
                new TestIndividual(-10.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 1.0);

        int[] selected = selection.selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 500, "counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "counts=" + counts[0]);
    }

    @Test
    void pEqualsZeroShouldFavorWorstWithNegativeFitnessInMinimization() {
        /*
         * Minimização, fitness negativos ordenados crescentemente:
         * A=-10 (melhor), B=-5, C=-1 (pior).
         * Com p=0, o pior (C = -1) deve ser o mais selecionado.
         */
        Individual<?>[] population = {
                new TestIndividual(-10.0),
                new TestIndividual(-5.0),
                new TestIndividual(-1.0)
        };

        ProbabilisticTournamentSelection selection =
                new ProbabilisticTournamentSelection(2, 0.0);

        int[] selected = selection.selectParents(population, population.length, 10_000, MIN);
        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[2] > counts[1],
                "Pior deveria superar o meio. counts=" + counts[2] + ", " + counts[1]
        );
        assertTrue(
                counts[1] > counts[0],
                "Meio deveria superar o melhor. counts=" + counts[1] + ", " + counts[0]
        );
        assertTrue(counts[0] > 500, "counts=" + counts[0]);
        assertTrue(counts[2] < 8000, "counts=" + counts[2]);
    }

    @Test
    void tournamentSizeOneShouldBeUniformRegardlessOfP() {
        /*
         * Com tournamentSize=1, o laço interno nunca executa e o p
         * não é consultado. O resultado é uma seleção uniforme.
         * Testa tanto com p=0 (que tenderia a escolher o pior) quanto
         * com p=1 (que tenderia a escolher o melhor) para mostrar que
         * a uniformidade não depende de p.
         */
        Individual<?>[] population = {
                new TestIndividual(1000.0),
                new TestIndividual(50.0),
                new TestIndividual(1.0)
        };

        int[] countsP0 = countSelections(
                new ProbabilisticTournamentSelection(1, 0.0)
                        .selectParents(population, population.length, 10_000, MAX),
                population.length
        );

        int[] countsP1 = countSelections(
                new ProbabilisticTournamentSelection(1, 1.0)
                        .selectParents(population, population.length, 10_000, MAX),
                population.length
        );

        for (int i = 0; i < countsP0.length; i++) {
            assertTrue(
                    countsP0[i] > 2500 && countsP0[i] < 4200,
                    "p=0, tournamentSize=1 deveria ser uniforme. counts="
                            + countsP0[0] + ", " + countsP0[1] + ", " + countsP0[2]
            );
            assertTrue(
                    countsP1[i] > 2500 && countsP1[i] < 4200,
                    "p=1, tournamentSize=1 deveria ser uniforme. counts="
                            + countsP1[0] + ", " + countsP1[1] + ", " + countsP1[2]
            );
        }
    }
}