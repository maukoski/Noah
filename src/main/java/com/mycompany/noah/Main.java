/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.noah;

import com.mycompany.noah.Examples.Multimodality.IndividualRastrigin;
import com.mycompany.noah.Examples.Unimodality.Sphere.IndividualSphere;
import com.mycompany.noah.core.Agent;
import com.mycompany.noah.core.selection.RouletteSelection;
import com.mycompany.noah.tools.BenchmarkRunner;

/**
 *
 * @author willi
 */
public class Main {

    public static void main(String[] args) {


        
        Agent a = new Agent<>(() -> new IndividualSphere(0.02, 12), 1000, 10000, 200);
        //Agent a  = new Agent<>(() -> new IndividualCarrier(0.02, 14,3), 1000, 5000, 200);
        //Agent a = new Agent<>(() -> new IndividualRosenbrock(0.02, 12, 2), 1000, 10000, 200);
        //Agent a = new Agent<>(() -> new IndividualShiftedRosenbrock(0.02, 12, 2), 1000, 10000, 200);
        //Agent a = new Agent<>(() -> new IndividualRastrigin(0.05,12,4), 10000, 100000, 2000);
        
        RouletteSelection r = new RouletteSelection();
        a.run(Agent.MINIMIZATION, r);
       // System.out.println("Melhor fitness: " + a.getBest().getIndividual().getFitness());


        BenchmarkRunner.evaluation(a, 0, 10, Agent.MINIMIZATION, 5, 10, 0.8, 50);
        //a.run(a.MINIMIZATION);

    }

}
