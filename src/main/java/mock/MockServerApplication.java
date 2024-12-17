package mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableScheduling
@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = {"mock.service", "mock.web"})
public class MockServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockServerApplication.class, args);
	}

	@GetMapping("/yes")
	public String indexHome() {
		return "test";
	}
}




