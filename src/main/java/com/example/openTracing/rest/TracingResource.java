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

import io.opentracing.Span;
import io.opentracing.Tracer;

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

	@Autowired
	private Tracer tracer;

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

	@RequestMapping(value = "/raw", method = RequestMethod.POST)
	public void raw() {
		
		Span span = tracer.buildSpan("create employee").start();
		// Set http status code
		span.setTag("http.status_code", 201);

		// Close the span
		span.finish();
	}

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public void startTest(@RequestParam("queue") String queue, @RequestParam("quantity") int quantity) {

//		producer.setQueueName(queue);
//		--------------------------------------

		Span span = tracer.buildSpan("testMessage").start();
//
//        HttpStatus status = HttpStatus.NO_CONTENT;
//
//        try {
//            int id = Integer.parseInt(idString);
//            log.info("Received Request to delete employee {}", id);
//            span.log(ImmutableMap.of("event", "delete-request", "value", idString));
//            if (employeeService.deleteEmployee(id, span)) {
//                span.log(ImmutableMap.of("event", "delete-success", "value", idString));
//                span.setTag("http.status_code", 200);
//                status = HttpStatus.OK;
//            } else {
//                span.log(ImmutableMap.of("event", "delete-fail", "value", "does not exist"));
//                span.setTag("http.status_code", 204);
//            }
//        } catch (NumberFormatException | NoSuchElementException nfe) {
//            span.log(ImmutableMap.of("event", "delete-fail", "value", idString));
//            span.setTag("http.status_code", 204);
//        }
//
//        span.finish();

//        -----------------------------

		consumer.setQueueName(queue);

		for (int i = 0; i < quantity; i++) {
			sendMessage(queue);
		}

		Thread consumerThread = new Thread(consumer);
		consumerThread.start();

//        Thread producerThread = new Thread(producer);
//        producerThread.start();
	}

	public void sendMessage(String queue) {
		sendCustomMessage(queue, java.util.UUID.randomUUID().toString());
	}

	public void sendCustomMessage(String queue, String message) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		jms.convertAndSend(queue, message);
	}
}
