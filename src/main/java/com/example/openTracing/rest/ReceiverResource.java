package com.example.openTracing.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.util.GlobalTracer;

import java.util.Map;

/**
 * This is the receiver class for the OpenTracing calls. The API calls here were primarily
 * used for testing. They can be triggered as stand alone calls through the swagger UI.
 */
@Service
@Component
@RestController
@RequestMapping("/api")
public class ReceiverResource {

	private int sleep_time = 100;

	@Autowired
	private ApplicationContext ctx;

	/**
	 * Creates several 'nested' child traces, if the parent exists.
	 * @param headers
	 */
	@RequestMapping(value = "/nestedApiTrace", method = RequestMethod.GET)
	public void getNestedApiTrace(@RequestHeader Map<String, String> headers) {

		Tracer t = GlobalTracer.get();

		Tracer.SpanBuilder spanBuilder;

		// Getting context from passed in parent span
		SpanContext parent = t.extract(Builtin.HTTP_HEADERS, new TextMapAdapter(headers));

		Span newSpan = null;

		// Nesting a child, if the parent exists. Otherwise, create a new span
		if (parent == null) {
			spanBuilder = t.buildSpan("new_span_1");
		} else {
			spanBuilder = t.buildSpan("child_span_1").asChildOf(parent);
		}

		newSpan = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();

		t.scopeManager().activate(newSpan);

		newSpan.setTag("time", sleep_time);

		// Nesting another span
		subSpan();

		try {
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		newSpan.finish();

		// Starting another span that will follow in the same context, rather than nest
		followChildSpan(t, newSpan.context());
	}

	/**
	 * Starting a new span after a previous span has finished under the
	 * same parent
	 * @param t
	 * @param parent
	 */
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

		span2.setTag("sleep_time", sleep_time);

		try {
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		span2.finish();
	}

	/**
	 * Creates a random nested subspan
	 */
	public void subSpan() {
		Tracer t = GlobalTracer.get();

		Tracer.SpanBuilder spanBuilder;

		Span newSpan = null;

		spanBuilder = t.buildSpan("sub_span");

		newSpan = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();

		t.scopeManager().activate(newSpan);

		int sleep_time = 50;
		newSpan.setTag("sleep_time", sleep_time);

		try {
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		newSpan.finish();
	}

	/**
	 * Creates a 'follows from' child if the parent trace exists
	 * @param headers
	 */
	@RequestMapping(value = "/finishedApiTrace", method = RequestMethod.GET)
	public void getFinshedApiTrace(@RequestHeader Map<String, String> headers) {

		Tracer t = GlobalTracer.get();

		Tracer.SpanBuilder spanBuilder;

		SpanContext parent = t.extract(Builtin.HTTP_HEADERS, new TextMapAdapter(headers));

		Span newSpan = null;
		if (parent == null) {
			spanBuilder = t.buildSpan("new_span");
		} else {
			spanBuilder = t.buildSpan("follow_span_1").addReference(References.FOLLOWS_FROM, parent);
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
