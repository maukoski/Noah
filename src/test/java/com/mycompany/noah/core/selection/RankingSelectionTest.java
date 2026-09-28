package com.mycompany.noah.core.selection;

import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankingSelectionTest {

    private static final String MAX = "MAXIMIZATION";
    private static final String MIN = "MINIMIZATION";

    /**
     * Individual mínimo para testes.
     * <p>
     * O crossover devolve novos indivíduos com o mesmo fitness dos pais,
     * para que a distribuição de ranks da população se mantenha estável
     * ao longo do loop de seleção. Isso isola o comportamento do
     * {@link RankingSelection#selectParents} da implementação real de
     * crossover.
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
            double f1 = this.getFitness();
            double f2 = parent.getFitness();
            return new Individual[] {
                    new TestIndividual(f1),
                    new TestIndividual(f2)
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

        RankingSelection selection = new RankingSelection();

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
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        RankingSelection selection = new RankingSelection();

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
    void shouldKeepPopulationSizeAndArrayIdentity() {
        /*
         * O RankingSelection aplica crossover na população (por design),
         * então não podemos exigir que ela fique inalterada. Mas podemos
         * garantir que o array original continua sendo usado e mantém o
         * mesmo tamanho.
         */
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        int sizeBefore = population.length;

        RankingSelection selection = new RankingSelection();
        selection.selectParents(population, population.length, 100, MAX);

        assertEquals(sizeBefore, population.length);

        for (Individual<?> individual : population) {
            assertNotNull(individual);
        }
    }

    // -------------------------------------------------------------------------
    // Testes probabilísticos
    //
    // Pesos atribuídos por RankingSelection:
    //   weights[i] = popSize - i
    //
    // População com popSize = 3:
    //   weights = [3, 2, 1], total = 6
    //   P(indice 0) ≈ 50,00%
    //   P(indice 1) ≈ 33,33%
    //   P(indice 2) ≈ 16,67%
    //
    // Em 10.000 seleções esperamos aproximadamente:
    //   índice 0 -> ~5000
    //   índice 1 -> ~3333
    //   índice 2 -> ~1667
    //
    // Como a seleção é estocástica, usamos limiares frouxos.
    // -------------------------------------------------------------------------

    @Test
    void shouldFavorHigherRanksInMaximization() {
        /*
         * População ordenada decrescentemente (maximização).
         * O rank, e não o fitness absoluto, define o peso.
         */
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        RankingSelection selection = new RankingSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MAX
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "Rank 0 deveria superar rank 1. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > counts[2],
                "Rank 1 deveria superar rank 2. counts=" + counts[1] + ", " + counts[2]
        );

        assertTrue(
                counts[2] > 500,
                "Rank 2 deveria aparecer para manter variabilidade. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "Rank 0 não deveria dominar totalmente. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorHigherRanksInMinimization() {
        /*
         * População ordenada crescentemente (minimização).
         * O RankingSelection ignora o optimizationMode — apenas o rank importa.
         * Portanto o resultado esperado é o mesmo do teste de maximização.
         */
        Individual[] population = {
                new TestIndividual(10.0),
                new TestIndividual(50.0),
                new TestIndividual(100.0)
        };

        RankingSelection selection = new RankingSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MIN
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "Rank 0 deveria superar rank 1. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > counts[2],
                "Rank 1 deveria superar rank 2. counts=" + counts[1] + ", " + counts[2]
        );

        assertTrue(
                counts[2] > 500,
                "Rank 2 deveria aparecer para manter variabilidade. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "Rank 0 não deveria dominar totalmente. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorHigherRanksWithNegativeFitnessInMaximization() {
        /*
         * Fitness negativos: como o ranking só usa posição, o comportamento
         * deve ser idêntico aos testes com fitness positivos.
         */
        Individual[] population = {
                new TestIndividual(-1.0),
                new TestIndividual(-5.0),
                new TestIndividual(-10.0)
        };

        RankingSelection selection = new RankingSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MAX
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "Rank 0 deveria superar rank 1. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > counts[2],
                "Rank 1 deveria superar rank 2. counts=" + counts[1] + ", " + counts[2]
        );

        assertTrue(
                counts[2] > 500,
                "Rank 2 deveria aparecer para manter variabilidade. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "Rank 0 não deveria dominar totalmente. counts=" + counts[0]
        );
    }

    @Test
    void shouldFavorHigherRanksWithNegativeFitnessInMinimization() {
        /*
         * Mesmo cenário do teste anterior, mas com fitness negativos
         * ordenados crescentemente (minimização).
         */
        Individual[] population = {
                new TestIndividual(-10.0),
                new TestIndividual(-5.0),
                new TestIndividual(-1.0)
        };

        RankingSelection selection = new RankingSelection();

        int[] selected = selection.selectParents(
                population,
                population.length,
                10_000,
                MIN
        );

        int[] counts = countSelections(selected, population.length);

        assertTrue(
                counts[0] > counts[1],
                "Rank 0 deveria superar rank 1. counts=" + counts[0] + ", " + counts[1]
        );

        assertTrue(
                counts[1] > counts[2],
                "Rank 1 deveria superar rank 2. counts=" + counts[1] + ", " + counts[2]
        );

        assertTrue(
                counts[2] > 500,
                "Rank 2 deveria aparecer para manter variabilidade. counts=" + counts[2]
        );

        assertTrue(
                counts[0] < 8000,
                "Rank 0 não deveria dominar totalmente. counts=" + counts[0]
        );
    }

    @Test
    void shouldBehaveTheSameRegardlessOfOptimizationMode() {
        /*
         * Documenta o fato de que o parâmetro optimizationMode é ignorado
         * nesta implementação — os pesos dependem apenas do rank. Este teste
         * protege contra uma futura alteração que introduza dependência
         * silenciosa do modo de otimização.
         */
        Individual[] population = {
                new TestIndividual(100.0),
                new TestIndividual(50.0),
                new TestIndividual(10.0)
        };

        RankingSelection selection = new RankingSelection();

        int[] selectedMax = selection.selectParents(
                population.clone(),
                population.length,
                5_000,
                MAX
        );

        int[] selectedMin = selection.selectParents(
                population.clone(),
                population.length,
                5_000,
                MIN
        );

        int[] countsMax = countSelections(selectedMax, population.length);
        int[] countsMin = countSelections(selectedMin, population.length);

        /*
         * Não esperamos distribuições idênticas (é estocástico), mas
         * esperamos a mesma ordenação de ranks nas duas execuções.
         */
        assertTrue(countsMax[0] > countsMax[1]);
        assertTrue(countsMax[1] > countsMax[2]);

        assertTrue(countsMin[0] > countsMin[1]);
        assertTrue(countsMin[1] > countsMin[2]);
    }
}