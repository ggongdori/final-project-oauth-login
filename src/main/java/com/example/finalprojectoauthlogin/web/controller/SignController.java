package com.example.finalprojectoauthlogin.web.controller;

import com.example.finalprojectoauthlogin.domain.auth.AuthCode;
import com.example.finalprojectoauthlogin.domain.dto.MemberLoginResponseDto;
import com.example.finalprojectoauthlogin.domain.dto.MemberRegisterResponseDto;
import com.example.finalprojectoauthlogin.domain.dto.TokenResponseDto;
import com.example.finalprojectoauthlogin.domain.result.SingleResult;
import com.example.finalprojectoauthlogin.domain.service.ResponseService;
import com.example.finalprojectoauthlogin.domain.service.SignService;
import com.example.finalprojectoauthlogin.web.dto.EmailAuthRequestDto;
import com.example.finalprojectoauthlogin.web.dto.MemberLoginRequestDto;
import com.example.finalprojectoauthlogin.web.dto.MemberRegisterRequestDto;
import com.example.finalprojectoauthlogin.web.dto.ReIssueRequestDto;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sign")
public class SignController {

    private final SignService signService;
    private final ResponseService responseService;

    @ApiOperation(value = "회원가입", notes = "회원가입을 진행한다.")
    @PostMapping("/register")
    public SingleResult<MemberRegisterResponseDto> register(@RequestBody MemberRegisterRequestDto requestDto) {
        log.info("request = {}, {}", requestDto.getEmail(), requestDto.getPassword());
        MemberRegisterResponseDto responseDto = signService.registerMember(requestDto);
        return responseService.getSingleResult(responseDto);
    }

    @ApiOperation(value = "이메일 인증", notes = "이메일 인증을 진행한다.")
    @GetMapping("/confirm-email")
    public SingleResult<String> confirmEmail(@ModelAttribute EmailAuthRequestDto requestDto) {
        signService.confirmEmail(requestDto);
        return responseService.getSingleResult("인증이 완료되었습니다.");
    }

    @ApiOperation(value = "로컬 로그인", notes = "로컬을 통해 로그인을 진행한다.")
    @PostMapping("/login")
    public SingleResult<MemberLoginResponseDto> login(@RequestBody MemberLoginRequestDto requestDto) {
        MemberLoginResponseDto responseDto = signService.loginMember(requestDto);
        return responseService.getSingleResult(responseDto);
    }

    @ApiOperation(value = "소셜 로그인", notes = "소셜을 통해 로그인을 진행한다.")
    @PostMapping("/login/{provider}")
    public SingleResult<MemberLoginResponseDto> loginByKakao(@RequestBody AuthCode authCode, @PathVariable String provider) {
        MemberLoginResponseDto responseDto = signService.loginMemberByProvider(authCode.getCode(), provider);
        return responseService.getSingleResult(responseDto);
    }

    @ApiOperation(value = "토큰 재발급", notes = "Refresh Token을 통해 토큰을 재발급받는다.")
    @PostMapping("/reissue")
    public SingleResult<TokenResponseDto> reIssue(@RequestBody ReIssueRequestDto requestDto) {
        TokenResponseDto responseDto = signService.reIssue(requestDto);
        return responseService.getSingleResult(responseDto);
    }
}
