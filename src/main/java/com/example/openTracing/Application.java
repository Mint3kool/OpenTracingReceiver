package com.example.openTracing;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws Exception {
		if (!configureGlobalTracer("reciever_tracer"))
			throw new Exception("Could not configure the global tracer");

		SpringApplication.run(Application.class, args);

	}

	static boolean configureGlobalTracer(String s) {
		Tracer t = null;
		Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv()
				.withType("const").withParam(1);
		Configuration.SenderConfiguration senderConfig = new Configuration.SenderConfiguration()
				.withAgentHost("localhost").withAgentPort(5775);
		Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
				.withLogSpans(true).withSender(senderConfig);
		t = new Configuration(s).withSampler(samplerConfig).withReporter(reporterConfig).getTracer();
		GlobalTracer.registerIfAbsent(t);
		return true;
	}
}