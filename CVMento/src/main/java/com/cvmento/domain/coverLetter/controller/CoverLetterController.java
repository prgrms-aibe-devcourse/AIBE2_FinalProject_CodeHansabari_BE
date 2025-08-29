package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.service.CoverLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cover-letters/ai")
@RequiredArgsConstructor
public class CoverLetterController {
    private final CoverLetterService coverLetterService;
    // 자소서 CRUD API 구현 예정
}
