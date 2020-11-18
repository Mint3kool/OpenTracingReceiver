package com.example.openTracing.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import io.opentracing.propagation.TextMap;

/**
 * Required method that performs the extract functionality expected from
 * OpenTracing. In Java, the extract method simply maps to a TextMap object
 * from the API call which contains the headers.
 * @author tshen
 *
 */
public class HttpHeadersExtract implements TextMap {
	private final Map<String, String> map = new HashMap<>();

	HttpHeadersExtract(final Map<String, String> headers) {
		for (Map.Entry<String, String> entry : headers.entrySet())
			map.put(entry.getKey(), entry.getValue());
	}

	@Override
	public void put(String arg0, String arg1) {
		throw new UnsupportedOperationException("This class can be used only with Tracer.inject()");
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		return map.entrySet().iterator();
	}

}
