package io.mseemann.mongodb.streams.mongo;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.mseemann.mongodb.streams.db.ResumeTokenRepository;
import io.mseemann.mongodb.streams.db.SyncToPostgresService;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ReactiveChangeStreamOperation;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class UserCollectionChangeStreamService {
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final SyncToPostgresService syncToPostgresService;
    private final ResumeTokenRepository resumeTokenRepositoryRepo;

    private Disposable subscription;
    private final AtomicLong lastSeenAt = new AtomicLong(0);
    private final AtomicLong lastProcessedAt = new AtomicLong(0);

    private static final Instant THE_BEGINNING_OF_ALL_PROBLEMS = LocalDateTime.parse("2021-08-24T00:00:00").toInstant(ZoneOffset.UTC);

    public UserCollectionChangeStreamService(ReactiveMongoTemplate reactiveMongoTemplate, SyncToPostgresService syncToPostgresService, ResumeTokenRepository resumeTokenRepositoryRepo) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.syncToPostgresService = syncToPostgresService;
        this.resumeTokenRepositoryRepo = resumeTokenRepositoryRepo;

        Gauge.builder("sync.last.seen.doc.at", this, listener -> listener.lastSeenAt.get())
                .strongReference(true)
                .register(Metrics.globalRegistry);
        Gauge.builder("sync.last.processed.doc.at", this, listener -> listener.lastProcessedAt.get())
                .strongReference(true)
                .register(Metrics.globalRegistry);
    }

    @EventListener(classes = ContextRefreshedEvent.class)
    public void start() {

        var resumeTokenEntry = resumeTokenRepositoryRepo.findById(ResumeTokenRepository.USER_COLLECTION);

        var streamBuilder = reactiveMongoTemplate.changeStream(User.class)
                .watchCollection(ResumeTokenRepository.USER_COLLECTION);

        ReactiveChangeStreamOperation.TerminatingChangeStream<User> stream;

        if (resumeTokenEntry.isPresent()) {
            log.info("resume mongo events at token position {}", resumeTokenEntry.get().getToken());
            lastSeenAt.set(resumeTokenEntry.get().getTokenTimeStamp());
            var resumeTokenBD = new BsonDocument();
            resumeTokenBD.put("_data", new BsonString(resumeTokenEntry.get().getToken()));
            stream = streamBuilder.startAfter(resumeTokenBD);
        } else {
            log.info("resume mongo events after timestamp {}", THE_BEGINNING_OF_ALL_PROBLEMS);
            stream = streamBuilder.resumeAt(THE_BEGINNING_OF_ALL_PROBLEMS);
        }

        subscription = stream.listen()
                .retryWhen(Retry.indefinitely().doAfterRetryAsync(signal -> Mono.delay(Duration.ofSeconds(10)).then()))
                .subscribe(event -> {
            log.info("change stream event {}", event);

            BsonDocument newResumeToken = Objects.requireNonNull(event.getRaw()).getResumeToken().asDocument();
            BsonString token = newResumeToken.getString("_data");
            var timeStampInMs = Objects.requireNonNull(event.getBsonTimestamp()).getTime() * 1000L;

            syncToPostgresService.syncInASingleTx(event.getBody(), token.getValue(), timeStampInMs);

            log.info("change stream event timestamp {}", new Date(timeStampInMs));
            lastSeenAt.set(timeStampInMs);
            lastProcessedAt.set(new Date().getTime());
        });
    }

    @EventListener(classes = ContextClosedEvent.class)
    public void stop() {
        if (subscription != null) {
            subscription.dispose();
        }
    }
}
