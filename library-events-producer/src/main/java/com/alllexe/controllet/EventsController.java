/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 24.04.2020
 */
package com.alllexe.controllet;

import com.alllexe.domain.LibraryEvent;
import com.alllexe.domain.LibraryEventType;
import com.alllexe.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class EventsController {

    @Autowired
    LibraryEventProducer libraryEventProducer;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> addLibraryEvent(@Valid @RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {

        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        log.info("before sending message");
//         libraryEventProducer.sendLibraryEvent(libraryEvent);
//        SendResult<Integer, String> result = libraryEventProducer.sendLibraryEventSynch(libraryEvent);
//        log.info("sending result is {}", result);
        libraryEventProducer.sendLibraryEvent_aproach2(libraryEvent);
        log.info("after sending message");

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> putLibraryEvent(@Valid @RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {

        if (libraryEvent.getLibraryEventId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the LibraryEventID");
        }
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        log.info("before sending message");
        libraryEventProducer.sendLibraryEvent_aproach2(libraryEvent);
        log.info("after sending message");

        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

}
