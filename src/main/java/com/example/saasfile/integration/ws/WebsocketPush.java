package com.example.saasfile.integration.ws;

import com.example.saasfile.support.websocket.WebSocketSender;
import com.example.saasfile.support.websocket.JsonMessageDO;


/**
 * WebSocket 閹恒劑鈧?
 *
 * @author luhao
 * @date 2025/06/12 14:41:30
 */
public class WebsocketPush {

    public static <T> void pushMessage(String sessionKey,T data) {
        WebSocketSender.sendByKey(sessionKey, JsonMessageDO.data(null, data));
    }


}
