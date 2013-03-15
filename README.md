This lets you use the Conductrics API easily from Java.


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
        // Set your access credentials (from the signup email)
        Conductrics.apiKey = "api-HFrPvhjnhVufRXtCGOIzejSW";
        Conductrics.ownerCode = "owner_HJJnKxAdm";
        // Create a new agent
        Agent agent = new Agent("color-picker");
        // Generate a session id (unique for each test in this case)
        String sessionId = Conductrics.createSessionId(); // (not required) use the built-in helper to make a session id for us
        // Choose one of three colors
        Color chosen = agent.decide(sessionId, Arrays.asList(Color.RED, Color.BLUE, Color.GREEN));
        if( chosen == Color.GREEN )
          agent.reward(sessionId, 2.0); // GREEN is best
        else if( chosen == Color.BLUE )
          agent.reward(sessionId, 1.0); // BLUE is ok
        // RED is worthless

        // very soon, the .decide() call will learn to favor GREEN
      }
    }
