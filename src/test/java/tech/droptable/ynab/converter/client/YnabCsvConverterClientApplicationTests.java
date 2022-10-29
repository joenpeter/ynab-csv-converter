package tech.droptable.ynab.converter.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
//@TestPropertySource(locations = "./application-test.properties")
class YnabCsvConverterClientApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void fullRunNoException() throws Exception {
	  SpringApplication.run(YnabCsvClientApplicationRunner.class);
	}
	
}
