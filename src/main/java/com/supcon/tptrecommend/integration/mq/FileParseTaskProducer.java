package com.supcon.tptrecommend.integration.mq;

import com.supcon.framework.mq.core.MqMessageProducer;
import com.supcon.tptrecommend.common.config.FileParseMqProperties;
import com.supcon.tptrecommend.dto.mq.FileParseTaskMessage;
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