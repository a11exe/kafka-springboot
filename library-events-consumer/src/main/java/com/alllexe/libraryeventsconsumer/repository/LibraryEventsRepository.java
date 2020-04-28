/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 28.04.2020
 */
package com.alllexe.libraryeventsconsumer.repository;

import com.alllexe.libraryeventsconsumer.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Integer> {


}
