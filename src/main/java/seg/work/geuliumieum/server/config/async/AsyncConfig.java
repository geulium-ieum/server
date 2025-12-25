package seg.work.geuliumieum.server.config.async;

import java.util.Map;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "auditAsyncExecutor")
    public AsyncTaskExecutor auditAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("audit-async-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setTaskDecorator(new LoggingTaskDecorator());
        executor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}

class LoggingTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> callerThreadContext = MDC.getCopyOfContextMap();

        return () -> {
            try {
                if (callerThreadContext != null) {
                    MDC.setContextMap(callerThreadContext);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}