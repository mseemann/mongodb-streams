package io.mseemann.mongodb.streams.mongo;

import io.mseemann.mongodb.streams.db.ResumeTokenRepository;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document(collection = ResumeTokenRepository.USER_COLLECTION)
public record User(@Id String id, @Indexed String userName) {
}
