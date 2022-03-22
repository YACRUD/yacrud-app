package com.github.jetnet.yacrud;

import com.github.jetnet.yacrud.controller.PersonController;
import com.github.jetnet.yacrud.dto.PersonDto;
import com.github.jetnet.yacrud.entity.Person;
import com.github.jetnet.yacrud.repository.ESRepository;
import com.github.jetnet.yacrud.service.PersonServiceImpl;
import com.github.jetnet.yacrud.util.EntityDtoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.times;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = PersonController.class)
@Import(PersonServiceImpl.class)
class PersonControllerTest {

    //@Autowired  // and @Mock required ES up and running
    @MockBean
    ESRepository repository;

    @Autowired
    private WebTestClient webClient;

    @BeforeEach
    public void setup() {
    }

    @Test
    void testAddPerson() {
        PersonDto personDto = new PersonDto();
        personDto.setId("2");
        personDto.setFirstName("Fist Name");
        personDto.setLastName("Last Name");
        personDto.setEmail("FistName.LastName@mail.com");

        Person person = EntityDtoUtil.toEntity(personDto);

        // Repository direct test
        //        Mono<Person> personSaved = repository.save(person);
        //        Assert.assertNotNull(personSaved.block());

        // No ES up and running

        // Assuming: the user does not exist yet
        Mockito.when(repository.findById(person.getId())).thenReturn(Mono.empty());
        Mockito.when(repository.save(person)).thenReturn(Mono.just(person));

        webClient.post()
                .uri("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(personDto))
                .exchange()
                .expectStatus().isCreated();

        Mockito.verify(repository, times(1)).save(person);
    }
}
