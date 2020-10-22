package com.example.openTracing.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.Data;
import okhttp3.MediaType;

import com.example.openTracing.Consumer;

import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.util.GlobalTracer;

import java.util.Map;

import javax.jms.Message;

/**
 * Note: jmsTemplate is used for completely synchronous jms client calls
 */
@Service
@Component
@RestController
@RequestMapping("/api")
public class ReceiverResource {

	@Autowired
	private ApplicationContext ctx;

	private static final Logger logger = LoggerFactory.getLogger(ReceiverResource.class);

	@RequestMapping(value = "/trace", method = RequestMethod.GET)
	public void getTrace(@RequestParam("queue") String queue) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		Tracer t = (Tracer) jms.receiveAndConvert(queue);

		Span s = t.buildSpan("jms_recieve").start();

		s.setTag("second", "2");
		s.finish();
	}

	@RequestMapping(value = "/apiTrace", method = RequestMethod.GET)
	public void getApiTrace(@RequestHeader Map<String, String> headers) {

		Tracer t = GlobalTracer.get();

		Tracer.SpanBuilder spanBuilder;

		SpanContext parent = t.extract(Builtin.HTTP_HEADERS, new TextMapAdapter(headers));

		Span newSpan = null;

		if (parent == null) {
			spanBuilder = t.buildSpan("new_span");
		} else {
			spanBuilder = t.buildSpan("extend_span").asChildOf(parent);
		}

		newSpan = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();

		t.scopeManager().activate(newSpan);

		newSpan.setTag("more_baggage", "super_bags");

		newSpan.finish();
	}

	public void sendMessage(String queue) {
		sendCustomMessage(queue, java.util.UUID.randomUUID().toString());
	}

	public void sendCustomMessage(String queue, String message) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		jms.convertAndSend(queue, message);
	}

	public void sendCustomObjectMessage(String queue, Object o) {
		JmsTemplate jms = ctx.getBean(JmsTemplate.class);
		jms.convertAndSend(queue, o);
	}
}
