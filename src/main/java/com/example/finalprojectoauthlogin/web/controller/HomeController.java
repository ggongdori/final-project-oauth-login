package com.example.finalprojectoauthlogin.web.controller;

import com.example.finalprojectoauthlogin.domain.service.SignService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final SignService memberService;

    @ApiOperation(value = "테스트 페이지", notes = "인증을 위한 테스트 페이지입니다.")
    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

}
