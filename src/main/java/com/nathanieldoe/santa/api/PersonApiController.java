package com.nathanieldoe.santa.api;

import com.nathanieldoe.santa.api.exception.InvalidExclusionException;
import com.nathanieldoe.santa.api.model.ExclusionRequest;
import com.nathanieldoe.santa.model.Person;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Validated
@RestController
@Tag(name = "Person API")
@RequestMapping(PersonApiController.BASE_PATH)
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "Bearer-Token")
@OpenAPIDefinition(info = @Info(title = "Person API", description = "Person Information"))
public class PersonApiController {

    public static final String BASE_PATH = "/person";

    PersonApiService api;

    ObservationRegistry observationRegistry;

    public PersonApiController(PersonApiService api, ObservationRegistry observationRegistry) {
        this.api = api;
        this.observationRegistry = observationRegistry;
    }

    /**
     * @return All of the {@link Person}
     */
    @Operation(summary = "Find all people")
    @GetMapping(path = "list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Person>> list() {
        List<Person> all = new ArrayList<>();
        Observation.createNotStarted("Find all people", observationRegistry)
                .observe(() -> all.addAll(api.list()));
        return ResponseEntity.of(Optional.ofNullable(all));
    }


    /**
     * @param id The ID of the {@link Person} to find
     *
     * @return The {@link Person} object if found
     */
    @Operation(summary = "Find person by ID")
    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> findById(@PathVariable Long id) {
        AtomicReference<Person> person = null;
        Observation.createNotStarted("Find Person by ID", observationRegistry)
                .observe(() -> person.set(api.fetchById(id)));
        return ResponseEntity.of(Optional.ofNullable(person.get()));
    }

    /**
     * @param personId The ID of the {@link  Person} to add an exclusion to
     * @param request The exclusion to add
     * @return The {@link Person} that was updated
     */
    @Operation(summary = "Adds a person to the list of exclusions for a person")
    @PutMapping(path = "{id}/exclude", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> addExclusion(@PathVariable("id") Long personId, @RequestBody ExclusionRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No person available to be excluded");
        }

        Person added = Observation.createNotStarted("addExclusion", this.observationRegistry)
                .observe(() -> {
                    try {
                        return api.addExclusion(personId, request);
                    } catch (InvalidExclusionException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                    }
                });

        return ResponseEntity.of(Optional.of(added));
    }

    /**
     * @param person The {@link Person} object to create or update
     *
     * @return The {@link Person} that was created or updated
     */
    @Operation(summary = "Create / update person")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> createOrUpdate(@RequestBody Person person) {
        if (person == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No person available to be persisted");
        }

        Optional<Person> createdOrUpdated = Observation.createNotStarted("createOrUpdatePerson", this.observationRegistry)
                .observe(() -> Optional.ofNullable(api.createOrUpdate(person)));
        return ResponseEntity.of(createdOrUpdated);
    }


    /**
     * @param personId The ID of the {@link Person} object to delete
     */
    @Operation(summary = "Delete person")
    @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> delete(@PathVariable("id") Long personId) {
        Observation.createNotStarted("deletePerson", this.observationRegistry)
                .observe(() -> api.delete(personId));
        return ResponseEntity.ok().build();
    }

}
