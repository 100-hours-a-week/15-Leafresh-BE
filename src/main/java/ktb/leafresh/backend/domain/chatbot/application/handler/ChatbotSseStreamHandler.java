//package ktb.leafresh.backend.domain.chatbot.application.handler;
//
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
///**
// * AI 서버로부터 받은 SSE 응답을 그대로 FE로 전달하는 중계 핸들러
// * - WebClient를 통해 AI 서버로 GET 요청을 보냄
// * - ServerSentEvent<String> 형식으로 수신
// * - 받은 SSE 메시지를 포맷 유지한 채 SseEmitter를 통해 FE로 전송
// */
//public interface ChatbotSseStreamHandler {
//    void streamToEmitter(SseEmitter emitter, String uriWithQueryParams);
//}
