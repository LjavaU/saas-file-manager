package com.example.saasfile.support.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpMqMessageProducer implements MqMessageProducer {

    @Override
    public void send(String binding, Object message) {
        log.info("No MQ backend configured, binding={}, message={}", binding, message);
    }
}
