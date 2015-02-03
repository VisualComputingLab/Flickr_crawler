/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.dimsam.flickrcrawl.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author dimitris.samaras
 */
public class RabbitMQHandling {
    
    
    Logger logger = new Logger();
    
    public Connection connection = null;
    public Channel channel = null;
    
      public void writeToRMQ(JSONObject json, String qName) throws IOException {

        channel.basicPublish("", qName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                json.toString().getBytes("UTF-8"));
        logger.log(" [x] Sent to queue '" + json + "'");
    }

    public void openRMQ(String host, String qName) throws IOException {
        //Pass the queue name here from the RESQUEST JSON

        //Create queue, connect and write to rabbitmq
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        logger.log("connected to rabbitMQ on localhost ...");

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(qName, true, false, false, null);
        } catch (IOException ex) {
            logger.err("IOException during queue creation: " + ex);
        }
    }

    public void closeRMQ() throws IOException {

        if (connection != null) {
            logger.log("Closing rabbitmq connection and channels");
            try {
                connection.close();
                connection = null;
            } catch (IOException ex) {
                logger.err("IOException during closing rabbitmq connection and channels: " + ex);
            }
        } else {
            logger.log("Closed OK");
        }
    }
    
}
