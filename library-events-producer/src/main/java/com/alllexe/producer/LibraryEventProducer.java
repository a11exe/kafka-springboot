/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 24.04.2020
 */
package com.alllexe.producer;

import com.alllexe.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LibraryEventProducer {

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    String topic = "library-events";

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent.getBook());

        ListenableFuture<SendResult<Integer, String>> result = kafkaTemplate.sendDefault(key, value);
        result.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });

    }

    public SendResult<Integer, String> sendLibraryEventSynch(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent.getBook());

        SendResult<Integer, String> result = null;

        try {
            result = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("ExecutionException | InterruptedException sending the message exception is {}", e.getMessage());
        } catch (Exception e) {
            log.error("Execution sending the message exception is {}", e.getMessage());
        }

        return result;

    }

    public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent_aproach2(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        List<Header> headers = List.of(new RecordHeader("header", "value".getBytes()));

        ProducerRecord<Integer, String> record = buildProducerRecord(key, value, topic, headers);

        ListenableFuture<SendResult<Integer, String>> result = kafkaTemplate.send(record);
        result.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });

        return result;

    }

    private ProducerRecord<Integer,String> buildProducerRecord(Integer key, String value, String topic, List<Header> headers) {
        return new ProducerRecord<>(topic, null, key, value, headers);

    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Message sent fails for the key {} value {}, exception is {}",
                key, value, ex.getMessage());
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key {} value {}, partition is {}",
                key, value, result.getRecordMetadata().partition());
    }

}
