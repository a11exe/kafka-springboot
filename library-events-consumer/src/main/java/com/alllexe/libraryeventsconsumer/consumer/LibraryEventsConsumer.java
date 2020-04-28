/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 27.04.2020
 */
package com.alllexe.libraryeventsconsumer.consumer;

import com.alllexe.libraryeventsconsumer.service.LibraryEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsConsumer {

    @Autowired
    LibraryEventsService libraryEventsService;

    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        libraryEventsService.processLibraryEvent(consumerRecord);
        log.info("Consumer record {}", consumerRecord);
    }

}
