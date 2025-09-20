package triB.triB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TriBApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriBApplication.class, args);
	}

}
