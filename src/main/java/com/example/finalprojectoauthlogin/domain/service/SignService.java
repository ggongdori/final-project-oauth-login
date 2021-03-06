package com.example.finalprojectoauthlogin.domain.service;

import com.example.finalprojectoauthlogin.advice.exception.*;
import com.example.finalprojectoauthlogin.config.security.jwt.JwtTokenProvider;
import com.example.finalprojectoauthlogin.domain.auth.AccessToken;
import com.example.finalprojectoauthlogin.domain.auth.Profile.ProfileDto;
import com.example.finalprojectoauthlogin.domain.dto.TokenResponseDto;
import com.example.finalprojectoauthlogin.domain.entity.Member;
import com.example.finalprojectoauthlogin.domain.rediskey.RedisKey;
import com.example.finalprojectoauthlogin.domain.repository.MemberRepository;
import com.example.finalprojectoauthlogin.domain.dto.MemberLoginResponseDto;
import com.example.finalprojectoauthlogin.domain.dto.MemberRegisterResponseDto;
import com.example.finalprojectoauthlogin.web.dto.EmailAuthRequestDto;
import com.example.finalprojectoauthlogin.web.dto.MemberLoginRequestDto;
import com.example.finalprojectoauthlogin.web.dto.MemberRegisterRequestDto;
import com.example.finalprojectoauthlogin.web.dto.ReIssueRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SignService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    private final RedisService redisService;
    private final RedisTemplate redisTemplate;
    private final ProviderService providerService;
    private final EmailService emailService;



    /**
     * Dto??? ????????? ?????? ?????? ??????????????? ??????
     * @param requestDto
     * @return
     */
    @Transactional
    public MemberRegisterResponseDto registerMember(MemberRegisterRequestDto requestDto) {
        validateDuplicated(requestDto.getEmail());

        String authToken = UUID.randomUUID().toString();
        redisService.setDataWithExpiration(RedisKey.EAUTH.getKey()+requestDto.getEmail(), authToken, 60*5L);

        Member member = memberRepository.save(
                Member.builder()
                        .email(requestDto.getEmail())
                        .password(passwordEncoder.encode(requestDto.getPassword()))
                        .provider(null)
//                        .emailAuth(false)
                        .build());

        emailService.send(requestDto.getEmail(), authToken);
        return MemberRegisterResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .build();
    }

    public void validateDuplicated(String email) {
        if (memberRepository.findByEmail(email).isPresent())
            throw new MemberEmailAlreadyExistsException();
    }

    /**
     * ????????? ?????? ??????
     * @param requestDto
     */
    @Transactional
    public void confirmEmail(EmailAuthRequestDto requestDto) {
        if (redisService.getData(RedisKey.EAUTH.getKey()+requestDto.getEmail()) == null)
            throw new EmailAuthTokenNotFountException();

        Member member = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(MemberNotFoundException::new);
        redisService.deleteData(RedisKey.EAUTH.getKey()+requestDto.getEmail());
        member.emailVerifiedSuccess();
    }

    /**
     * ?????? ????????? ??????
     * @param requestDto
     * @return
     */
    @Transactional
    public MemberLoginResponseDto loginMember(MemberLoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(MemberNotFoundException::new);
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword()))
            throw new LoginFailureException();
        if (!member.getEmailAuth())
            throw new EmailNotAuthenticatedException();

        String refreshToken = jwtTokenProvider.createRefreshToken();
        redisService.setDataWithExpiration(RedisKey.REFRESH.getKey()+member.getEmail(), refreshToken, JwtTokenProvider.REFRESH_TOKEN_VALID_TIME);
        return new MemberLoginResponseDto(member.getId(), jwtTokenProvider.createToken(requestDto.getEmail()), refreshToken);
    }

    /**
     * ?????? ????????? ??????
     * @param code
     * @param provider
     * @return
     */
    @Transactional
    public MemberLoginResponseDto loginMemberByProvider(String code, String provider) {
        AccessToken accessToken = providerService.getAccessToken(code, provider);
        ProfileDto profile = providerService.getProfile(accessToken.getAccess_token(), provider);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        redisService.setDataWithExpiration(RedisKey.REFRESH.getKey()+refreshToken, refreshToken, JwtTokenProvider.REFRESH_TOKEN_VALID_TIME);

        Optional<Member> findMember = memberRepository.findByEmailAndProvider(profile.getEmail(), provider);
        if (findMember.isPresent()) {
            Member member = findMember.get();
            return new MemberLoginResponseDto(member.getId(), jwtTokenProvider.createToken(findMember.get().getEmail()), refreshToken);
        } else {
            Member saveMember = saveMember(profile, provider);
            return new MemberLoginResponseDto(saveMember.getId(), jwtTokenProvider.createToken(saveMember.getEmail()), refreshToken);
        }
    }

    private Member saveMember(ProfileDto profile, String provider) {
        Member member = Member.builder()
                .email(profile.getEmail())
                .password(null)
                .provider(provider)
                .build();
        Member saveMember = memberRepository.save(member);
        return saveMember;
    }

    /**
     * ?????? ?????????
     * @param requestDto
     * @return
     */
    @Transactional
    public TokenResponseDto reIssue(ReIssueRequestDto requestDto) {
        String findRefreshToken = redisService.getData(RedisKey.REFRESH.getKey()+requestDto.getEmail());
        if (findRefreshToken == null || !findRefreshToken.equals(requestDto.getRefreshToken()))
            throw new InvalidRefreshTokenException();

        Member member = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(MemberNotFoundException::new);
        String accessToken = jwtTokenProvider.createToken(member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken();
        redisService.setDataWithExpiration(RedisKey.REFRESH.getKey()+member.getEmail(), refreshToken, JwtTokenProvider.REFRESH_TOKEN_VALID_TIME);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public ResponseEntity logout(String token) {
        ValueOperations<String, String> logoutValueOps = redisTemplate.opsForValue();
        logoutValueOps.set(token, token);
        User user = (User) jwtTokenProvider.getAuthentication(token).getPrincipal();
        log.info("???????????? ?????? ????????? : '{}', ?????? ??????: '{}' ", user.getUsername(), user.getUsername());
        return new ResponseEntity(HttpStatus.OK);
    }

}
