package ktb.leafresh.backend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Swagger 프로파일에서만 활성화되어 OpenAPI JSON 파일을 자동으로 생성하는 컴포넌트
 */
@Component
@Profile("swagger")
public class OpenApiExportConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final ObjectMapper objectMapper;
    
    @Value("${server.port:8080}")
    private int serverPort;

    public OpenApiExportConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 별도 스레드에서 실행하여 애플리케이션 시작을 블록하지 않음
        new Thread(() -> {
            try {
                extractOpenApiSpec();
            } catch (Exception e) {
                System.err.println("❌ OpenAPI spec 추출 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }, "openapi-export-thread").start();
    }

    private void extractOpenApiSpec() throws IOException, InterruptedException {
        System.out.println("🔄 OpenAPI 문서 생성을 시작합니다...");
        
        // 서버가 완전히 준비될 때까지 대기
        int maxRetries = 30;
        int retryCount = 0;
        RestTemplate restTemplate = new RestTemplate();
        
        while (retryCount < maxRetries) {
            try {
                Thread.sleep(2000); // 2초 대기
                
                String apiUrl = "http://localhost:" + serverPort + "/v3/api-docs";
                System.out.println("📡 API 문서 요청 중: " + apiUrl + " (시도 " + (retryCount + 1) + "/" + maxRetries + ")");
                
                String openApiJson = restTemplate.getForObject(apiUrl, String.class);
                
                if (openApiJson != null && !openApiJson.trim().isEmpty()) {
                    saveOpenApiSpec(openApiJson);
                    System.out.println("✅ OpenAPI 문서 생성 완료!");
                    System.exit(0);
                    return;
                } else {
                    throw new RuntimeException("빈 응답을 받았습니다");
                }
                
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("최대 재시도 횟수 초과: " + e.getMessage(), e);
                }
                System.out.println("⚠️ 재시도 중... (" + retryCount + "/" + maxRetries + "): " + e.getMessage());
            }
        }
    }
    
    private void saveOpenApiSpec(String openApiJson) throws IOException {
        // JSON을 예쁘게 포맷팅
        Object jsonObject = objectMapper.readValue(openApiJson, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        
        // build 디렉토리 생성
        Files.createDirectories(Paths.get("build"));
        
        // build/openapi.json 파일로 저장
        String outputPath = Paths.get("build", "openapi.json").toString();
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(prettyJson);
        }
        
        System.out.println("📄 파일 저장 완료: " + outputPath);
        System.out.println("📊 파일 크기: " + Files.size(Paths.get(outputPath)) + " bytes");
    }
}
