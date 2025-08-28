package com.cvmento.domain.coverLetter.entity;

import com.cvmento.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "crawl_cover_letters")
public class CrawlCoverLetter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_letter_id")
    private Long coverLetterId;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    protected CrawlCoverLetter() {}

    public CrawlCoverLetter(String text) {
        this.text = text;
    }
}
