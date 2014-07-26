package org.cep.socket.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class MessageSocket {

	private static String url = "tcp://localhost:61616";
	//private static String url = "tcp://10.100.5.110:61616"; // Awanthika Queue

	// Name of the queue (Real time Queue)
	private static String subject = "mygpsdataout";

	private final CountDownLatch closeLatch;

	@SuppressWarnings("unused")
	private Session session;

	public MessageSocket() {
		this.closeLatch = new CountDownLatch(1);
	}

	public boolean awaitClose(int duration, TimeUnit unit)
			throws InterruptedException {
		return this.closeLatch.await(duration, unit);
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
		this.session = null;
		this.closeLatch.countDown();
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		System.out.printf("Got connect: %s%n", session);
		this.session = session;
		try {

			Future<Void> fut;
		
			int i = 0;

			// Getting JMS connection from the server
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					url);
			Connection connection = connectionFactory.createConnection();
			connection.start();

			// Creating session for sending messages
			javax.jms.Session session2 = connection.createSession(false,
					javax.jms.Session.AUTO_ACKNOWLEDGE);

			// Getting the messages from the queue
			Destination destination = session2.createQueue(subject);

			// MessageConsumer is used for receiving (consuming) messages
			MessageConsumer consumer = session2.createConsumer(destination);

			try {

				// Receive the message.
				System.out.println("The Consumer : " + consumer.toString());
				while (true) {

					Message message = consumer.receive();
					System.out.println("Received message '"
							+ ((TextMessage) message).getText() + "'");
					fut = session.getRemote().sendStringByFuture(((TextMessage) message).getText());
					fut.get(2, TimeUnit.SECONDS);
					Thread.sleep(50);
					i++;
				}

			} catch (Exception e) {
				System.out.println("Error : " + e.getMessage());
			}

			finally {
				session.close(StatusCode.NORMAL,
						"[Consumer]Closing the session with the Server!!");
				connection.close();
				session2.close();

			}

			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@OnWebSocketMessage
	public void onMessage(String msg) {
		System.out.printf("Got msg: %s%n", msg);
	}

}