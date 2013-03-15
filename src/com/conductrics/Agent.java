package com.conductrics;
import java.util.*;
import java.net.*;
import java.io.*;
import com.google.gson.Gson;

/** Agent is the focal point for using the Conductrics API.
 * Each agent makes decisions, and learns to optimize those decisions to maximize reward.
 * Example: see ../test/SimpleTest.java
 */
public class Agent {
	public String name;
	private String baseUrl;
	private String apiKey;
	private String ownerCode;
	private Gson gson;
	public Agent(String name) {
		this.name = name;
		this.gson = new Gson();
	}

	/** Use this to change the API server you are connecting to.
	 * By default, the base URL is an 'https:' URL,
	 * this can also be used to connect without SSL if you want.
	 */
	public Agent setBaseUrl(String url) throws MalformedURLException {
		new URL(url); // force it to parse, throws
		this.baseUrl = url;
		return this;
	}

	/** Specify an API key for this instance (optional).
	 * If you don't specify a key here, `Conductrics.apiKey` will be used.
	 */
	public Agent setApiKey(String key) {
		this.apiKey = key;
		return this;
	}

	/** Specify an Owner Code for this instance (optional).
	 * If you don't specify an owner code here, `Conductrics.ownerCode` will be used.
	 */
	public Agent setOwnerCode(String code) {
		this.ownerCode = code;
		return this;
	}

	/** Change the Agent Name (previously given to the constructor). */
	public Agent setAgentName(String name) {
		this.name = name;
		return this;
	}

	/** Gets the correct Base URL (either this instance's or the global). */
	public String getBaseUrl() { return this.baseUrl != null ? this.baseUrl : Conductrics.baseUrl; }

	/** Gets the correct API Key (either this instance's or the global). */
	public String getApiKey() { return this.apiKey != null ? this.apiKey : Conductrics.apiKey; }

	/** Gets the correct Owner Code (either this instance's or the global). */
	public String getOwnerCode() { return this.ownerCode != null ? this.ownerCode : Conductrics.ownerCode; }

	protected String buildUrl(String... parts) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getBaseUrl());
		sb.append("/");
		sb.append(this.getOwnerCode());
		sb.append("/");
		sb.append(this.name);
		for( String part : parts ) {
			sb.append("/");
			sb.append(part);
		}
		return sb.toString();
	}

	/** Headers is a type alias for passing HTTP headers around. */
	protected class Headers extends HashMap<String,String> { }

	/** Options apply to all request types.  */
	protected class Options {
		public String session;
		public String apikey;
		public Headers toHeaders() {
			Headers map = new Headers();
			map.put("x-mpath-session", session);
			map.put("x-mpath-apikey", apikey);
			return map;
		}
	}
	public class DecisionOptions extends Options {
		public String ua;
		public String ip;
		public String referer;
		public String segment;
		public Map<String, Double> features;
		public Headers toHeaders() {
			Headers map = super.toHeaders();
			if( ua != null ) map.put("x-mpath-ua", ua);
			if( ip != null ) map.put("x-mpath-ip", ip);
			if( referer != null ) map.put("x-mpath-referer", referer);
			if( segment != null ) map.put("x-mpath-segment", segment);
			if( features != null && features.size() > 0 ) {
				StringBuilder sb = new StringBuilder();
				for( String k : features.keySet() )
					sb.append(String.format("%s:%.2f,",k,features.get(k)));
				map.put("x-mpath-features", sb.toString().replaceAll(",$",""));
			}
			return map;
		}
	}
	public class RewardOptions extends Options {
		public Double value;
		public RewardOptions() { value = 1.0; }
		public RewardOptions(Double v) { value = v; }
		public Headers toHeaders() {
			Headers h = super.toHeaders();
			if( value != null && value != 1.0 )
				h.put("x-mpath-reward", String.format("%.2f", value));
			return h;
		}
	}
	public class ExpireOptions extends Options {
	}
	protected class SimpleDecision {
		public String session;
		public String decision;
	}
	protected class SimpleReward {
		public String session;
		public Double value;
	}
	protected class FullDecision {
		public String agent;
		public String session;
		public String policy;
		public String point;
		public String segment;
		public Map<String, Double> features;
		public Map<String, Map<String, Map<String, String>>> decisions;
	}

	private String requestJson(Options opts, String url) {
		// Parse the URL
		URL u;
		try {
			u = new URL(url);
		} catch( MalformedURLException ex ) {
			ex.printStackTrace();
			return null;
		}

		// Connect to the server
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection)u.openConnection();
		} catch( IOException ex ) {
			ex.printStackTrace();
			return null;
		}

		// Set headers
		Headers h = opts.toHeaders();
		for( String k : h.keySet() ) {
			conn.setRequestProperty(k, h.get(k));
		}

		// Ask for a result
		try {
			return Conductrics.readStream(conn.getInputStream());
		} catch( IOException ex ) {
			ex.printStackTrace();
			System.out.println("getErrorStream() has: " + Conductrics.readStream(conn.getErrorStream()));
		}

		return null;
	}

	// Make a simple decision.
	public <T> T decide(String sessionId, List<T> choices) {
		DecisionOptions opts = new DecisionOptions();
		opts.session = sessionId;
		return decide(opts, choices);
	}

	public <T> T decide(DecisionOptions opts, List<T> choices) {
		// First, peek at the choices to get set up
		int len = choices.size();
		if( len == 0 ) return null;
		T defaultChoice = choices.get(0);

		// The apikey can be set per-request, per-agent, or globally
		if( opts.apikey == null )
			opts.apikey = this.getApiKey();

		try {
			// Fetch some json text from the server
			String result = this.requestJson(opts, this.buildUrl("decision", String.format("%d", len)));
			// Parse the response
			if( result != null ) {
				SimpleDecision d = this.gson.fromJson(result, SimpleDecision.class);
				// Return the choice dictated by the server
				int choice = Integer.parseInt(d.decision);
				return choices.get(choice);
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
		}

		// If all else fails, always return the default choice
		return defaultChoice;
	}

	public Double reward(String sessionId) {
		RewardOptions opts = new RewardOptions();
		opts.session = sessionId;
		return this.reward(sessionId, opts);
	}
	public Double reward(String sessionId, Double value) {
		RewardOptions opts = new RewardOptions();
		opts.session = sessionId;
		opts.value = value;
		return this.reward(sessionId, opts);
	}
	public Double reward(String sessionId, RewardOptions opts) {
		opts.session = sessionId;
		if( opts.apikey == null )
			opts.apikey = this.getApiKey();
		String result = requestJson(opts, this.buildUrl("goal"));
		return this.gson.fromJson(result, SimpleReward.class).value;
	}

	public void expire(String sessionId) { this.expire(sessionId, new ExpireOptions()); }
	public void expire(String sessionId, ExpireOptions opts) {
		opts.session = sessionId;
		if( opts.apikey == null )
			opts.apikey = this.getApiKey();
		requestJson(opts, this.buildUrl("expire"));
	}

}
