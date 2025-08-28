package com.cvmento.domain.coverLetter.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "cover_letter_qna")
public class CoverLetterQna extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_letter_qna_id")
    private Long coverLetterQnaId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_letter_id", nullable = false)
    private CoverLetter coverLetter;

    protected CoverLetterQna() {}

    public CoverLetterQna(String question, CoverLetter coverLetter) {
        this.question = question;
        this.coverLetter = coverLetter;
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
    }

    public void setCoverLetter(CoverLetter coverLetter) {
        this.coverLetter = coverLetter;
    }
}