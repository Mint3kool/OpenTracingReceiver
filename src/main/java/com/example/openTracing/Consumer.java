package com.example.openTracing;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
public class Consumer implements Runnable {

	private int timeout = 1000; // timeout in ms
	private String queueName = "defaultQ";

	public void setQueueName(String s) {
		queueName = s;
	}

	@Override
	public void run() {
		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create Connection
			Connection connection = factory.createConnection();

			// Start the connection
			connection.start();

			// Create Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create queue
			Destination queue = session.createQueue(queueName);

			MessageConsumer consumer = session.createConsumer(queue);

			while (true) {
				Message message = consumer.receive(timeout);

				if (message != null) {
					if (message instanceof TextMessage) {
						TextMessage textMessage = (TextMessage) message;
						String text = textMessage.getText();
						System.out.println("Consumer Received: " + text + " from: " + queueName);
					}
				} else {
					break;
				}
			}

			session.close();
			connection.close();
		} catch (Exception ex) {
			System.out.println("Exception Occured");
		}
	}
}