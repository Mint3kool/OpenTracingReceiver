package com.example.openTracing.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * The class that generates all of the API endpoints
 */
@RestController
@RequestMapping("/api")
public class TracingResource {

	@Value(value = "${aws.readFromAws:false}")
	private boolean readFromAws;

	private static final Logger logger = LoggerFactory.getLogger(TracingResource.class);

	@PostMapping(value = "/token/{value}", consumes = "text/plain")
	public void token(@PathVariable("value") String value) {
		return;
	}
	
	@RequestMapping(value = "/batch", method = RequestMethod.POST)
	public Integer request() {
		return -1;
	}

//	@RequestMapping(value = "/batch", method = RequestMethod.POST)
//	public Integer batchRequest(@RequestBody CustomerList customers) {
//		int batch = -1;
//
//		try {
//			batch = service.setBatchNumber(customers);
//		} catch (IOException e) {
//			logger.error(String.format("Failed to generate batch number with error: %s"), e.toString());
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "failed to generate batch");
//		}
//
//		if (batch == -1) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//					"Invalid customerId in batch");
//		} else if (batch == -2) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//					"Batch requests full, process some batches before adding more");
//		} else  {
//			return batch;
//		}
//	}
}
