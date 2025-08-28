package com.cvmento.global.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final String domain;
    private final boolean secure;
    private final String sameSite;

    public CookieUtil(@Value("${cookie.domain}") String domain,
                      @Value("${cookie.secure}") boolean secure,
                      @Value("${cookie.same-site}") String sameSite) {
        this.domain = domain;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public void addAccessTokenCookie(HttpServletResponse response, String token, Duration maxAge) {
        addCookie(response, ACCESS_TOKEN_COOKIE_NAME, token, maxAge);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token, Duration maxAge) {
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, token, maxAge);
    }

    private void addCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setDomain(domain);
        cookie.setMaxAge((int) maxAge.getSeconds());

        // SameSite 속성 설정을 위해 Set-Cookie 헤더 직접 설정
        String cookieHeader = String.format("%s=%s; Path=/; Domain=%s; Max-Age=%d; HttpOnly; SameSite=%s%s",
                name, value, domain, maxAge.getSeconds(), sameSite, secure ? "; Secure" : "");

        response.addHeader("Set-Cookie", cookieHeader);
    }

    public Optional<String> getAccessTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    public Optional<String> getRefreshTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE_NAME);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_COOKIE_NAME);
    }

    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setDomain(domain);
        cookie.setMaxAge(0);

        // SameSite 속성 설정을 위해 Set-Cookie 헤더 직접 설정
        String cookieHeader = String.format("%s=; Path=/; Domain=%s; Max-Age=0; HttpOnly; SameSite=%s%s",
                cookieName, domain, sameSite, secure ? "; Secure" : "");

        response.addHeader("Set-Cookie", cookieHeader);
    }

    public void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }
}