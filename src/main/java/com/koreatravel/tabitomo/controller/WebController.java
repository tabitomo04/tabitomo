package com.koreatravel.tabitomo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    private final ResourceLoader resourceLoader;

    @GetMapping("/")
    public String index() {
        log.debug("Accessing index page");
        return "index";
    }

    @GetMapping("/menu")
    public String menu() {
        log.debug("Accessing menu page");
        return "menu";
    }

    @GetMapping("/chat")
    public String chat() {
        log.debug("Accessing chat page");
        return "chat";
    }
    
    @GetMapping(value = "/image/logo.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> getLogo() {
        Resource resource = resourceLoader.getResource("classpath:static/image/logo.png");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=logo.png")
                .body(resource);
    }

}
