/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.vcl.flickrcrawl.util;

/**
 *
 * @author dimitris.samaras
 */
public class Logger {
    
     public static void log(String message) {
        System.out.println("FlickrCrawler:INFO: " + message);
    }

    public static void err(String message) {
        System.err.println("FlickrCrawler:ERROR:" + message);
    }
    
}
