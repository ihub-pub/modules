package pub.ihub.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import pub.ihub.demo.feign.fallback.HelloClientFallback;

/**
 * @author liheng
 */
@FeignClient(
	value = "demo",
	fallback = HelloClientFallback.class
)
public interface HelloClient {

	/**
	 * hello
	 *
	 * @return hello
	 */
	@GetMapping("hello")
	String hello();

}
