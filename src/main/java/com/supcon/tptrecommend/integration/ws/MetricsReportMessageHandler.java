package com.supcon.tptrecommend.integration.ws;

import com.supcon.systemcomponent.websocket.handler.PlanTextMessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MetricsReportMessageHandler implements PlanTextMessageHandler {
    @Override
    public void handle(WebSocketSession session, String message) {
        System.out.println(session.getUri());
        System.out.println("收到的消息为："+message);
    }
}
