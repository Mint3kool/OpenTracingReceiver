package com.example.openTracing.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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

	private int sleep_time = 1000;

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

	@RequestMapping(value = "/nestedApiTrace", method = RequestMethod.GET)
	public void getNestedApiTrace(@RequestHeader Map<String, String> headers) {

		Tracer t = GlobalTracer.get();

		Tracer.SpanBuilder spanBuilder;

		SpanContext parent = t.extract(Builtin.HTTP_HEADERS, new TextMapAdapter(headers));

		Span newSpan = null;

		if (parent == null) {
			spanBuilder = t.buildSpan("new_span_1");
		} else {
			spanBuilder = t.buildSpan("child_span_1").asChildOf(parent);
		}

		newSpan = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();

		t.scopeManager().activate(newSpan);

		newSpan.setTag("time", sleep_time);

		subSpan();

		try {
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		newSpan.finish();

		followChildSpan(t, parent);
	}

	public void followChildSpan(Tracer t, SpanContext parent) {
		Tracer.SpanBuilder spanBuilder;

		Span span2 = null;

		if (parent == null) {
			spanBuilder = t.buildSpan("new_span_2");
		} else {
			spanBuilder = t.buildSpan("child_span_2").asChildOf(parent);
		}

		span2 = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();

		t.scopeManager().activate(span2);

		span2.setTag("time_2", sleep_time);

		try {
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		span2.finish();
	}

	public void subSpan() {
		Tracer t = GlobalTracer.get();

		Tracer.SpanBuilder spanBuilder;

		Span newSpan = null;

		spanBuilder = t.buildSpan("sub_span");

		newSpan = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();

		t.scopeManager().activate(newSpan);

		int sleep_time = 420;
		newSpan.setTag("blaze", sleep_time);

		try {
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		newSpan.finish();
	}

	@RequestMapping(value = "/finishedApiTrace", method = RequestMethod.GET)
	public void getFinshedApiTrace(@RequestHeader Map<String, String> headers) {

		Tracer t = GlobalTracer.get();

		Tracer.SpanBuilder spanBuilder;

		SpanContext parent = t.extract(Builtin.HTTP_HEADERS, new TextMapAdapter(headers));

		Span newSpan = null;
		if (parent == null) {
			spanBuilder = t.buildSpan("new_span");
		} else {
			spanBuilder = t.buildSpan("follow_span_1").addReference(Tags.SPAN_KIND_SERVER, parent);
		}

		newSpan = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();

		t.scopeManager().activate(newSpan);

		newSpan.setTag("time", sleep_time);

		try {
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		newSpan.finish();
	}
}
