package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TournamentSelectionTest {

    private static final String MAX = "MAXIMIZATION";
    private static final String MIN = "MINIMIZATION";

    /**
     * Individual mínimo para testes.
     * <p>
     * O crossover preserva o fitness dos pais por posição, de modo que
     * a distribuição da população não muda com a substituição aplicada
     * pelo {@link TournamentSelection}. Isso isola os testes de seleção
     * da implementação real do crossover.
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

    // -------------------------------------------------------------------------
    // Testes determinísticos
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnRequestedNumberOfParents() {
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        TournamentSelection selection = new TournamentSelection(2);

        int[] parents = selection.selectParents(population, population.length, 100, MAX);

        assertEquals(100, parents.length);
    }

    @Test
    void shouldReturnRequestedNumberOfParentsWhenNumParentsIsOdd() {
        /*
         * O Javadoc recomenda numParents par, mas a implementação não
         * valida isso. O array retornado ainda deve ter o tamanho pedido;
         * apenas o último pai fica fora do laço de crossover.
         */
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        TournamentSelection selection = new TournamentSelection(2);

        int[] parents = selection.selectParents(population, population.length, 7, MAX);

        assertEquals(7, parents.length);
    }

    @Test
    void shouldReturnOnlyValidPopulationIndices() {
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        TournamentSelection selection = new TournamentSelection(2);

        int[] parents = selection.selectParents(population, population.length, 1000, MAX);

        for (int parent : parents) {
            assertTrue(
                    parent >= 0 && parent < population.length,
                    "Índice selecionado fora da população: " + parent
            );
        }
    }

    @Test
    void shouldKeepPopulationSizeAfterCrossover() {
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        int sizeBefore = population.length;

        TournamentSelection selection = new TournamentSelection(2);
        selection.selectParents(population, population.length, 100, MAX);

        assertEquals(sizeBefore, population.length);

        for (Individual<?> individual : population) {
            assertNotNull(individual, "Crossover deixou posição nula na população");
        }
    }

    // -------------------------------------------------------------------------
    // Testes probabilísticos
    //
    // TournamentSelection: cada pai é o vencedor de um torneio de
    // tournamentSize indivíduos sorteados com reposição.
    //
    // Para popSize=3 (A melhor, B meio, C pior) e tournamentSize=2:
    //   P(vencedor = A) = 1 - (2/3)^2 = 5/9  ≈ 55,6%
    //   P(vencedor = B) = 1/3               ≈ 33,3%
    //   P(vencedor = C) = 1/9               ≈ 11,1%
    //
    // Em 10.000 torneios esperamos aproximadamente:
    //   A → 5556, B → 3333, C → 1111
    //
    // Como é estocástico, usamos limiares frouxos.
    // -------------------------------------------------------------------------

    @Test
    void shouldFavorBestFitnessInMaximization() {
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        TournamentSelection selection = new TournamentSelection(2);

        int[] selected = selection.selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "Melhor deveria superar o segundo. counts=" + counts[0] + ", " + counts[1]
        );
        assertTrue(
                counts[1] > counts[2],
                "Segundo deveria superar o pior. counts=" + counts[1] + ", " + counts[2]
        );
        assertTrue(
                counts[2] > 500,
                "Pior indivíduo deveria aparecer para manter variabilidade. counts=" + counts[2]
        );
        assertTrue(
                counts[0] < 8000,
                "Melhor não deveria dominar totalmente. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorBestFitnessInMinimization() {
        /* População ordenada crescentemente (minimização). */
        Individual[] population = {
                new TestIndividual(10.0),
                new TestIndividual(50.0),
                new TestIndividual(100.0)
        };

        TournamentSelection selection = new TournamentSelection(2);

        int[] selected = selection.selectParents(population, population.length, 10_000, MIN);
        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "Melhor deveria superar o segundo. counts=" + counts[0] + ", " + counts[1]
        );
        assertTrue(
                counts[1] > counts[2],
                "Segundo deveria superar o pior. counts=" + counts[1] + ", " + counts[2]
        );
        assertTrue(
                counts[2] > 500,
                "Pior indivíduo deveria aparecer. counts=" + counts[2]
        );
        assertTrue(
                counts[0] < 8000,
                "Melhor não deveria dominar totalmente. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorBestFitnessWithNegativeFitnessInMaximization() {
        Individual[] population = {
                new TestIndividual(-1.0),
                new TestIndividual(-5.0),
                new TestIndividual(-10.0)
        };

        TournamentSelection selection = new TournamentSelection(2);

        int[] selected = selection.selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 500, "counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "counts=" + counts[0]);
    }

    @Test
    void shouldFavorBestFitnessWithNegativeFitnessInMinimization() {
        Individual[] population = {
                new TestIndividual(-10.0),
                new TestIndividual(-5.0),
                new TestIndividual(-1.0)
        };

        TournamentSelection selection = new TournamentSelection(2);

        int[] selected = selection.selectParents(population, population.length, 10_000, MIN);
        int[] counts = countSelections(selected, population.length);

        assertTrue(counts[0] > counts[1], "counts=" + counts[0] + ", " + counts[1]);
        assertTrue(counts[1] > counts[2], "counts=" + counts[1] + ", " + counts[2]);
        assertTrue(counts[2] > 500, "counts=" + counts[2]);
        assertTrue(counts[0] < 8000, "counts=" + counts[0]);
    }

    @Test
    void shouldBeUniformWhenTournamentSizeIsOne() {
        /*
         * Com tournamentSize=1, cada "torneio" tem apenas um indivíduo —
         * a seleção é uniforme e o fitness não importa.
         *
         * Esperado em 10.000 seleções: ~3333 por indivíduo.
         * Std. dev. ≈ sqrt(10000 * 1/3 * 2/3) ≈ 47.
         * Limite [2500, 4200] é folgado o suficiente para não quebrar
         * por variação aleatória, mas ainda detecta viés sistemático.
         */
        Individual[] population = {
                new TestIndividual(1000.0),
                new TestIndividual(50.0),
                new TestIndividual(1.0)
        };

        TournamentSelection selection = new TournamentSelection(1);

        int[] selected = selection.selectParents(population, population.length, 10_000, MAX);
        int[] counts = countSelections(selected, population.length);

        for (int c : counts) {
            assertTrue(
                    c > 2500 && c < 4200,
                    "Seleção uniforme esperada; counts=" + counts[0] + ", "
                            + counts[1] + ", " + counts[2]
            );
        }
    }

    @Test
    void largerTournamentShouldIncreaseSelectionPressure() {
        /*
         * Mesma população, dois tamanhos de torneio. Torneio maior deve
         * selecionar o melhor indivíduo mais vezes.
         *
         * Para popSize=5 e tournamentSize=2:
         *   P(melhor vence) = 1 - (4/5)^2 = 0.36 → ~3600/10000
         *
         * Para tournamentSize=5:
         *   P(melhor vence) = 1 - (4/5)^5 ≈ 0.672 → ~6720/10000
         *
         * Diferença esperada ~3120; exigimos pelo menos 1000 de folga
         * para não quebrar por variação aleatória.
         */
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(90.0),
                new TestIndividual(80.0),
                new TestIndividual(70.0),
                new TestIndividual(60.0)
        };

        int[] smallTournament = new TournamentSelection(2)
                .selectParents(population.clone(), population.length, 10_000, MAX);
        int[] largeTournament = new TournamentSelection(5)
                .selectParents(population.clone(), population.length, 10_000, MAX);

        int smallBest = countSelections(smallTournament, population.length)[0];
        int largeBest = countSelections(largeTournament, population.length)[0];

        assertTrue(
                largeBest > smallBest + 1000,
                "Torneio maior deveria selecionar o melhor com mais frequência. "
                        + "small=" + smallBest + ", large=" + largeBest
        );
    }
}