package ktb.leafresh.backend.global.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Configuration
@Profile("!local")
@RequiredArgsConstructor
public class PubSubPublisherConfig {

    private final Environment environment;

    @Bean(name = "purchasePubSubPublisher")
    public Publisher purchasePubSubPublisher() throws IOException {
        String projectId = environment.getProperty("gcp.project-id");
        String topicId = environment.getProperty("gcp.pubsub.topics.order");

        TopicName topicName = TopicName.of(projectId, topicId);

        return Publisher.newBuilder(topicName)
                .setCredentialsProvider(() -> GoogleCredentials.getApplicationDefault())
                .build();
    }

    @Bean(name = "imageVerificationPubSubPublisher")
    public Publisher imageVerificationPublisher() throws IOException {
        String projectId = environment.getProperty("gcp.project-id");
        String topicId = environment.getProperty("gcp.pubsub.topics.image-verification");

        TopicName topicName = TopicName.of(projectId, topicId);

        return Publisher.newBuilder(topicName)
                .setCredentialsProvider(() -> GoogleCredentials.getApplicationDefault())
                .build();
    }
}
