package pub.ihub.demo.feign.fallback;

import org.springframework.stereotype.Component;
import pub.ihub.demo.feign.HelloClient;

/**
 * @author liheng
 */
@Component
public class HelloClientFallback implements HelloClient {

	@Override
	public String hello() {
		return null;
	}

}
