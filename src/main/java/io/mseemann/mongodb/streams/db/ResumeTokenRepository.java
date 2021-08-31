package io.mseemann.mongodb.streams.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeTokenRepository extends JpaRepository<ResumeTokenEntity, String> {

  String USER_COLLECTION = "user";

 }
