package pub.ihub.demo.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import pub.ihub.demo.rest.Application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author liheng
 */
@AutoConfigureMockMvc
@ContextConfiguration(
	classes = Application.class,
	initializers = ConfigDataApplicationContextInitializer.class
)
@SpringBootTest
class HelloControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void hello() {
		try {
			mockMvc.perform(get("/hello")).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
