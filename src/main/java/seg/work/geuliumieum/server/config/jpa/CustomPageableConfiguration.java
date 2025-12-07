package seg.work.geuliumieum.server.config.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class CustomPageableConfiguration {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer customize() {
        return pageableHandlerMethodArgumentResolver -> pageableHandlerMethodArgumentResolver.setOneIndexedParameters(true);
    }
}