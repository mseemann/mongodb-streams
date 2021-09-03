package io.mseemann.mongodb.streams.mongo;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

// $changeStream may not be opened on the internal local database

@Component
@Slf4j
public class MongoStats {

    final ReactiveMongoTemplate reactiveMongoTemplate;

    private final AtomicLong lastOplogAt = new AtomicLong(0);

    public MongoStats(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;


        Gauge.builder("sync.last.seen.oplog.at", this, mongoStats -> mongoStats.lastOplogAt.get()).strongReference(true).register(Metrics.globalRegistry);
    }

    public void init() {
        Query tailQ = new Query().addCriteria(Criteria.where("ns").is("streams.user"));
        reactiveMongoTemplate.tail(tailQ, OplogEntry.class, "oplog.rs")
                .retryWhen(Retry.indefinitely().doAfterRetryAsync(signal -> Mono.delay(Duration.ofSeconds(10)).then()))
                .subscribe(entry -> {
                    log.info("tailed entry: {}", entry);
                    lastOplogAt.set(entry.ts().getTime() * 1000L);
                });
    }
}
