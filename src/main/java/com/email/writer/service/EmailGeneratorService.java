//package com.email.writer.service;
//
//
//import com.email.writer.model.EmailRequest;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//
//@Service
//public class EmailGeneratorService {
//
//    private final WebClient webClient;
//    private final String apiKey;
//
//    public EmailGeneratorService(WebClient.Builder webClientBuilder,
//                                @Value("${gemini.api.url}") String baseUrl,
//                                 @Value("${gemini.api.key}") String geminiApiKey) {
//        this.apiKey = geminiApiKey;
//        this.webClient = webClientBuilder.baseUrl(baseUrl)
//                .build();
//    }
//    public String generateEmailReply(EmailRequest emailRequest) {
//        String prompt = buildPrompt(emailRequest);
//        String requestBody = String.format("""
//                {
//                    "contents": [
//                      {
//                        "parts": [
//                          {
//                            "text": "%s"
//                          }
//                        ]
//                      }
//                    ]
//                  }
//                """ , prompt);
//
//        String response = webClient.post()
//                .uri(uriBuilder -> uriBuilder.path("/v1beta/models/gemini-3-flash-preview:generateContent")
//                        .build())
//                .header("x-goog-api-key", apiKey)
//                .header ( "Content-Type", "application/json")
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        return extractResponseContent(response);
//
//    }
//
//    private String extractResponseContent(String response) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode rootNode = mapper.readTree(response);
//            return rootNode.path("candidates")
//                    .get(0)
//                    .path("content")
//                    .path("parts")
//                    .get(0)
//                    .path("text")
//                    .asText();
//        } catch (Exception e) {
//            return "Error processing request: " + e.getMessage();
//        }
//    }
//
//
//    private String buildPrompt(EmailRequest emailRequest) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("Generate a professional email reply for the following email. ");
//        prompt.append("Return only the email body without any formatting symbols. ");
//        prompt.append("Keep it clean and plain text only. ");
//        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
//            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone. ");
//        }
//        prompt.append("\nOriginal Email:\n").append(emailRequest.getEmailContent());
//        return prompt.toString();
//    }
//}


package com.email.writer.service;

import com.email.writer.model.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final String apiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey) {

        this.apiKey = geminiApiKey;

        // 🔥 DEBUG (optional, check once)
        System.out.println("Base URL = " + baseUrl);
        System.out.println("API KEY = " + geminiApiKey);

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {

        String prompt = buildPrompt(emailRequest);

        // ✅ Safe JSON body (no formatting issues)
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String response = webClient.post()
                // ✅ ONLY WORKING MODEL (do not change)
                .uri("/v1beta/models/gemini-pro:generateContent")
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Write a professional email reply.\n");
        prompt.append("Keep it short, clear and polite.\n");

        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Tone: ").append(emailRequest.getTone()).append("\n");
        }

        prompt.append("\nOriginal Email:\n");
        prompt.append(emailRequest.getEmailContent());

        return prompt.toString();
    }
}