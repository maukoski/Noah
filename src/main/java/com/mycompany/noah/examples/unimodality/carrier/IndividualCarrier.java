/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.examples.unimodality.Carrier;

import com.mycompany.noah.core.Individual;
import com.mycompany.noah.examples.unimodality.carrier.Product;
import java.util.Random;

/**
 *
 * @author willi
 */
public class IndividualCarrier extends Individual<int[]> {

    private Product[] products;
    private double ownVolume;
    private double maxVolume;

    public IndividualCarrier(double mutationTax, int genotypeSize, double maxVolume) {
        super(mutationTax);
        this.maxVolume = maxVolume;
        this.setGenotype(this.genotypeInitialization(genotypeSize));
        this.productsInitialization();
        this.setFitness(this.fitnessEvaluate());
    }

    public IndividualCarrier(int[] genotype, double mutationTax, double maxVolume) {
        super(genotype, mutationTax);
        this.maxVolume = maxVolume;
        this.productsInitialization();
        this.setFitness(this.fitnessEvaluate());
    }

    public int[] genotypeInitialization(int genotypeSize) {
        Random r = new Random();
        int[] result = new int[genotypeSize];
        for (int i = 0; i < genotypeSize; i++) {
            result[i] = r.nextInt(2);
        }
        return result;
    }

    @Override
    protected double fitnessEvaluate() {
        double result = 0;
        this.ownVolume = 0;  // Reiniciar o volume antes de calcular
        for (int i = 0; i < this.getGenotype().length; i++) {
            if (this.getGenotype()[i] == 1) {
                result += this.products[i].getPrice();
                this.ownVolume += this.products[i].getVolume();
            }
        }
        if (this.ownVolume > this.maxVolume) {
            result = 1;  // Definir fitness mínimo se o volume exceder o máximo
        }
        return result;
    }

    @Override
    protected int[] mutation(int[] genotype) {
        Random r = new Random();

        for (int i = 0; i < genotype.length; i++) {
            if (r.nextDouble() < this.getMutationTax()) {
                // Flip the gene: '1' becomes '0' and '0' becomes '1'
                if (genotype[i] == 1) {
                    genotype[i] = 0;
                } else {
                    genotype[i] = 1;
                }
            }
        }
        return genotype;
    }

    @Override
    public Individual[] crossover(Individual parent) {
        int[] father = this.getGenotype();
        int[] mother = (int[]) parent.getGenotype();
        IndividualCarrier[] children = new IndividualCarrier[2];

        Random r = new Random();
        int crossoverPoint = r.nextInt(father.length);

        int[] son = new int[father.length];
        int[] daughter = new int[mother.length];

        for (int i = 0; i < father.length; i++) {
            if (i < crossoverPoint) {
                son[i] = father[i];
                daughter[i] = mother[i];
            } else {
                son[i] = mother[i];
                daughter[i] = father[i];
            }
        }

        son = this.mutation(son);
        daughter = this.mutation(daughter);

        children[0] = new IndividualCarrier(son, this.getMutationTax(), this.maxVolume);
        children[1] = new IndividualCarrier(daughter, this.getMutationTax(), this.maxVolume);

        return children;
    }

    private void productsInitialization() {
        this.products = new Product[this.getGenotype().length];
        this.products[0] = new Product("Geladeira Dako", 0.751, 999.90);
        this.products[1] = new Product("Iphone 6", 0.0000899, 2911.12);
        this.products[2] = new Product("TV 55", 0.400, 4346.99);
        this.products[3] = new Product("TV 50", 0.290, 3999.90);
        this.products[4] = new Product("TV 42", 0.200, 2999.90);
        this.products[5] = new Product("Notebook Dell", 0.00350, 2499.00);
        this.products[6] = new Product("Ventilador Panasonic", 0.496, 199.90);
        this.products[7] = new Product("Microondas eletrolux", 0.0424, 308.66);
        this.products[8] = new Product("Microondas LG", 0.0544, 429.90);
        this.products[9] = new Product("Microondas Panasonic", 0.0319, 299.29);
        this.products[10] = new Product("Geladeira Brastemp", 0.0635, 849.00);
        this.products[11] = new Product("Geladeira Consul", 0.870, 1199.90);
        this.products[12] = new Product("Notebook Lenovo", 0.498, 1999.90);
        this.products[13] = new Product("Notebook Asus", 0.0527, 3999.00);
    }

    public void print() {
        System.out.print("Genotype: ");
        for (int i = 0; i < this.getGenotype().length; i++) {
            System.out.print(this.getGenotype()[i]);
        }
        System.out.println("");
        System.out.println("Valor da carga R$" + this.getFitness());
        System.out.println("Volume da carga " + this.ownVolume);

        System.out.print("Produtos da carga: ");
        boolean firstProduct = true;
        for (int i = 0; i < this.getGenotype().length; i++) {
            if (this.getGenotype()[i] == 1) {
                if (firstProduct) {
                    System.out.print(this.products[i].getName());
                    firstProduct = false;
                } else {
                    System.out.print(", " + this.products[i].getName());
                }
            }
        }
        System.out.println();
    }
    
    

}
