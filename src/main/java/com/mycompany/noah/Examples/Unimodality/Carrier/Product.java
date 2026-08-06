/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noah.Examples.Unimodality.Carrier;

/**
 *
 * @author willi
 */
public class Product {

    private String name;
    private double price;
    private double volume;

    public Product(String name, double volume, double price) {
        this.name = name;
        this.price = price;
        this.volume = volume;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getVolume() {
        return volume;
    }

}
