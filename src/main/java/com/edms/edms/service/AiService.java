package com.edms.edms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;

    public AiService(
            WebClient.Builder builder,
            @Value("${team5.rag.api.url}") String baseUrl
    ) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    // ============================================
    // 1️⃣ INDEX DOCUMENT (Send file to RAG API)
    // ============================================

    public String indexDocument(Long documentId, File file) {

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("documentId", documentId.toString());
            bodyBuilder.part("file", new FileSystemResource(file));

            MultiValueMap<String, org.springframework.http.HttpEntity<?>> multipartBody =
                    bodyBuilder.build();

            return webClient.post()
                    .uri("/index-document")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error indexing document: " + e.getMessage();
        }
    }


    // ============================================
    // 2️⃣ ASK QUESTION
    // ============================================

    public String askQuestion(Long documentId, String question) {

        try {
            Map<String, Object> requestBody = Map.of(
                    "question", question,
                    "documentId", documentId
            );

            return webClient.post()
                    .uri("/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error asking AI: " + e.getMessage();
        }
    }
}