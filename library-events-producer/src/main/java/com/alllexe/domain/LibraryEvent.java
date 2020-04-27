/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 24.04.2020
 */
package com.alllexe.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LibraryEvent {

    private Integer libraryEventId;
    @NotNull
    @Valid
    private Book book;
    private LibraryEventType libraryEventType;
}
