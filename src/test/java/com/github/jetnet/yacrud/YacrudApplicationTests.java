package com.github.jetnet.yacrud;

import com.github.jetnet.yacrud.entity.Person;
import com.github.jetnet.yacrud.repository.ESRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class YacrudApplicationTests {

    @Autowired
    ReactiveElasticsearchTemplate esTemplate;

    @Autowired
    ESRepository esRepository;

    //@BeforeEach
    public void setUp() {
        Person person = new Person("John", "Do", "john.do@chammy.info");
//        esRepository.save(person).block();
        esRepository.save(person).subscribe(result ->
                System.out.println("++++++++++++++ result: " + result)
        );
//        System.out.println("++++++++++++++++++++ total: " + esRepository.findAll().count().block());
        esRepository.findAll().count().subscribe(result ->
                System.out.println("++++++++++++++++++++ total: " + result)
        );
    }

    @AfterEach
    public void tearDown() {
        // esRepository.deleteAll().block();
    }

    //@Test
    void searchPerson() {
        final String firstName = "John";
        final Query query = new NativeSearchQueryBuilder().withQuery(matchQuery("firstName", firstName)).build();
        //final SearchHits<Person> hits = esTemplate.search(query, Person.class, IndexCoordinates.of("persons"));

        //assertEquals(1, hits.getTotalHits());
    }

}
