package com.github.jetnet.yacrud.service;

import com.github.jetnet.yacrud.dto.PersonDto;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonService {
    Mono<PersonDto> getPerson(String id);

    Mono<PersonDto> addPerson(PersonDto personDto);

    Mono<PersonDto> updatePerson(String id, PersonDto personDto);

    Mono<PersonDto> deletePerson(String id);

    Flux<PersonDto> getAll(Pageable pageable);
}
