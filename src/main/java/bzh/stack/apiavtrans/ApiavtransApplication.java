package bzh.stack.apiavtrans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiavtransApplication {

	@Value("${server.address}")
	private String serverAddress;

	@Value("${server.port}")
	private String serverPort;

	public static void main(String[] args) {
		SpringApplication.run(ApiavtransApplication.class, args);
	}

	@Bean
	CommandLineRunner onStartup() {
		return args -> System.out.println("API started on http://" + serverAddress + ":" + serverPort);
	}

}
