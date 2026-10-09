# Noah

**An open-source framework for evolutionary and genetic programming**

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.22314944.svg)](https://doi.org/10.5281/zenodo.22314944)
[![Java](https://img.shields.io/badge/Java-26%2B-orange)](https://www.oracle.com/java/)

Noah is an open-source framework for **Genetic Programming (GP)** designed to provide a didactic and extensible evolutionary infrastructure while allowing complete freedom in the representation of genotypes.

The framework implements the general mechanisms required to execute an evolutionary process, allowing researchers and developers to focus on the aspects that are specific to their problem: the representation of individuals, fitness evaluation, mutation, and crossover.

**Version 1.0.0.1** is the first official release of Noah and corresponds to the software version associated with the initial research work describing the framework.

---

## Overview

Implementing a Genetic Programming system from scratch requires several components that are common across evolutionary approaches, including population management, selection, evolutionary iterations, reproduction, fitness tracking, and experimental evaluation.

Noah provides these mechanisms as reusable components.

A user implementing a problem with Noah is primarily responsible for defining:

- The genotype representation
- The fitness evaluation
- The mutation operator
- The crossover operator

The framework does **not** impose a predefined genotype representation. Through the generic `Individual<T>` abstraction, `T` can represent any Java type appropriate for the problem.

For example, an individual may use:

- `double[]`
- `int[]`
- `String`
- Trees
- Graphs
- Custom Java classes
- Other user-defined data structures

This separation allows the evolutionary infrastructure to remain independent of the representation of the problem being studied.

---

## Main Features

### Flexible Genotype Representation

Noah uses Java generics to provide freedom in the representation of individuals:

```java
Individual<T>
```

The generic parameter `T` represents the genotype selected by the user.

The framework therefore does not require a specific representation such as vectors, trees, or strings.

### Extensible Individuals

Problem-specific individuals are implemented by extending `Individual<T>`.

The user defines the three main genetic programming components:

1. **Fitness evaluation**
2. **Mutation**
3. **Crossover**

Noah provides the evolutionary infrastructure around these components.

### Multiple Selection Strategies

Noah 1.0.0.1 provides seven selection strategies:

1. Roulette Wheel Selection
2. Ranking Selection
3. Tournament Selection
4. Truncation Selection
5. Stochastic Universal Sampling
6. Probabilistic Tournament Selection
7. Boltzmann Selection

The architecture allows additional selection strategies to be implemented in future versions.

### Selection Strategy Benchmarking

Noah includes a benchmarking component for experimentally evaluating the available selection strategies.

The benchmark performs multiple independent runs and records statistical information including:

- Absolute fitness error
- Generation in which the best solution was found
- Mean error
- Sample standard deviation of error
- Mean generation of the best solution
- Sample standard deviation of generation

The selection strategies have been evaluated using benchmark functions and problems such as Sphere, Rosenbrock, Shifted Rosenbrock, Rastrigin, and Carrier.

### Fitness History

Noah can record fitness throughout the evolutionary process, allowing the progress of an evolutionary run to be analyzed across generations.

---

## Architecture

Noah separates the problem-specific definition of an individual from the general evolutionary process.

```text
                 +----------------------+
                 |       Agent<T>       |
                 |  Evolutionary Process|
                 +----------+-----------+
                            |
                            v
                 +----------------------+
                 | SelectionStrategy    |
                 +----------+-----------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
        Selection 1    Selection 2    Selection N
                            |
                            v
                 +----------------------+
                 |    Individual<T>     |
                 +----------------------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
         Fitness        Mutation       Crossover
```

### `Individual<T>`

`Individual<T>` represents an individual in the evolutionary population.

Its generic parameter `T` defines the genotype representation.

The individual provides the problem-specific implementation of:

- Fitness evaluation
- Mutation
- Crossover

It also stores information such as fitness and mutation rate.

### `Agent<T>`

`Agent<T>` manages the evolutionary process and population.

Its responsibilities include:

- Population initialization
- Evolutionary generations
- Population management
- Best-individual tracking
- Fitness history
- Selection strategy execution
- Optimization mode
- Evolution control

### `SelectionStrategy`

`SelectionStrategy` defines the interface for parent-selection mechanisms.

This abstraction allows selection strategies to be implemented independently from the main evolutionary process.

---

## Getting Started

Noah 1.0.0.1 is currently distributed as a **NetBeans project**.

### Requirements

- Java 26 or later
- NetBeans

Clone the repository:

```bash
git clone https://github.com/maukoski/Noah.git
```

Open the project in NetBeans and build/run the project from the IDE.

> A more convenient packaging and installation process is planned for future versions.

---

## Implementing an Individual

A problem-specific individual can be created by extending `Individual<T>`.

For example:

```java
public class MyIndividual extends Individual<double[]> {

    public MyIndividual(double[] genotype) {
        super(genotype);
    }

    @Override
    protected double fitnessEvaluate() {
        // Implement the fitness function
    }

    @Override
    protected double[] mutation(double[] genotype) {
        // Implement the mutation operator
    }

    @Override
    public Individual[] crossover(Individual parent) {
        // Implement the crossover operator
    }
}
```

The important aspect of this design is that Noah does not determine what the genotype means.

The same framework can therefore accommodate different representations:

```java
Individual<double[]>
```

```java
Individual<String>
```

```java
Individual<MyTree>
```

depending on the problem being investigated.

---

## Running an Evolutionary Process

Once an individual has been implemented, it can be used with `Agent`.

A simplified example is:

```java
Agent<MyIndividual> agent = new Agent<>(
    MyIndividual::new,
    1000,
    10000,
    200
);

SelectionStrategy selection = new RouletteSelection();

agent.run(
    Agent.MINIMIZATION,
    selection
);
```

The evolutionary process is managed by `Agent`, while the problem-specific genetic operators remain defined by the individual.

---

## Selection Strategies

The following selection strategies are currently implemented in Noah 1.0.0.1:

| Selection Strategy | General Principle |
|---|---|
| Roulette Wheel Selection | Probability-based selection according to fitness |
| Ranking Selection | Selection based on the relative ranking of individuals |
| Tournament Selection | Selection through competition among sampled individuals |
| Truncation Selection | Selection from an elite fraction of the population |
| Stochastic Universal Sampling | Systematic sampling based on fitness |
| Probabilistic Tournament Selection | Tournament selection with probabilistic outcomes |
| Boltzmann Selection | Selection based on a temperature-controlled fitness distribution |

The selection strategy architecture is designed to accommodate additional methods as the framework develops.

---

## Benchmarking

Noah provides an internal benchmarking component for comparing the behavior of its selection strategies.

Multiple independent evolutionary runs can be performed under the same experimental configuration. The benchmark records performance statistics that can be used to compare selection mechanisms.

The current benchmarking process evaluates metrics including:

- Absolute error relative to a known optimum
- Generation at which the best solution was obtained
- Average error across independent runs
- Sample standard deviation of error
- Average generation of the best solution
- Sample standard deviation of generation

This functionality is intended to support controlled experimental studies of selection mechanisms in evolutionary search.

---

## Benchmark Problems

The current project includes official examples based on benchmark functions and problems including:

- **Sphere**
- **Rosenbrock**
- **Shifted Rosenbrock**
- **Rastrigin**
- **Carrier**

These examples serve both as demonstrations of Noah and as experimental problems for evaluating selection strategies.

---

## Example Workflow

A typical experiment with Noah follows this process:

```text
1. Define the genotype
        |
        v
2. Implement Individual<T>
        |
        +--> Fitness evaluation
        |
        +--> Mutation
        |
        +--> Crossover
        |
        v
3. Create the population
        |
        v
4. Select a SelectionStrategy
        |
        v
5. Run the evolutionary process
        |
        v
6. Analyze fitness history
        |
        v
7. Benchmark and compare strategies
```

This workflow allows the representation and genetic operators to change while keeping the evolutionary infrastructure consistent across experiments.

---

## Design Philosophy

Noah follows three central design principles.

### 1. Freedom of Representation

The framework should not force researchers to adopt a particular genotype representation.

The representation is part of the problem being investigated and should remain under the control of the researcher or developer.

### 2. Extensibility

Noah provides the common evolutionary infrastructure while allowing new individuals, genetic operators, and selection strategies to be introduced without redesigning the entire framework.

### 3. Didactic and Experimental Use

Noah is intended to make the implementation and study of evolutionary and genetic programming algorithms accessible while also providing mechanisms for controlled experimentation.

The benchmarking functionality is particularly intended to facilitate experimental comparisons between selection strategies.

---

## Documentation

The Noah source code is documented following the **JavaDoc standard**.

The documentation is intended to support both developers implementing new evolutionary problems and researchers using Noah for experimentation.

Online API documentation and additional documentation resources are planned for future releases.

---

## Project Status

**Current version:** `1.0.0.1`

**Status:** Initial official release

Noah 1.0.0.1 provides the core Genetic Programming framework, seven selection strategies, benchmarking functionality, fitness-history tracking, and official benchmark examples.

The current release is distributed as a NetBeans project.

---

## Roadmap

Future development may include:

- Additional selection strategies
- Expanded evolutionary algorithm capabilities
- Parallel evolutionary computation
- GPU acceleration using CUDA
- Improved project packaging and installation
- Online API documentation
- Additional benchmark problems
- Expanded experimental and statistical tools

CUDA/GPU acceleration is part of the long-term roadmap and is **not implemented in version 1.0.0.1**.

---

## Research and Publication

Noah was developed as an open-source research framework for evolutionary and genetic programming.

The manuscript describing the Noah framework is currently **under evaluation**.

Version `1.0.0.1` represents the initial software release associated with this research work.

Further research is planned using Noah in practical problems and through comparisons with state-of-the-art evolutionary computation tools.

---

## Citation

If you use Noah in academic research, please cite the specific software version used in your work.

### Noah 1.0.0.1

**MAUKOSKI, W. X.** (2026). *Noah 1.0.0.1: An Open-Source Framework for Evolutionary and Genetic Programming*. Zenodo.

**DOI:** https://doi.org/10.5281/zenodo.22314944

The DOI above identifies the specific Noah 1.0.0.1 release archived in Zenodo.

For reproducible research, cite the specific version of Noah used to obtain your results rather than referring only to the repository.

---

## License

Noah is released under the Apache License 2.0.

---

## Author

**MAUKOSKI, W. X.**

---

## Repository

https://github.com/maukoski/Noah
