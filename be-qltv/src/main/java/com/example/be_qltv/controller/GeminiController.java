package com.example.be_qltv.controller;

import com.example.be_qltv.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private static final Logger logger = LoggerFactory.getLogger(GeminiController.class);


    @Value("${gemini.api.key}")
    private String apiKey;

    // ✅ Khai báo client có timeout dài để tránh SocketTimeoutException
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(60))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> requestMap) {
        try {
            String message = requestMap.get("message");

            // ✅ Validate input
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Message không được để trống"));
            }

            // ✅ Escape quotes trong message
            String escapedMessage = message.replace("\"", "\\\"");

            // ✅ Tạo request body
            String requestBody = String.format(
                    "{\"contents\": [{\"parts\":[{\"text\":\"%s\"}]}]}",
                    escapedMessage
            );

            logger.info("🔹 Sending to Gemini API:");
            logger.info("   URL: {}", GEMINI_API_URL + "?key=" + apiKey.substring(0, 10) + "***");
            logger.info("   Request Body: {}", requestBody);

            // ✅ Gọi Gemini API
            Request request = new Request.Builder()
                    .url(GEMINI_API_URL + "?key=" + apiKey)
                    .post(okhttp3.RequestBody.create(requestBody, JSON))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                logger.info("   Response Code: {}", response.code());

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    logger.error("❌ Gemini API error: {} - {}", response.code(), errorBody);
                    return ResponseEntity.status(response.code())
                            .body(createErrorResponse("Gemini API error: " + response.message() + " - " + errorBody));
                }

                if (response.body() == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse("Empty response from Gemini API"));
                }

                // ✅ Parse JSON response và lấy text
                String responseBody = response.body().string();
                logger.info("   Response Body: {}", responseBody);

                String geminiText = extractTextFromResponse(responseBody);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", geminiText);

                return ResponseEntity.ok(result);
            }

        } catch (IOException e) {
            logger.error("❌ IOException: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("IOException: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    // ✅ Helper: Parse JSON response từ Gemini API
    private String extractTextFromResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);

        // Gemini API response structure:
        // {"candidates": [{"content": {"parts": [{"text": "..."}]}}]}
        if (root.has("candidates") && root.get("candidates").isArray() && root.get("candidates").size() > 0) {
            JsonNode firstCandidate = root.get("candidates").get(0);
            if (firstCandidate.has("content") && firstCandidate.get("content").has("parts")) {
                JsonNode parts = firstCandidate.get("content").get("parts");
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode textNode = parts.get(0).get("text");
                    if (textNode != null) {
                        return textNode.asText();
                    }
                }
            }
        }

        throw new Exception("Invalid response format from Gemini API");
    }

    // ✅ Helper: Tạo error response
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", errorMessage);
        return error;
    }
}