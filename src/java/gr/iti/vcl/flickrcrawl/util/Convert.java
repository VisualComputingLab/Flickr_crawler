/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.vcl.flickrcrawl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author dimitris.samaras
 */
public class Convert {

    public static String convertStreamToString(InputStream is) throws IOException {
        //
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        //
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public static String convertWord(String word) {
        StringBuilder sb = new StringBuilder();

        char[] charArr = word.toLowerCase().toCharArray();

        for (int i = 0; i < charArr.length; i++) {
            // Single character case
            if (charArr[i] == ',') {
                sb.append("%2C");
            } // Char to two characters
            else if (charArr[i] == ' ') {
                sb.append("+");
            }  // Base case
            else {
                sb.append(word.charAt(i));
            }
        }

        return sb.toString();
    }
}
