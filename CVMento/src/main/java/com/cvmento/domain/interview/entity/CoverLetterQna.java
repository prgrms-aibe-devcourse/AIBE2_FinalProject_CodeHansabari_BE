package com.cvmento.domain.interview.entity;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.interview.enums.QuestionSourceType;
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

    @Column(name = "tip", columnDefinition = "TEXT")
    private String tip;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private QuestionSourceType sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_letter_id", nullable = false)
    private CoverLetter coverLetter;

    protected CoverLetterQna() {}

    public CoverLetterQna(String question, CoverLetter coverLetter, QuestionSourceType sourceType) {
        this.question = question;
        this.coverLetter = coverLetter;
        this.sourceType = sourceType;
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
    }

    public void updateTip(String tip) {
        this.tip = tip;
    }
    public void updateAnswerAndTip(String answer, String tip) {
        this.answer = answer;
        this.tip = tip;
    }

    public void updateSourceType(QuestionSourceType sourceType) {
        this.sourceType = sourceType;
    }
}