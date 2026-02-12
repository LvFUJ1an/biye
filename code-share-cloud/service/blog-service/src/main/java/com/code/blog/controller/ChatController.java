package com.code.blog.controller;

import com.code.blog.manager.AiManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author lfj
 * @createDate 2025/11/30
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Resource
    private AiManager aiManager;

    @Value("${ai.url}")
    private String url;

    @Value("${ai.apiKey}")
    private String apiKey;

    @Resource
    private WebClient webClient;


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamResponse(String userMessage) throws InterruptedException {
        return doRequest(userMessage);
    }

    private Flux<String> doRequest(String userMessage) throws InterruptedException {
        return webClient.post()
                .uri(url)
                .header("Accept", "text/event-stream")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey )
                .bodyValue("{\n" +
                        "    \"model\": \"glm-4\",\n" +
                        "    \"messages\": [\n" +
                        "        {\n" +
                        "            \"role\": \"user\",\n" +
                        "            \"content\": \""+userMessage+"\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "            \"stream\" : \"true\"\n" +
                        "}")
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(e-> log.error("异常:",e));
    }
}
