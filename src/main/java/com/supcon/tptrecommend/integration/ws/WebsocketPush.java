package com.supcon.tptrecommend.integration.ws;

import com.supcon.systemcomponent.websocket.WebSocketSender;
import com.supcon.systemcomponent.websocket.holder.WebSocketSessionHolder;
import com.supcon.systemcomponent.websocket.message.JsonMessageDO;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;


/**
 * WebSocket 推送
 *
 * @author luhao
 * @date 2025/06/12 14:41:30
 */
public class WebsocketPush {

    public static <T> void pushMessage(T data) {
        List<WebSocketSession> sessions = WebSocketSessionHolder.getSessions();
        sessions.forEach(session -> WebSocketSender.send(session, JsonMessageDO.data(null, data)));
    }


}
