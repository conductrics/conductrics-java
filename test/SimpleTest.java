import com.conductrics.*;
import java.util.*;


public class SimpleTest {
	// some stuff to choose from
	enum Color {
		RED,
		BLUE,
		GREEN
	}
	public static abstract class Test {
		Conductrics.Agent agent;
		public void setup() { }
		public abstract void run();
		public void teardown() { }
	}
	/* Write tests for the following use cases:
	 * 1. a dev NBA create an Agent, and give it a name
	 * 2. a dev NBA set global: (apply as defaults for all Agents)
	 *   2a. apiKey
	 *   2b. ownerCode
	 *   2c. baseUrl
	 * 3. a dev NBA set per-agent values for: a) apiKey, b) ownerCode, c) baseUrl, d) name
	 * 4. a dev NBA decide(session, array of choices...)
	 *   4a. returns one of the choices
	 * 5. a dev NBA reward(session, [value])
	 * 6. a dev NBA expire(session)
	 * 7. TBD: a dev NBA throttle() globally or per-agent.
	 */
	public static class CreateAgentTest extends Test {
		public void run() {
			this.agent = new Conductrics.Agent("java-test-one");
			assert this.agent.name == "java-test-one" : "a dev NBA create an Agent, and give it a name";
		}
	}
	public static class GlobalSettingsTest extends Test {
		public void run() {
			this.agent = new Conductrics.Agent("java-test-two");
			Conductrics.apiKey = "magic-api-key";
			Conductrics.ownerCode = "magic-owner-code";
			String save = Conductrics.baseUrl;
			try {
				Conductrics.baseUrl = "http://magic-base-url";
				assert this.agent.getApiKey() == "magic-api-key" : "2a. apiKey";
				assert this.agent.getOwnerCode() == "magic-owner-code" : "2b. ownerCode";
				assert this.agent.getBaseUrl() == "magic-base-url" : "2c. baseUrl";
			} finally {
				Conductrics.baseUrl = save;
			}
		}
	}
	public static class AgentSettingsTest extends Test {
		public void run() {
			this.agent = new Conductrics.Agent("unit-test");
			try {
				this.agent.setApiKey("local-api-key")
					.setOwnerCode("local-owner-code")
					.setBaseUrl("http://local-base-url")
					.setAgentName("new-local-name");
			} catch( java.net.MalformedURLException ex ) { }
			assert this.agent.getApiKey() == "local-api-key" : "3a. apiKey";
			assert this.agent.getApiKey() != Conductrics.apiKey : "3a-1. apiKey";
			assert this.agent.getOwnerCode() == "local-owner-code" : "3b. ownerCode";
			assert this.agent.getOwnerCode() != Conductrics.ownerCode: "3b-1. ownerCode";
			assert this.agent.getBaseUrl() == "http://local-base-url" : "3c. baseUrl";
			assert this.agent.getBaseUrl() != Conductrics.baseUrl : "3c-1. baseUrl";
		}
	}
	public static class DecisionTest extends Test {
		public void run() {
			this.agent = new Conductrics.Agent("unit-test");
			Conductrics.apiKey = "api-HFrPvhjnhVufRXtCGOIzejSW";
			Conductrics.ownerCode = "owner_HJJnKxAdm";
			String sessionId = Conductrics.createSessionId();
			Color chosen = this.agent.decide(sessionId, Arrays.asList(Color.RED, Color.BLUE));
			assert chosen == Color.RED || chosen == Color.BLUE : "4a. decide returns one of the choices";
		}
	}
	public static class RewardTest extends Test {
		public void run() {
			this.agent = new Conductrics.Agent("unit-test")
				.setApiKey("api-HFrPvhjnhVufRXtCGOIzejSW")
				.setOwnerCode("owner_HJJnKxAdm");
			String sessionId = Conductrics.createSessionId();
			assert agent.reward(sessionId) == 0 : "5a. Rewarding a session before making decisions has 0 value";
			agent.decide(sessionId, Arrays.asList(Color.RED, Color.BLUE));
			assert agent.reward(sessionId, 2.2) == 2.2 : "5. Rewarding with a value produces that value.";
			assert agent.reward(sessionId, 2.2) == 0.0 : "5b. By default, each session can only be rewarded once.";
		}
	}
	public static class ExpireTest extends Test {
		public void run() {
			this.agent = new Conductrics.Agent("unit-test")
				.setApiKey("api-HFrPvhjnhVufRXtCGOIzejSW")
				.setOwnerCode("owner_HJJnKxAdm");
			String sessionId = Conductrics.createSessionId();
			agent.decide(sessionId, Arrays.asList("a", "b"));
			agent.expire(sessionId);
			assert agent.reward(sessionId, 2.2) == 0.0 : "6. expire";
		}
	}

	public static void main(String... args) {
		List<Test> tests = Arrays.asList(
			new CreateAgentTest(),
			new GlobalSettingsTest(),
			new AgentSettingsTest(),
			new DecisionTest(),
			new RewardTest(),
			new ExpireTest()
		);
		for( Test t : tests ) {
			try {
				System.out.print(t.getClass().getSimpleName() + ": ");
				t.run();
				System.out.println("PASS");
			} catch( Exception e ) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}

	}
}
