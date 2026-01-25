package seg.work.geuliumieum.server.config.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
        @Value("${cloud.aws.region}") String region,
        @Value("${cloud.aws.access-key:}") String accessKey,
        @Value("${cloud.aws.secret-key:}") String secretKey
    ) {
        Region r = Region.of(region);
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            return S3Client.builder()
                .region(r)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        }
        return S3Client.builder()
            .region(r)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
