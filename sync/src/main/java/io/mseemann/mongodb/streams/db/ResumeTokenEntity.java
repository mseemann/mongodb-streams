package io.mseemann.mongodb.streams.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_bookmark", schema = "company")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResumeTokenEntity {

    @Id
    @Column(name = "collection")
    private String collection;

    @Column(name = "token")
    private String token;

    @Column(name = "token_time_stamp")
    private Long tokenTimeStamp;
}
