package ktb.leafresh.backend.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile("!eks")
public class GcsConfig {

  @Value("${gcp.project-id}")
  private String projectId;

  @Value("${gcp.credentials.location}")
  private String credentialsPath;

  @Bean
  public Storage storage() throws IOException {
    InputStream credentialsInputStream;

    File credentialsFile = new File(credentialsPath);
    if (credentialsFile.exists()) {
      credentialsInputStream = new FileInputStream(credentialsFile);
    } else {
      credentialsInputStream = new ClassPathResource(credentialsPath).getInputStream();
    }

    GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsInputStream);

    return StorageOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(credentials)
        .build()
        .getService();
  }
}
