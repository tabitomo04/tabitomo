package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.koreatravel.tabitomo.service.trip.CitiesService;
import com.koreatravel.tabitomo.domain.dto.trip.CitiesDTO;

import org.springframework.ui.Model;

import com.koreatravel.tabitomo.service.storybook.EditorService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    private final ResourceLoader resourceLoader;

    private final CitiesService citiesService;

    private final EditorService editorService;

    @GetMapping("/")
    public String index(Model model) {
        log.debug("Accessing index page");
        List<CitiesDTO> cities = citiesService.getRecommendCities();
        model.addAttribute("cities", cities);
        List<StorybookListDTO> storybookList = editorService.getmainStory();
        model.addAttribute("storylist", storybookList);
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
