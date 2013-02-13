package nz.co.fortytwo.freeboard.server.util;

import java.util.ArrayList;
import java.util.Vector;

import org.alternativevision.gpx.beans.Waypoint;

/**
 * SGImplify is a polyline simplification tool using the Douglas-Peucker algorithm.
 */
public class SGImplify {
    
    //adapted from http://geometryalgorithms.com/Archive/algorithm_0205/algorithm_0205.htm
    //original copyright message follows
    
    // Copyright 2002, softSurfer (www.softsurfer.com)
    // This code may be freely used and modified for any purpose
    // providing that this copyright notice is included with it.
    // SoftSurfer makes no warranty for this code, and cannot be held
    // liable for any real or imagined damage resulting from its use.
    // Users of this code must verify correctness for their application.
            
	//Copied from http://screamyguy.net/SGImplify/, under GPL licence as stated on the site
	//Modified for Freeboard by R T Huitema 2013
	
	/**
	 * Simplify a line of waypoints by tolerance.
	 * @param tol - higher will simplify more. 
	 * @param track
	 * @return
	 */
	public static ArrayList<Waypoint> simplifyLine2D(double tol, ArrayList<Waypoint> track) {
		Tuple2f [] tuples = toTuples(track);
		Tuple2f []  result = simplifyLine2D(tol, tuples);
		return toWaypoints(result);
	}
	
	private static ArrayList<Waypoint> toWaypoints(Tuple2f[] result) {
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		for(Tuple2f tp: result){
			waypoints.add(tp.getWaypoint());
		}
		return waypoints;
	}

	private static Tuple2f[] toTuples(ArrayList<Waypoint> track) {
		ArrayList<Tuple2f> tuples = new ArrayList<Tuple2f>();
		for(Waypoint wp: track){
			tuples.add(new Tuple2f(wp));
		}
		return tuples.toArray(new Tuple2f[0]);
	}
    /**
     * This method will reduce a 2D complex polyline.
     * @param tol the tolerance of the reduction algorithm. Higher numbers will simplify the line more.
     * @param V the array of Tuple2fs to be simplified
     * @return an array of Tuple2f representing the simplified polyline
     */
    public static Tuple2f [] simplifyLine2D(double tol, Tuple2f [] V) {
        
        int n = V.length;
        
        int i, k, m, pv;
        double tol2 = tol*tol;
        Tuple2f [] vt = new Tuple2f[n];
        int [] mk = new int[n];
        
        Vector sV = new Vector();
        
        for (int b = 0; b < n; b++){
            mk[b] = 0;
        }
        
        //STAGE 1 simple vertex reduction
        vt[0] = V[0];
        
        for (i=k=1, pv=0; i < n; i++){
            if (V[i].distanceSquared(V[pv]) < tol2)
                continue;
            vt[k++] = V[i];
            pv = i;
        }
        
        if (pv < n-1)
            vt[k++] = V[n-1];
        
        //STAGE 2 Douglas-Peucker polyline simplify
        //mark the first and last vertices
        mk[0] = mk[k-1] = 1;
        simplifyDP2D(tol, vt, 0, k-1, mk);
        
        //copy marked vertices to output
        for (i=m=0; i<k; i++) {
            if (mk[i] == 1)
                sV.add(vt[i]);
        }
        
        Tuple2f [] out = new Tuple2f[sV.size()];
        sV.copyInto(out);
        return out;
        
    }
    
    private static void simplifyDP2D(double tol, Tuple2f[] v, int j, int k, int [] mk){
        
        if (k <= j+1) return;  //nothing to simplify
        
        int maxi = j;
        double maxd2 = 0;
        double tol2 = tol*tol;
        //Seg S = new Seg(v[j], v[k]);
        
        Tuple2f u = v[k].minus(v[j]);
        double cu = u.dot(u);
        
        Tuple2f w;
        Tuple2f Pb;
        double b, cw, dv2;
        
        for (int i=j+1; i < k; i++ ){
            w = v[i].minus(v[j]);
            cw = w.dot(u);
            if (cw <= 0)
                dv2 = v[i].distanceSquared(v[j]);
            else if (cu <= cw)
                dv2 = v[i].distanceSquared(v[k]);
            else{
                b = cw/cu;
                Pb= v[j].minus(u.times(-b));
                dv2 = v[i].distanceSquared(Pb);
                
            }
            
            if (dv2 <= maxd2)
                continue;
            maxi = i;
            maxd2 = dv2;
        }
        if (maxd2 > tol2){
            mk[maxi] = 1;
            simplifyDP2D(tol,v,j,maxi,mk);
            simplifyDP2D(tol,v,maxi,k,mk);
            
        }
        return;
        
    }
    
}
