/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.examples.unimodality.carrier;

/**
 * 
 * represent a product in a traveling salesman variation
 * @author MAUKOSKI; W. X.
 */
public class Product {

    private String name;
    private double price;
    private double volume;

    /***
     * Create a new instance of one product.
     * @param name The name of the product
     * @param volume The volume of the product
     * @param price  the price of the product
     */
    public Product(String name, double volume, double price) {
        this.name = name;
        this.price = price;
        this.volume = volume;
    }

    /***
     * return the name of the product 
     * @return the name of the product 
     */
    public String getName() {
        return name;
    }

    /***
     * return the price of the product 
     * @return the price of the product 
     */
    public double getPrice() {
        return price;
    }

    /***
     * return the volume of the product 
     * @return the volume of the product 
     */
    public double getVolume() {
        return volume;
    }

}
