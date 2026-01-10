package com.example.demo.uitl;

import jakarta.servlet.http.HttpServletRequest;
import eu.bitwalker.useragentutils.UserAgent; // Thêm thư viện này vào pom.xml

public class RequestUtils {

    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-Forwarded-For");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    public static String getDeviceType(HttpServletRequest request) {
        String userAgentString = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
        return userAgent.getOperatingSystem().getName() + " - " + userAgent.getBrowser().getName();
    }
}