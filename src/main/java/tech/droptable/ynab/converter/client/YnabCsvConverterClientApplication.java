package tech.droptable.ynab.converter.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"tech.droptable.ynab.converter"})
public class YnabCsvConverterClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(YnabCsvConverterClientApplication.class, args);
	}

}
