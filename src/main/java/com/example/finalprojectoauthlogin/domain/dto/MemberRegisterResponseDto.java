package com.example.finalprojectoauthlogin.domain.dto;

import io.swagger.annotations.ApiParam;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberRegisterResponseDto {
    @ApiParam(value = "로그인 PK", required = true)
    private Long id;
    @ApiParam(value = "로그인 아이디", required = true)
    private String email;

    @Builder
    public MemberRegisterResponseDto(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}
