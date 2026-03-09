package com.example.saasfile.integration.mq;

import com.example.saasfile.support.mq.MqMessageProducer;
import com.example.saasfile.common.config.FileParseMqProperties;
import com.example.saasfile.dto.mq.FileParseTaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileParseTaskProducer {

    private final ObjectProvider<MqMessageProducer> mqMessageProducerProvider;

    private final FileParseMqProperties fileParseMqProperties;

    public void send(FileParseTaskMessage message) {
        MqMessageProducer producer = mqMessageProducerProvider.getIfAvailable();
        if (producer == null) {
            throw new IllegalStateException("MQ producer is not available. Please enable sup.mq.enabled and configure broker.");
        }
        producer.send(fileParseMqProperties.getFileParseBinding(), message);
    }
}