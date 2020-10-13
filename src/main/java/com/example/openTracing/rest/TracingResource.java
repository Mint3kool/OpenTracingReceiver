package com.example.openTracing.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.Data;

import com.example.openTracing.Consumer;
import com.example.openTracing.Producer;

import javax.jms.Message;

/**
 * Note: jmsTemplate is used for completely synchronous jms client calls
 */
@Service
@Component
@RestController
@RequestMapping("/api")
public class TracingResource {
	
	@Autowired
	private Consumer consumer;
	
	@Autowired
	private Producer producer;

	@Autowired
	private ApplicationContext ctx;

	private static final Logger logger = LoggerFactory.getLogger(TracingResource.class);

	@RequestMapping(value = "/request/{queue}", method = RequestMethod.GET)
	public String getRequest(@PathVariable("queue") String queue) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		Object o = jms.receiveAndConvert(queue);
		return o.toString();
	}
	
	@RequestMapping(value = "/batch/{queue}", method = RequestMethod.GET)
	public void getBatch(@PathVariable("queue") String queue, @RequestParam("quantity") int quantity) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		for (int i = 0; i < quantity; i++) {
			jms.receiveAndConvert(queue);
		}
	}
	
	@RequestMapping(value = "/request/{queue}", method = RequestMethod.POST)
	public void sendRequest(@PathVariable("queue") String queue, @RequestParam("message") String message) {
		sendCustomMessage(queue, message);
	}

	@RequestMapping(value = "/batch/{queue}", method = RequestMethod.POST)
	public void sendBatch(@PathVariable("queue") String queue, @RequestParam("quantity") int quantity) {
		for (int i = 0; i < quantity; i++) {
			sendMessage(queue);
		}
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public void startTest(@RequestParam("queue") String queue, @RequestParam("quantity") int quantity) {
		
		producer.setQueueName(queue);
		consumer.setQueueName(queue);
		
		Thread consumerThread = new Thread(consumer);
        consumerThread.start();
		
        Thread producerThread = new Thread(producer);
        producerThread.start();
	}

	public void sendMessage(String queue) {
		sendCustomMessage(queue, java.util.UUID.randomUUID().toString());
	}
	
	public void sendCustomMessage(String queue, String message) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		jms.convertAndSend(queue, message);
	}
}
