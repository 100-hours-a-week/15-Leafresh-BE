package ktb.leafresh.backend.global;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/spring")
@RequiredArgsConstructor
public class TestController {

    private final RestTemplate restTemplate = new RestTemplate();

    // FastAPI → Spring: GET 수신
    @GetMapping("/hello")
    public String receiveFromFastApiGet() {
        return "Hello from Spring!";
    }

    // FastAPI → Spring: POST 수신
    @PostMapping("/echo")
    public Map<String, String> receiveFromFastApiPost(@RequestBody Map<String, String> body) {
        return Map.of("spring_received", body.get("message"));
    }

    // Spring → FastAPI: GET 요청
    @GetMapping("/call-fastapi")
    public Map<String, String> callFastApiGet() {
        String response = restTemplate.getForObject("http://localhost:8000/fastapi/hello", String.class);
        return Map.of("from_fastapi", response);
    }

    // Spring → FastAPI: POST 요청
    @PostMapping("/call-fastapi")
    public Map<String, String> callFastApiPost() {
        Map<String, String> body = Map.of("message", "방가방가!");
        String response = restTemplate.postForObject("http://localhost:8000/fastapi/echo", body, String.class);
        return Map.of("from_fastapi", response);
    }
}
