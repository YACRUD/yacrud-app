package com.github.jetnet.yacrud.repository;

import com.github.jetnet.yacrud.entity.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ESRepository extends ReactiveSortingRepository<Person, String> {
    // Create this signature for "findAll", as this repository interface does not provide it
    @Query("{\"match_all\":{}}")
    Flux<Person> findAll(Pageable pageable);
}
