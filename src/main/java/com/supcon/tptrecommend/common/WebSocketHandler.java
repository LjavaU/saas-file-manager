package com.supcon.tptrecommend.common;

import com.supcon.systemcomponent.websocket.handler.WebSocketEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler implements WebSocketEventHandler {

    
    @Override
    public void onOpen(WebSocketSession session) {
        WebSocketEventHandler.super.onOpen(session);

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
