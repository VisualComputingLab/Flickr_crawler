/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.vcl.flickrcrawl.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 *
 * @author dimitris.samaras
 */
public class GetRequests {
    
    public static String callGET(URL url) {
        String output;
        int code = 0;
        String msg = null;

        try {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            // you need the following if you pass server credentials
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            output = Convert.convertStreamToString(httpCon.getInputStream());//convertStreamToString(httpCon.getInputStream());
            code = httpCon.getResponseCode();
            msg = httpCon.getResponseMessage();
            //output = "" + httpCon.getResponseCode() + "\n" + httpCon.getResponseMessage() + "\n" + output;

        } catch (IOException e) {
            output = "IOException during callGET: " + e;
            Logger.err("ERROR:" + output);
        }
        if ((code != 200 || code != 201) && !("OK".equals(msg))) {
            //output = "NOT OK RESPONSE";
            Logger.err("ERROR: HTTP error code : " + code);
        }
        return output;
    }
}
