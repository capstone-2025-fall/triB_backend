//package triB.triB.global.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//import triB.triB.chat.webSocket.ChatWebSocketHandler;
//
//@Configuration
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(new ChatWebSocketHandler(), "/myHandler")
//                .setAllowedOrigins("*")
//                .withSockJS();
//    }
//}
