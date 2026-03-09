package com.example.saasfile.support.mq;

public abstract class AbstractMqMessageHandler<T> {

    public abstract String binding();

    public abstract void handleMessage(T message);
}
