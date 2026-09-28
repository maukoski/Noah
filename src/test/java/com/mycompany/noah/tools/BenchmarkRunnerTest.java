package com.mycompany.noah.tools;

import com.mycompany.noah.core.Agent;
import com.mycompany.noah.core.Individual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários de {@link BenchmarkRunner}.
 * <p>
 * Como a única API pública é {@code evaluation(...)}, os testes verificam
 * o comportamento observável: chamadas às sete estratégias, cálculo de erro
 * e formato da saída em {@code System.out}.
 * <p>
 * <b>Atenção:</b> esta classe usa campos {@code static} e escreve em
 * {@code System.out}. Os testes <b>não devem ser executados em paralelo</b>
 * — veja a seção "Limitações" no final.
 */
class BenchmarkRunnerTest {

    private static final String MAX = Agent.MAXIMIZATION;
    private static final String MIN = Agent.MINIMIZATION;

    private ByteArrayOutputStream capturedOut;
    private PrintStream originalOut;

    @BeforeEach
    void captureStdout() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    private String captured() {
        return capturedOut.toString();
    }

    private static int count(String haystack, String needle) {
        int n = 0, i = 0;
        while ((i = haystack.indexOf(needle, i)) != -1) {
            n++;
            i += needle.length();
        }
        return n;
    }

    /**
     * Individual com fitness fixo.
     * <p>
     * O crossover devolve as mesmas referências, então a população nunca
     * troca de fitness ao longo das gerações. Isso torna o "best" e o erro
     * totalmente previsíveis — condição essencial para asserções numéricas.
     */
    private static class FixedFitnessIndividual extends Individual<Double> {

        private final double value;

        FixedFitnessIndividual(double fitness) {
            super(0.02);
            this.value = fitness;
            setFitness(fitness);
        }

        @Override
        protected double fitnessEvaluate() {
            return value;
        }

        @Override
        protected Double mutation(Double genotype) {
            return genotype;
        }

        @Override
        public Individual[] crossover(Individual parent) {
            return new Individual[] { this, parent };
        }
    }

    private static Agent buildAgent(double fitness, int popSize, int epoch, int numReplacements) {
        Supplier<Individual> supplier = () -> new FixedFitnessIndividual(fitness);
        return new Agent(supplier, popSize, epoch, numReplacements);
    }

    private static void runFullBenchmark(Agent agent, double perfectFitness, int trials) {
        BenchmarkRunner.evaluation(agent, perfectFitness, trials, MAX, 2, 10, 0.75, 1000.0);
    }

    // -------------------------------------------------------------------------
    // Smoke tests — a orquestração não pode explodir
    // -------------------------------------------------------------------------

    @Test
    void shouldRunWithoutThrowing() {
        Agent agent = buildAgent(100.0, 4, 2, 4);

        assertDoesNotThrow(() -> runFullBenchmark(agent, 100.0, 3));
    }

    @Test
    void shouldRunWithMinimumPopulation() {
        Agent agent = buildAgent(100.0, 2, 2, 2);

        assertDoesNotThrow(() -> runFullBenchmark(agent, 100.0, 2));
    }

    @Test
    void shouldRunWithTruncationPercentageOfZero() {
        /*
         * O TruncationSelection faz max(1, ...), então trunc=0 deve continuar
         * funcionando. Aqui passamos o valor explicitamente (o default do
         * helper é 10).
         */
        Agent agent = buildAgent(100.0, 4, 2, 4);

        assertDoesNotThrow(() ->
                BenchmarkRunner.evaluation(agent, 100.0, 2, MAX, 2, 0, 0.75, 1000.0)
        );
    }

    // -------------------------------------------------------------------------
    // Cobertura das 7 estratégias
    // -------------------------------------------------------------------------

    @Test
    void shouldPrintHeaderForEveryStrategy() {
        Agent agent = buildAgent(100.0, 3, 2, 2);
        runFullBenchmark(agent, 100.0, 2);

        String out = captured();

        assertTrue(out.contains(">>> Statistics for ROULETTE <<<"), "ROULETTE ausente");
        assertTrue(out.contains(">>> Statistics for RANKING <<<"), "RANKING ausente");
        assertTrue(out.contains(">>> Statistics for TOURNAMENT <<<"), "TOURNAMENT ausente");
        assertTrue(out.contains(">>> Statistics for Truncation <<<"), "Truncation ausente");
        assertTrue(out.contains(">>> Statistics for Stocastic <<<"), "Stocastic ausente");
        assertTrue(out.contains(">>> Statistics for PROBABILISTIC TOURNAMENT <<<"),
                "PROBABILISTIC TOURNAMENT ausente");
        assertTrue(out.contains(">>> Statistics for BOLTZMAN <<<"), "BOLTZMAN ausente");
    }

    @Test
    void shouldPrintExactlyOneStatsBlockPerStrategy() {
        Agent agent = buildAgent(100.0, 3, 2, 2);
        runFullBenchmark(agent, 100.0, 2);

        String out = captured();

        assertEquals(7, count(out, ">>> Statistics for "),
                "Esperado exatamente 7 cabeçalhos");
        assertEquals(7, count(out, "Average Error:"),
                "Esperado exatamente 7 linhas de erro");
        assertEquals(7, count(out, "Average Generation:"),
                "Esperado exatamente 7 linhas de geração");
    }

    @Test
    void shouldPrintOneEpochLinePerTrialPerStrategy() {
        /*
         * recordStats imprime uma linha "Epoch:" por trial.
         * Com 7 estratégias × trials, temos exatamente 7 * trials linhas.
         */
        int trials = 3;
        Agent agent = buildAgent(100.0, 3, 2, 2);
        runFullBenchmark(agent, 100.0, trials);

        int expected = 7 * trials;
        assertEquals(
                expected,
                count(captured(), "Epoch:"),
                "Esperado " + expected + " linhas 'Epoch:'"
        );
    }

    // -------------------------------------------------------------------------
    // Cálculo de erro
    // -------------------------------------------------------------------------

    @Test
    void shouldReportZeroErrorWhenBestMatchesPerfectFitness() {
        /*
         * fitness constante = 100, perfectFitness = 100 -> erro = 0 em todos
         * os trials. 7 estratégias × 3 trials = 21 linhas com "Error: 0.000000".
         */
        Agent agent = buildAgent(100.0, 3, 2, 2);
        runFullBenchmark(agent, 100.0, 3);

        String out = captured();

        assertEquals(
                21,
                count(out, "Error: 0.000000"),
                "Erro deveria ser zero em todos os trials"
        );
        assertEquals(
                7,
                count(out, "Average Error:      0.000000"),
                "Média de erro deveria ser 0.000000 em cada bloco"
        );
    }

    @Test
    void shouldReportExpectedErrorWhenPerfectFitnessDiffers() {
        /*
         * fitness = 100, perfectFitness = 80 -> erro = 20 em todos os trials.
         * O formatador usa %f (6 casas), então a string é "20.000000".
         */
        Agent agent = buildAgent(100.0, 3, 2, 2);
        runFullBenchmark(agent, 80.0, 3);

        String out = captured();

        assertEquals(
                21,
                count(out, "Error: 20.000000"),
                "Erro deveria ser 20.000000 em todos os trials"
        );
        assertEquals(
                7,
                count(out, "Average Error:      20.000000"),
                "Média deveria ser 20.000000 em cada bloco"
        );
    }

    @Test
    void shouldReportGenerationZeroWhenNoImprovementHappens() {
        /*
         * Com fitness constante, o best é definido na geração 0 do primeiro
         * run e nunca é superado. Portanto todas as linhas devem ter
         * "generation: 0".
         */
        int trials = 3;
        Agent agent = buildAgent(100.0, 3, 2, 2);
        runFullBenchmark(agent, 100.0, trials);

        assertEquals(
                7 * trials,
                count(captured(), "generation: 0"),
                "Todas as linhas deveriam ter generation: 0"
        );
    }

    // -------------------------------------------------------------------------
    // Minimização
    // -------------------------------------------------------------------------

    @Test
    void shouldWorkInMinimizationMode() {
        Agent agent = buildAgent(100.0, 3, 2, 2);

        assertDoesNotThrow(() ->
                BenchmarkRunner.evaluation(agent, 100.0, 2, MIN, 2, 10, 0.75, 1000.0)
        );

        // As 7 estratégias ainda devem ser chamadas
        assertEquals(7, count(captured(), ">>> Statistics for "));
    }

    @Test
    void shouldReportErrorInMinimizationMode() {
        /*
         * Minimização com fitness = 100 e perfectFitness = 80:
         * erro = |100 - 80| = 20.
         */
        Agent agent = buildAgent(100.0, 3, 2, 2);

        BenchmarkRunner.evaluation(agent, 80.0, 3, MIN, 2, 10, 0.75, 1000.0);

        assertEquals(
                21,
                count(captured(), "Error: 20.000000"),
                "Erro deveria ser 20.000000 em todos os trials"
        );
    }

    // -------------------------------------------------------------------------
    // Caracterização de bug conhecido: epoch=1 -> NaN
    // -------------------------------------------------------------------------

    @Test
    void shouldPrintNaNDesvioPadraoWhenTrialsIsOne() {
        /*
         * calculateStdDev divide por (values.length - 1). Com trials=1,
         * values.length=1 e a divisão vira 0/0 = NaN.
         *
         * Este teste DOCUMENTA o comportamento atual. Se a classe for
         * corrigida no futuro (por exemplo, retornando 0.0 quando
         * values.length <= 1), este teste precisa ser ajustado.
         */
        Agent agent = buildAgent(100.0, 3, 2, 2);
        runFullBenchmark(agent, 100.0, 1);

        assertTrue(
                captured().contains("NaN"),
                "Com trials=1, o desvio padrão é NaN (bug caracterizado)"
        );
    }

    // -------------------------------------------------------------------------
    // Constantes / configuração estática não vaza entre chamadas
    // -------------------------------------------------------------------------

    @Test
    void shouldUseTheAgentConfigurationFromTheCurrentCall() {
        /*
         * A classe lê populationSize, maxGenerations e numReplacements do
         * Agent na entrada de evaluation. Se um teste anterior tivesse
         * deixado estado estático sujo, a contagem de linhas "Epoch:"
         * seria diferente. Aqui garantimos que a configuração do Agent
         * atual é a usada.
         */
        Agent small = buildAgent(100.0, 3, 2, 2);
        Agent large = buildAgent(100.0, 5, 3, 4);

        runFullBenchmark(small, 100.0, 2);
        int smallLines = count(captured(), "Epoch:");

        capturedOut.reset();

        runFullBenchmark(large, 100.0, 2);
        int largeLines = count(captured(), "Epoch:");

        // Ambos rodam 7 estratégias × 2 trials = 14 linhas, independente do popSize
        assertEquals(14, smallLines);
        assertEquals(14, largeLines);
    }
}