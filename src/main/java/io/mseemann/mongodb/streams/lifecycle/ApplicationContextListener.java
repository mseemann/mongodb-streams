package io.mseemann.mongodb.streams.lifecycle;

import io.mseemann.mongodb.streams.mongo.MongoStats;
import io.mseemann.mongodb.streams.mongo.UserCollectionChangeStreamService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ApplicationContextListener {

    UserCollectionChangeStreamService userCollectionChangeStreamService;
    MongoStats mongoStats;

    @EventListener(classes = ContextRefreshedEvent.class)
    public void handleContextStart(){
        mongoStats.init();
        userCollectionChangeStreamService.start();
    }

    @EventListener(classes = ContextClosedEvent.class)
    public void handleShutdown() {
        userCollectionChangeStreamService.stop();
    }
}
