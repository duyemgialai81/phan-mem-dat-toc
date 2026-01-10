package com.example.demo.core.login.service;

import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class LocationService {
    public static String getLocation(String ip) {
        try {
            if (ip.equals("127.0.0.1") || ip.startsWith("192.168")) return "Localhost";

            RestTemplate restTemplate = new RestTemplate();
            String url = "http://ip-api.com/json/" + ip;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                return response.get("city") + ", " + response.get("country");
            }
        } catch (Exception e) {
            return "Unknown Location";
        }
        return "Unknown";
    }
}
