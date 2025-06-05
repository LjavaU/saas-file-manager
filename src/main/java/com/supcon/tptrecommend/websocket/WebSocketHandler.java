package com.supcon.tptrecommend.websocket;

import com.supcon.systemcomponent.websocket.handler.WebSocketEventHandler;
import com.supcon.tptrecommend.manager.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler implements WebSocketEventHandler {

    private final FileManager fileManager;
    @Value("${sup.websocket.session-key-options.param-name}")
    private String paramName;

    
    @Override
    public void onOpen(WebSocketSession session) {
        WebSocketEventHandler.super.onOpen(session);
        CompletableFuture.runAsync(() -> {
            Map<String, Object> attributes = session.getAttributes();
            fileManager.handleFileAnalysis(Long.valueOf(String.valueOf(attributes.get(paramName))));
        });

    }

    @Override
    public void onClose(WebSocketSession session, CloseStatus closeStatus) {
        WebSocketEventHandler.super.onClose(session, closeStatus);
        log.info("WebSocket connection closed: " + closeStatus);
    }

    @Override
    public void onError(WebSocketSession session, Throwable exception) {
        WebSocketEventHandler.super.onError(session, exception);
    }
}
