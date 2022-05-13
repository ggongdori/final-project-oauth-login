package com.example.finalprojectoauthlogin.domain.service;

import com.example.finalprojectoauthlogin.advice.exception.CommunicationException;
import com.example.finalprojectoauthlogin.domain.auth.AccessToken;
import com.example.finalprojectoauthlogin.domain.auth.OAuthRequest.OAuthRequest;
import com.example.finalprojectoauthlogin.domain.auth.OAuthRequest.OAuthRequestFactory;
import com.example.finalprojectoauthlogin.domain.auth.Profile.GoogleProfile;
import com.example.finalprojectoauthlogin.domain.auth.Profile.KakaoProfile;
import com.example.finalprojectoauthlogin.domain.auth.Profile.NaverProfile;
import com.example.finalprojectoauthlogin.domain.auth.Profile.ProfileDto;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {

    private final RestTemplate restTemplate;
    private final Gson gson;
    private final OAuthRequestFactory oAuthRequestFactory;

    public ProfileDto getProfile(String accessToken, String provider) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        String profileUrl = oAuthRequestFactory.getProfileUrl(provider);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(profileUrl, request, String.class);

        try {
            if (response.getStatusCode() == HttpStatus.OK) {
                return extractProfile(response, provider);
            }
        } catch (Exception e) {
            throw new CommunicationException();
        }
        throw new CommunicationException();
    }

    private ProfileDto extractProfile(ResponseEntity<String> response, String provider) {
        if (provider.equals("kakao")) {
            KakaoProfile kakaoProfile = gson.fromJson(response.getBody(), KakaoProfile.class);
            return new ProfileDto(kakaoProfile.getKakao_account().getEmail());
        } else if(provider.equals("google")) {
            GoogleProfile googleProfile = gson.fromJson(response.getBody(), GoogleProfile.class);
            return new ProfileDto(googleProfile.getEmail());
        } else {
            NaverProfile naverProfile = gson.fromJson(response.getBody(), NaverProfile.class);
            return new ProfileDto(naverProfile.getResponse().getEmail());
        }
    }

    public AccessToken getAccessToken(String code, String provider) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        OAuthRequest oAuthRequest = oAuthRequestFactory.getRequest(code, provider);
        HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(oAuthRequest.getMap(), httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(oAuthRequest.getUrl(), request, String.class);
        try {
            if (response.getStatusCode() == HttpStatus.OK) {
                return gson.fromJson(response.getBody(), AccessToken.class);
            }
        } catch (Exception e) {
            throw new CommunicationException();
        }
        throw new CommunicationException();
    }
}
