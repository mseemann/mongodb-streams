package io.mseemann.mongodb.streams.db;

import io.mseemann.mongodb.streams.mongo.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(propagation = Propagation.REQUIRED)
@AllArgsConstructor
public class SyncToPostgresService {

    UserEntityRepository userEntityRepository;
    ResumeTokenRepository resumeTokenRepository;

    public void syncInASingleTx(User document, String bookmarkToken, Long tokenTimeStamp) {
        log.info("syncing to db: {}, with token: {}", document, bookmarkToken);
        var token = ResumeTokenEntity.builder()
                .collection(ResumeTokenRepository.USER_COLLECTION)
                .token(bookmarkToken)
                .tokenTimeStamp(tokenTimeStamp)
                .build();
        var entity = UserEntity.builder()
                .id(document.id())
                .userName(document.userName()
                ).build();

        userEntityRepository.save(entity);
        resumeTokenRepository.save(token);
    }


}
