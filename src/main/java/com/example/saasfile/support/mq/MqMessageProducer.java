package com.example.saasfile.support.mq;

public interface MqMessageProducer {

    void send(String binding, Object message);
}
