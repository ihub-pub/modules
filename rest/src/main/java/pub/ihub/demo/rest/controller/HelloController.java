package pub.ihub.demo.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import pub.ihub.demo.feign.HelloClient;
import pub.ihub.demo.service.HelloService;

/**
 * @author liheng
 */
@RequiredArgsConstructor
@RestController
public class HelloController implements HelloClient {

	private final HelloService helloService;

	@Override
	public String hello() {
		return helloService.hello();
	}

}
