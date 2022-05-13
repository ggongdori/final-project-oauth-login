package com.example.finalprojectoauthlogin.config.security;

import com.example.finalprojectoauthlogin.config.security.AccessDeniedHandler.CustomAccessDeniedHandler;
import com.example.finalprojectoauthlogin.config.security.AuthenticationEntryPoint.CustomAuthenticationEntryPoint;
import com.example.finalprojectoauthlogin.config.security.jwt.JwtAuthenticationFilter;
import com.example.finalprojectoauthlogin.config.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configure(WebSecurity web)  {
        web.ignoring()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .antMatchers(
                        "/v2/api-docs",
                        "/h2-console/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/configuration/ui",
                        "/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic().disable() // rest api이므로 기본설정 미사용
            .csrf().disable() // rest api이므로 csrf 보안 미사용
            .formLogin().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt로 인증하므로 세션 미사용
            .and()
                .authorizeRequests()
                .antMatchers("/sign/**").permitAll()
                .antMatchers("/social/**").permitAll()
                .antMatchers("/exception/**").permitAll()
                .antMatchers("/confirm-email").permitAll()
                .antMatchers("/test").permitAll()
                .antMatchers("/login/oauth2/code/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            .and()
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            .and()
                .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
            .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // jwt 필터 추가
    }
}
