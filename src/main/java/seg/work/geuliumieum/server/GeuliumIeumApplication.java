package seg.work.geuliumieum.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GeuliumIeumApplication {

    static void main(String[] args) {
        SpringApplication.run(GeuliumIeumApplication.class, args);
    }

}
