package io.mseemann.mongodb.streams.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class MongoStats {

    MongoClient mongoClient;

    public void init() {
        // db.getCollection('oplog.rs').find({ns:"streams.user"},{ts:1}).sort({"ts":-1}).limit(1)
        Query q = new Query().addCriteria(Criteria.where("ns").is("streams.user")).with(Sort.by(Sort.Direction.DESC, "ts"));
        ReactiveMongoTemplate reactiveMongoTemplate = new ReactiveMongoTemplate(mongoClient, "local");

        Gauge.builder("sync.last.seen.oplog.at", reactiveMongoTemplate, template -> {
                    var result = Optional.ofNullable(template.find(q, OplogEntry.class, "oplog.rs").blockFirst());
                    // timestamp in ms
                    return result.map(r -> result.get().ts().getTime() * 1000L).orElse(0L);
                })
                .strongReference(true)
                .register(Metrics.globalRegistry);
    }
}
