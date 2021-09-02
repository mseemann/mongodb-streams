package io.mseemann.mongodb.streams.ui;

import io.mseemann.mongodb.streams.mongo.User;
import io.mseemann.mongodb.streams.mongo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.apache.commons.lang3.RandomStringUtils;

@RestController
@AllArgsConstructor
public class UserController {

    UserRepository userRepository;

    @GetMapping("/user")
    public Flux<User> getAllUser() {
        return userRepository.findAll();
    }

    @PostMapping("/create-user")
    public Mono<User> createUser() {
        var randomName = RandomStringUtils.random(10, true, false);
        return userRepository.save(new User(null, randomName));
    }
}
