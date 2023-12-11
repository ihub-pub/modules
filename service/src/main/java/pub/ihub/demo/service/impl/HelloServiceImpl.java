package pub.ihub.demo.service.impl;

import org.springframework.stereotype.Service;
import pub.ihub.demo.service.HelloService;

/**
 * @author liheng
 */
@Service
public class HelloServiceImpl implements HelloService {

	@Override
	public String hello() {
		return "Hello IHub";
	}

}
