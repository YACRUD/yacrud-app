package com.github.jetnet.yacrud.service;

import com.github.jetnet.yacrud.dto.PersonDto;
import com.github.jetnet.yacrud.entity.Person;
import com.github.jetnet.yacrud.repository.ESRepository;
import com.github.jetnet.yacrud.util.EntityDtoUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.Objects;

@Log4j2
@Service
public class PersonServiceImpl implements PersonService {

    private final ESRepository repository;

    public PersonServiceImpl(ESRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PersonDto> getPerson(final String id) {
        return repository.findById(id).filter(Objects::nonNull)
                .map(EntityDtoUtil::toDto).retryWhen(esRetrySpec());
    }

    /**
     * Creates a person entry in the repository for the given {@link PersonDto} object.
     * If ID is present in the given entry, then it will be checked, if that ID exists
     * in the repository already.
     * <br/>
     * En empty Mono object will be returned, if there is an entry with the same ID
     * in the repository.
     * <br/>
     * The full object will be returned, if en entry has been created in the repository.
     * The repository will auto-generate ID, if that has not been provided.
     * <br/>
     *
     * @param personDto a {@link PersonDto} object
     * @return a {@link Mono} with the full {@link PersonDto} object or an empty {@link Mono}
     */
    @Override
    public Mono<PersonDto> addPerson(PersonDto personDto) {
        String id = personDto.getId();
        return Mono.just(new Person())  // starting with a clean Person object to get into first "flatMap"
                .flatMap(p -> id != null ? repository.findById(id) : Mono.empty()) // searches in the repo, returns a Person or empty
                .flatMap(p -> Mono.just(new Person())) // if a record was found, replace it by an empty Person (NOT empty Mono!)
                .switchIfEmpty(repository.save(EntityDtoUtil.toEntity(personDto)))  // if nothing was found or the given Person object does not have an ID, then create a new one
                .filter(p -> p.getId() != null) // discard the empty Person object from the second "flatMap" step
                .map(EntityDtoUtil::toDto).retryWhen(esRetrySpec());
    }

    @Override
    public Mono<PersonDto> updatePerson(final String id, final PersonDto personDto) {
        return repository.findById(id).switchIfEmpty(Mono.empty()).filter(Objects::nonNull)
                .flatMap(p -> repository.save(EntityDtoUtil.toEntity(personDto)))
                .map(EntityDtoUtil::toDto).retryWhen(esRetrySpec());
    }

    @Override
    public Mono<PersonDto> deletePerson(final String id) {
        return repository.findById(id).switchIfEmpty(Mono.empty()).filter(Objects::nonNull)
                .flatMap(p -> repository.delete(p).then(Mono.just(p)))
                .map(EntityDtoUtil::toDto).retryWhen(esRetrySpec());
    }

    @Override
    public Flux<PersonDto> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .filter(Objects::nonNull)
                .map(EntityDtoUtil::toDto).retryWhen(esRetrySpec());
    }

    @Override
    public Mono<Object> saveAll(Flux<PersonDto> personsDto) {
        return repository.saveAll(personsDto.map(EntityDtoUtil::toEntity))
                .then(Mono.empty())
                .retryWhen(esRetrySpec());
    }

    /**
     * Return a retry specification for retrying failed requests to Elasticsearch.
     * Currently, a progressively increasing delay between three attempts: roughly at 2, 4, 8-second intervals.
     * <br/>
     * It retries on <code>TOO_MANY_REQUESTS</code> error only.
     *
     * @return a {@link Retry} backoff specification
     */
    private RetryBackoffSpec esRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(2)).filter(this::isTooManyRequests);
    }

    /**
     * Checks if the exception from the Elasticsearch is a "Too Many Requests"
     *
     * @param throwable A {@link Throwable} from the {@link Retry} filter
     * @return true if the REST error response is "Too Many Requests"
     */
    private boolean isTooManyRequests(Throwable throwable) {
        log.warn("Checking for 'too many requests' - error ({}) message: {}", throwable.getClass(), throwable.getMessage());
        return
                // Workaround: instead of RestStatusException, we get an NPE here (errors are not propagated correctly - bug???)
                (throwable instanceof NullPointerException) ||
                        // The proper 429-error handling, but we never get here:
                        (throwable instanceof RestStatusException &&
                                ((RestStatusException) throwable).getStatus() == HttpStatus.TOO_MANY_REQUESTS.value());
    }
}