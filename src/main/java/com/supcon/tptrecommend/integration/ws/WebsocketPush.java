package com.supcon.tptrecommend.integration.ws;

import com.supcon.systemcomponent.websocket.WebSocketSender;
import com.supcon.systemcomponent.websocket.message.JsonMessageDO;


/**
 * WebSocket 推送
 *
 * @author luhao
 * @date 2025/06/12 14:41:30
 */
public class WebsocketPush {

    public static <T> void pushMessage(String sessionKey,T data) {
        WebSocketSender.sendByKey(sessionKey, JsonMessageDO.data(null, data));
    }


}
