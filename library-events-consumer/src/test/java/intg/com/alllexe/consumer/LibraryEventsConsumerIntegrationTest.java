/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 29.04.2020
 */
package com.alllexe.consumer;

import com.alllexe.libraryeventsconsumer.LibraryEventsConsumerApplication;
import com.alllexe.libraryeventsconsumer.consumer.LibraryEventsConsumer;
import com.alllexe.libraryeventsconsumer.entity.Book;
import com.alllexe.libraryeventsconsumer.entity.LibraryEvent;
import com.alllexe.libraryeventsconsumer.entity.LibraryEventType;
import com.alllexe.libraryeventsconsumer.repository.LibraryEventsRepository;
import com.alllexe.libraryeventsconsumer.service.LibraryEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = LibraryEventsConsumerApplication.class)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;
    
    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumer;

    @SpyBean
    LibraryEventsService libraryEventsService;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }


    @AfterEach
    void tearDown() {
        libraryEventsRepository.deleteAll();
    }

    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {

        String json = "{\"libraryEventId\":null,\"book\":{\"bookId\":222,\"bookName\":\"Kafka\",\"bookAuthor\":\"Alex\"},\"libraryEventType\":\"NEW\"}";
        kafkaTemplate.sendDefault(json).get();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(libraryEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsService, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> libraryEvents = (List<LibraryEvent>) libraryEventsRepository.findAll();
        assertEquals(1, libraryEvents.size());
        assertEquals(222, libraryEvents.get(0).getBook().getBookId());
    }

    @Test
    void publishUpdateLibraryEvent() throws JsonProcessingException, InterruptedException {
        String json = "{\"libraryEventId\":null,\"book\":{\"bookId\":222,\"bookName\":\"Kafka\",\"bookAuthor\":\"Alex\"},\"libraryEventType\":\"NEW\"}";
        LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);

        Book updatedBook = Book.builder()
                .bookId(222)
                .bookAuthor("Alex")
                .bookName("Kafka for dummies")
                .build();

        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        libraryEvent.setBook(updatedBook);

        kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), objectMapper.writeValueAsString(libraryEvent));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);


        verify(libraryEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsService, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        LibraryEvent libraryEventsDb = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();
        assertEquals("Kafka for dummies", libraryEventsDb.getBook().getBookName());


    }

    @Test
    void publishUpdateLibraryEvent_Not_A_Valid_LibraryEventId() throws JsonProcessingException, InterruptedException {
        String json = "{\"libraryEventId\":456,\"book\":{\"bookId\":222,\"bookName\":\"Kafka\",\"bookAuthor\":\"Alex\"},\"libraryEventType\":\"UPDATE\"}";
        LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);

        kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), objectMapper.writeValueAsString(libraryEvent));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);


        verify(libraryEventsConsumer, times(3)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsService, times(3)).processLibraryEvent(isA(ConsumerRecord.class));

        Optional<LibraryEvent> libraryEventsDb = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
        assertFalse(libraryEventsDb.isPresent());

    }
}
