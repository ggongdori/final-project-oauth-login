package com.example.finalprojectoauthlogin.domain.auth.Profile;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class KakaoOAuth2UserProfile extends OAuth2UserInfo{

    /** 카카오는 Integer 로 받아져서 그런지 (String) 또는 (Long) 으로 cascading 이 되지 않는다... 그래서 Integer 로 받아준다 */
    private Integer id;

    public KakaoOAuth2UserProfile(Map<String, Object> attributes) {

        super((Map<String, Object>) attributes.get("kakao_account"));
        this.id = (Integer) attributes.get("id");
    }

    @Override
    public String getId() {
        return null;
//        return this.id.toString();
    }

    @Override
    public String getName() {
        return (String) ((Map<String, Object>) attributes.get("profile")).get("nickname");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) ((Map<String, Object>) attributes.get("profile")).get("thumbnail_image_url");
    }
}
