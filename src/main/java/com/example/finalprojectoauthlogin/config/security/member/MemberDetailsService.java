package com.example.finalprojectoauthlogin.config.security.member;

import com.example.finalprojectoauthlogin.advice.exception.MemberNotFoundException;
import com.example.finalprojectoauthlogin.domain.entity.Member;
import com.example.finalprojectoauthlogin.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        return MemberDetails.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .authorities(member.getRoles().stream()
                                        .map(auth -> new SimpleGrantedAuthority(auth.toString()))
                                        .collect(Collectors.toList()))
                .build();
    }
}
