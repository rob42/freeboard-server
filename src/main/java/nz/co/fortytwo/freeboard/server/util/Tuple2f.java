
package nz.co.fortytwo.freeboard.server.util;

import org.alternativevision.gpx.beans.Waypoint;


/*
 * Tuple2f.java
 *
 * Created on December 30, 2007, 8:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 * 
 * Copied from http://screamyguy.net/Tuple/, under GPL licence as stated on the site
 * Modified to handle GPX by R T Huitema 2013 for Freeboard
 */

/**
 *
 * @author Matt
 */
public class Tuple2f{
    
    public double x=0,y=0;
	private Waypoint waypoint;
    
    /**
     * Creates a new instance of Tuple2f
     * @param x 
     * @param y 
     */
    public Tuple2f(double x, double y) {
        this.x = x;
        this.y = y;          
        
    }
    
    public Tuple2f(Waypoint tp) {
        this.x = tp.getLongitude();
        this.y = tp.getLatitude(); 
        this.setWaypoint(tp);
        
    }
    /**
     * Will parse the format used in the toString() method
     * @param token string in the format from the toString() method. Specifically, "x:y"
     */
  public Tuple2f(String token){

    String [] coords = token.split(":");
    this.x = new Float(coords[0]).floatValue();
    this.y = new Float(coords[1]).floatValue(); 
  }  
    
    
    public String toString(){
        return x + ":" + y;
    }
    
    /**
     * Finds the squared distance between two Tuples. This is useful when comparing
     * distances because it avoids a square root.
     * @param other 
     * @return squared distance
     */
    public double distanceSquared(Tuple2f other){
        return (x-other.x)*(x-other.x) + (y-other.y)*(y-other.y);        
    }

    
    public double length(){
        return (double)Math.sqrt(x*x + y*y);        
    }    
    
    /**
     * Subtract two tuples and return the value in a new Tuple
     * @param a 

     */
    public Tuple2f minus( Tuple2f a){
    return new Tuple2f(x-a.x, y-a.y);
  }
    /**
     * Add two tuples and return the value in a new Tuple
     * @param a 

     */
    public Tuple2f plus( Tuple2f a){
    return new Tuple2f(x+a.x, y+a.y);
  }  
    /**
     * In place divide
     * @param a divisor 

     */
    public void divideEquals( float a){
    x /= a;
    y /=a;
  }  
    
    public void plusEquals( Tuple2f a){
    x += a.x;
    y += a.y;
  }  
    
    
    /**
     * Returns the dot product of two Tuples.  Generally, this can b interpreted as the
     * angle between them.
     * @param a compare to this Tuple
     * @return the dot product
     */
  public double dot( Tuple2f a){
    return (x*a.x) + (y*a.y);  
  }
  
     public Tuple2f times(double a){
    return new  Tuple2f(x*a,y*a);
    
  }

	public Waypoint getWaypoint() {
		return waypoint;
	}

	public void setWaypoint(Waypoint waypoint) {
		this.waypoint = waypoint;
	}

	
  
    
}
