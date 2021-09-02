package io.mseemann.mongodb.streams.mongo;

import org.bson.BsonTimestamp;

public record OplogEntry(String op, String ns, BsonTimestamp ts) {
}
