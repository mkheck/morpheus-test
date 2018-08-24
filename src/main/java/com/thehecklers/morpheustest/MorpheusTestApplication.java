package com.thehecklers.morpheustest;

import lombok.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.UUID;

@SpringBootApplication
public class MorpheusTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MorpheusTestApplication.class, args);
    }
}

@RestController
@RequestMapping("/greetings")
class GreetingController {
    private final MessageRepo repo;

    GreetingController(MessageRepo repo) {
        this.repo = repo;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<HelloMessage> getAllGreetings() {
        return repo.findAll()
                .delayElements(Duration.ofSeconds(1));
    }
}

@Component
class DataLoader {
    private final MessageRepo repo;

    DataLoader(MessageRepo repo) {
        this.repo = repo;
    }

    @PostConstruct
    private void load() {
        repo.deleteAll().thenMany(
                Flux.just("Hello", "Hi", "Guten Tag", "Hola", "Buenos días", "Bonjour")
                        .map(message -> new HelloMessage(UUID.randomUUID().toString(), message))
                        .flatMap(repo::save))
                .thenMany(repo.findAll())
                .subscribe(System.out::println);
    }
}

interface MessageRepo extends ReactiveCrudRepository<HelloMessage, String> {
}

@Document
@Value
class HelloMessage {
    private String id;
    private String message;
}