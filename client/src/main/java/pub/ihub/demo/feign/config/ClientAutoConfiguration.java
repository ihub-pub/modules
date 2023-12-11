package pub.ihub.demo.feign.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author liheng
 */
@AutoConfiguration
@ComponentScan("pub.ihub.demo.feign.fallback")
public class ClientAutoConfiguration {

}
