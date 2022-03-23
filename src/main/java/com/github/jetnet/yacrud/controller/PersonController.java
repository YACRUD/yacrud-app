package com.github.jetnet.yacrud.controller;

import com.github.jetnet.yacrud.dto.PersonDto;
import com.github.jetnet.yacrud.entity.Person;
import com.github.jetnet.yacrud.service.PersonService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Objects;

@Log4j2
@RestController
@RequestMapping("person")
@Validated
public class PersonController extends BaseController {
    public static final String MSG_DOCUMENT_DOES_NOT_EXIST = "Document does not exist";
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    @ApiOperation(
            value = "This method is used to get all clients with paging and soring support.",
            authorizations = {@Authorization(value = "auth")}
    )
    public Flux<PersonDto> getAll(
            // For Swagger
            @ApiParam(value = "Page number", allowableValues = "range[0, infinity]", defaultValue = "0")
            // For Controller
            @RequestParam(required = false, defaultValue = "0")
            @Min(value = 0, message = "Page number must be positive")
                    Integer page,

            @ApiParam(value = "Results per page", allowableValues = "range[1, 100]", defaultValue = "10")
            @RequestParam(required = false, defaultValue = "10")
            @Min(value = 1, message = "Number results per page must be greater than zero")
            @Max(value = 100, message = "Max results per page must not exceed 100")
                    Integer size,

            @ApiParam(value = "Field name to sort results in ascending order")
                    Person.SortField sortField) {

        // Parameters validation
        String sortBy = Objects.toString(sortField, null);
        Pageable pageable = sortBy != null ?
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy)) :
                PageRequest.of(page, size);

        log.debug("getAll parameters: page = {}, size = {}, sort field = {}", page, size, sortBy);

        return personService.getAll(pageable);
    }

    @GetMapping("{id}")
    public Mono<PersonDto> getPerson(@PathVariable final String id) {
        return personService.getPerson(id).switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_DOCUMENT_DOES_NOT_EXIST)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PersonDto> addPerson(@RequestBody final PersonDto personDto) {
        log.debug("Creating a new record with email address: {}", personDto.getEmail());

        return personService.addPerson(personDto)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document exists already")));
    }

    @PatchMapping("{id}")
    public Mono<PersonDto> updatePerson(@PathVariable final String id, @RequestBody final PersonDto personDto) {
        if (!id.equals(personDto.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document ID mismatch");
        }

        return personService.updatePerson(id, personDto)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_DOCUMENT_DOES_NOT_EXIST)));
    }

    @DeleteMapping("{id}")
    public Mono<PersonDto> deletePerson(@PathVariable final String id) {
        log.debug("Deleting a person: {}", id);

        return personService.deletePerson(id).switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_DOCUMENT_DOES_NOT_EXIST)));
    }

    @PostMapping("bulk")
    public Mono<Object> saveAll(@RequestBody final Flux<PersonDto> personsDto) {
        log.debug("Bulk upload");
        return personService.saveAll(personsDto);
    }
}
