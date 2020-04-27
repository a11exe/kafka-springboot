package controller;/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 27.04.2020
 */

import com.alllexe.LibraryEventsProducerApplication;
import com.alllexe.controllet.EventsController;
import com.alllexe.domain.Book;
import com.alllexe.domain.LibraryEvent;
import com.alllexe.producer.LibraryEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventsController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {LibraryEventsProducerApplication.class})
public class LibraryEventControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEvent() throws Exception {

        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Alex")
                .bookName("Kafka for developers")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent_aproach2(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    }

    @Test
    void postLibraryEvent_4xx() throws Exception {

        Book book = Book.builder()
                .bookId(null)
                .bookAuthor(null)
                .bookName("Kafka for developers")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent_aproach2(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("book.bookAuthor - must not be blank ,book.bookId - must not be null"));
    }

    @Test
    void putLibraryEvent() throws Exception {

        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Alex")
                .bookName("Kafka for developers")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(342)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent_aproach2(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(put("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void putLibraryEvent_4xx() throws Exception {

        Book book = Book.builder()
                .bookId(342)
                .bookAuthor("Alex")
                .bookName("Kafka for developers")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent_aproach2(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(put("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please pass the LibraryEventID"));
    }
}
