package org.cep.socket.server;
import java.net.URI;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
 
/**
 * Example of a simple Echo Client.
 */
public class SocketMessagePush {
 
    public static void main(String[] args) {
        
        String destUri = "ws://localhost:9763/CEP_DeviceTracking_Sample/server.jag";
        
      WebSocketClient client = new WebSocketClient();
        MessageSocket socket = new MessageSocket();
        try {
            client.start();
        
            URI echoUri = new URI(destUri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, echoUri, request);
            System.out.println("Connecting to :" +echoUri);
            //socket.awaitClose(60, TimeUnit.SECONDS); // Enable the Latch if need to terminate the client connection
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
               // client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}