/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.fortytwo.freeboard.server;

/**
 *
 * @author Code copied from http://www.javapractices.com/topic/TopicAction.do?Id=62
 */

import java.util.Random;

/**
 Generate pseudo-random floating point values, with an
 approximately Gaussian (normal) distribution.

 Many physical measurements have an approximately Gaussian
 distribution; this provides a way of simulating such values.
*/
public final class RandomGaussian {

  public static void main(String... aArgs){
    RandomGaussian gaussian = new RandomGaussian();
    double MEAN = 3.0f;
    double VARIANCE = 1.0f;
    for (int idx = 1; idx <= 10; ++idx){
      log("Generated : " + gaussian.getGaussian(MEAN, VARIANCE));
    }
  }

  private Random fRandom = new Random();

  public double getGaussian(double aMean, double aVariance){
    return aMean + fRandom.nextGaussian() * aVariance;
  }

  private static void log(Object aMsg){
    System.out.println(String.valueOf(aMsg));
  }
}

