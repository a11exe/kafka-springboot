/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 28.04.2020
 */
package com.alllexe.libraryeventsconsumer.service;

import com.alllexe.libraryeventsconsumer.entity.LibraryEvent;
import com.alllexe.libraryeventsconsumer.repository.LibraryEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsService {

    @Autowired
    private LibraryEventsRepository libraryEventsRepository;

    @Autowired
    ObjectMapper objectMapper;


    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                validate(libraryEvent);
                save(libraryEvent);
                break;
        }

    }

    private void validate(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("Library id is missing");
        }
        Optional<LibraryEvent> byId = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
        if (!byId.isPresent()) {
            throw new IllegalArgumentException("Library event with id " + libraryEvent.getLibraryEventId() + " doesn't exist");
        }
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully persisted library event {}", libraryEvent);
    }
}
