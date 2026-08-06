/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.core;

/**
 *
 * @author willi
 */
public class Best<T> {
    private Individual<T> individual;
    private int generation;


    public Individual<T> getIndividual() {
        return individual;
    }

    public void setIndividual(Individual<T> individual) {
        this.individual = individual;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }
    
    
}
