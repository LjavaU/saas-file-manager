package com.example.saasfile.support.websocket;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WebSocketSender {

    private WebSocketSender() {
    }

    public static void sendByKey(String sessionKey, Object message) {
        log.debug("WebSocket push skipped. sessionKey={}, payload={}", sessionKey, message);
    }
}
