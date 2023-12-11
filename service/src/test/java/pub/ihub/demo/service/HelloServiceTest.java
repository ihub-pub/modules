package pub.ihub.demo.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pub.ihub.demo.service.config.ServiceAutoConfiguration;

/**
 * @author liheng
 */
@ContextConfiguration(
	classes = ServiceAutoConfiguration.class,
	initializers = ConfigDataApplicationContextInitializer.class
)
@SpringBootTest
class HelloServiceTest {

	@Autowired
	HelloService helloService;

	@Test
	void hello() {
		Assertions.assertEquals("Hello IHub", helloService.hello());
	}

}
