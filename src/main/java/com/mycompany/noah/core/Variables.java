/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.core;

/**
 *
 * @author Maukoski, W. X.
 */

/**
 * Represents the genotype of an individual as an array of binary strings.
 * Each string in the array corresponds to one variable of the problem.
 * <p>
 * This class is used to encapsulate the genetic representation of an
 * individual, where each element of the array is a binary-encoded variable
 * that can be decoded into a real value within a specific domain.
 * </p>
 */
public class Variables {
    
    /** Array of binary strings representing the variables. */
    private String[] x;

    /**
     * Constructs a new {@code Variables} instance with the given array
     * of binary strings.
     *
     * @param x array of binary strings, one per variable
     */
    public Variables(String[] x) {
        this.x = x;
    }

    /**
     * Returns the array of binary strings representing the variables.
     *
     * @return array of binary strings
     */
    public String[] getX() {
        return x;
    }

    /**
     * Sets the array of binary strings representing the variables.
     *
     * @param x array of binary strings, one per variable
     */
    public void setX(String[] x) {
        this.x = x;
    }
 
}
