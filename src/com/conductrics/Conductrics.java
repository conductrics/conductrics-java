package com.conductrics;
import java.io.*;
import java.util.*;
import java.security.SecureRandom;
import java.math.BigInteger;
// vim: let g:syntastic_java_javac_classpath="lib/gson-2.2.2.jar"

public class Conductrics {

	// These are the global defaults, Agent instances can over-ride them.
	public static String baseUrl = "https://api.conductrics.com";
	public static String apiKey;
	public static String ownerCode;

	// Static utilities
	private static SecureRandom random = new SecureRandom();
	public static String createSessionId() { return new BigInteger(130, new SecureRandom()).toString(32); }
	public static String readStream(InputStream is) {
		Scanner s = new Scanner(is,"UTF-8").useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
