/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.vcl.flickrcrawl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.UUID;

/**
 *
 * @author dimitris.samaras
 */
public class DownloadFromUrl {

    public static String saveImage(String imageUrl, String imageFolder, String filename) {
        String destinationFile = null;
        InputStream is = null;
        OutputStream os = null;

        try {
            URL url = new URL(imageUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY); //using proxy may increase latency

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(false);
            connection.addRequestProperty("User-Agent", "Mozilla");
            is = connection.getInputStream();

            int lenunit = 2048;

            byte[] b = new byte[lenunit];
            int length;

            if ((length = is.read(b)) != -1) {
                //destinationFile = imageFolder + "/" + UUID.randomUUID() + ".jpg";
                //randomUUID created really peculiar names for my images... use filename variable instead!
                destinationFile = imageFolder + "/" + filename + ".jpg";
                os = new FileOutputStream(destinationFile);
                os.write(b, 0, length);

                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
            }

            System.out.println("Downloaded file: " + destinationFile + " from: " + imageUrl);
        } catch (IOException e) {
            System.err.println("IOException during downloading file: " + e);
            if (destinationFile != null) {
                File f = new File(destinationFile);
                if (f.exists()) {
                    f.delete();
                }
            }

            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
            }
        }
        //System.out.println("imageFolder OK");
        return destinationFile;
    }
}
