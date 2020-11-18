package com.example.openTracing;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Landing page for the application
 * 
 * @author tshen
 *
 */
@RestController
public class HelloController {

	/**
	 * url that points to swagger api resources
	 */
	@RequestMapping("/")
	public String index() {
		return "<a href=\"http://localhost:8081/swagger-ui.html#/tracing-resource\">API test page</a>";
	}

}