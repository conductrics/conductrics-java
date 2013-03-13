import com.conductrics.*;
import java.util.Arrays;
import java.security.SecureRandom;
import java.math.BigInteger;

public class SimpleTest {
	enum Color {
		RED,
		BLUE,
		GREEN
	}
	public static void main(String... args) {
		// Set our access credentials (from the signup email)
		Conductrics.apiKey = "api-HFrPvhjnhVufRXtCGOIzejSW";
		Conductrics.ownerCode = "owner_HJJnKxAdm";
		// Create a new agent
		Conductrics.Agent agent = new Conductrics.Agent("color-picker");
		// Generate a session id (unique for each test in this case)
		String sessionId = new BigInteger(130, new SecureRandom()).toString(32);
		// Choose one of three colors
		System.out.println( agent.decide(sessionId, Arrays.asList(Color.RED, Color.BLUE, Color.GREEN)) );
	}
}
