/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 25.04.2020
 */

import com.alllexe.LibraryEventsProducerApplication;
import com.alllexe.domain.Book;
import com.alllexe.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = LibraryEventsProducerApplication.class)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.bootstrap.servers=${spring.embedded.kafka.brokers}"})
@Slf4j
public class LibraryEventsControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs =
                new HashMap<>(KafkaTestUtils.consumerProps(
                        "group1",
                        "true",
                        embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(
                        configs,
                new IntegerDeserializer(),
                new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(10)
    void LibraryEventAdd() throws JsonProcessingException {
        Book book = Book.builder()
                .bookId(222)
                .bookAuthor("Alex")
                .bookName("Kafka")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(libraryEvent, httpHeaders);

        ResponseEntity<LibraryEvent> response =
                restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, httpEntity, LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        String expected = "{\"libraryEventId\":null,\"book\":{\"bookId\":222,\"bookName\":\"Kafka\",\"bookAuthor\":\"Alex\"},\"libraryEventType\":\"NEW\"}";
        assertEquals(expected, record.value());
    }

    @Test
    @Timeout(10)
    void LibraryEventUpdate() throws JsonProcessingException {
        Book book = Book.builder()
                .bookId(222)
                .bookAuthor("Alex")
                .bookName("Kafka")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(2345)
                .book(book)
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(libraryEvent, httpHeaders);

        ResponseEntity<LibraryEvent> response =
                restTemplate.exchange("/v1/libraryevent", HttpMethod.PUT, httpEntity, LibraryEvent.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        String expected = "{\"libraryEventId\":2345,\"book\":{\"bookId\":222,\"bookName\":\"Kafka\",\"bookAuthor\":\"Alex\"},\"libraryEventType\":\"UPDATE\"}";
        assertEquals(expected, record.value());
    }
}
