package pub.ihub.demo.feign;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pub.ihub.demo.feign.config.ClientAutoConfiguration;

/**
 * @author liheng
 */
@ContextConfiguration(
	classes = ClientAutoConfiguration.class,
	initializers = ConfigDataApplicationContextInitializer.class
)
@SpringBootTest
class HelloClientTest {

	@Autowired
	HelloClient helloClient;

	@Test
	void hello() {
		Assertions.assertNull(helloClient.hello());
	}

}
