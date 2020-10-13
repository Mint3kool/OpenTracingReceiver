package com.example.openTracing.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.Data;

import com.example.openTracing.Consumer;

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
	private ApplicationContext ctx;

	private static final Logger logger = LoggerFactory.getLogger(TracingResource.class);

	@RequestMapping(value = "/request/{topic}", method = RequestMethod.GET)
	public String getRequest(@PathVariable("topic") String topic) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		Object o = jms.receiveAndConvert(topic);
		return o.toString();
	}
	
	@RequestMapping(value = "/batch/{topic}", method = RequestMethod.GET)
	public void getBatch(@PathVariable("topic") String topic, @RequestParam("quantity") int quantity) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		for (int i = 0; i < quantity; i++) {
			jms.receiveAndConvert(topic);
		}
	}
	
	@RequestMapping(value = "/request/{topic}", method = RequestMethod.POST)
	public void sendRequest(@PathVariable("topic") String topic, @RequestParam("message") String message) {
		sendCustomMessage(topic, message);
	}

	@RequestMapping(value = "/batch/{topic}", method = RequestMethod.POST)
	public void sendBatch(@PathVariable("topic") String topic, @RequestParam("quantity") int quantity) {
		for (int i = 0; i < quantity; i++) {
			sendMessage(topic);
		}
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public void startTest(@RequestParam("test") String topic, @RequestParam("quantity") int quantity) {
		Thread consumerThread = new Thread(consumer);
		consumerThread.start();
	}

	public void sendMessage(String topic) {
		sendCustomMessage(topic, java.util.UUID.randomUUID().toString());
	}
	
	public void sendCustomMessage(String topic, String message) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		jms.convertAndSend(topic, message);
	}
}
