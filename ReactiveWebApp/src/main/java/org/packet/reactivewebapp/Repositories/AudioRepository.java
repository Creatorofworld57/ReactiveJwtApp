package org.packet.reactivewebapp.Repositories;

import org.packet.reactivewebapp.Entities.Audio;
import org.packet.reactivewebapp.Entities.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Repository
public interface AudioRepository extends ReactiveMongoRepository<Audio,Long> {
    Mono<Audio> findByName(String name);

    Flux<Audio> findAllById (Long id);
}
