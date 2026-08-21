package com.credarc.credarc.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    public static String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(ip != null && !ip.isBlank()){
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
