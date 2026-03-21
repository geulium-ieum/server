package seg.work.geuliumieum.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
@OpenAPIDefinition(
    servers = {
        @Server(url = "https://geulium-ieum-api-dev.seoeungi.work", description = "Default Server url"),
        @Server(url = "http://geulium-ieum-api-dev.seoeungi.work", description = "Default Server url"),
    }
)
@EnableJpaRepositories(basePackages = "seg.work.geuliumieum.server.common.repository")
public class GeuliumIeumApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(GeuliumIeumApplication.class, args);
    }

}
