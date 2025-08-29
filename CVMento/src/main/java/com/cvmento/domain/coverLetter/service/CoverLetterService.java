package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoverLetterService {
    private final CoverLetterRepository coverLetterRepository;

}

