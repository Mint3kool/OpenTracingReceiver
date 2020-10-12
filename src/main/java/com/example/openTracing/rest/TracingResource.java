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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.jms.Message;

/**
 * Note: jmsTemplate is used for completely synchronous jms client calls
 */
@RestController
@RequestMapping("/api")
public class TracingResource {

	@Autowired
	private ApplicationContext ctx;

	private static final Logger logger = LoggerFactory.getLogger(TracingResource.class);

	@RequestMapping(value = "/singleConsumer", method = RequestMethod.POST)
	public String consumeRequest() {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		Object o = jms.receiveAndConvert("newmessageclass");
		return o.toString();
	}


	@RequestMapping(value = "/singleProducer/{guid}", method = RequestMethod.POST)
	public void produceRequest(@PathVariable("guid") String value) {
		createMessage(value);
	}

	@RequestMapping(value = "/batch/{quantity}", method = RequestMethod.POST)
	public void batchRequest(@PathVariable("quantity") String value) {
		int quantity = Integer.parseInt(value);
		for (int i = 0; i < quantity; i++) {
			createMessage(java.util.UUID.randomUUID().toString());
		}
	}

	public void createMessage(String message) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		jms.convertAndSend("newmessageclass", message);
	}
}
