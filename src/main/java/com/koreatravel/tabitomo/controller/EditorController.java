package com.koreatravel.tabitomo.controller;


import com.koreatravel.tabitomo.domain.dto.storybook.SaveRequestDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.trip.TempsaveDTO;
import com.koreatravel.tabitomo.service.storybook.EditorService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class EditorController {


    @Autowired
    private EditorService editorService;

    // 에디터 페이지 열기
    @GetMapping("/editor")
    public String editor() {
        return "editor";
    }


    /**
     * 저장 컨트롤러 (새 스토리북 저장, 게시글 수정 후 저장, 임시저장 글 저장)
     * @param saveRequestDTO 저장하고자 하는 내용
     * @return
     */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> save(@RequestBody SaveRequestDTO saveRequestDTO) {
        Map<String,Object> response = new HashMap<>();
        try {
            Integer booknum;

            if ("temp".equals(saveRequestDTO.getSavetype())) {
                // 임시저장 → 최종 저장
                booknum = editorService.saveTempAsPost(saveRequestDTO);
            } else {
                // 기존 글 수정 또는 새 글 저장
                booknum = editorService.savePost(saveRequestDTO);
            }

            // 미디어 저장
            editorService.saveMedia(booknum, saveRequestDTO.getContent());

            response.put("status", "success");
            response.put("booknum", booknum);
            return ResponseEntity.ok(response);

        } catch(Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "게시글 저장에 실패했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 저장된 해당 스토리북 출력
     * @param booknum 해당 스토리북 넘버
     * @param model 해당 스토리북 제목+내용
     * @return 스토리북 출력 페이지
     */
    @GetMapping("/view")
    public String view(@RequestParam("booknum") Integer booknum, Model model) {
        StorybookDTO dto = editorService.getstory(booknum);
        model.addAttribute("post",dto);
        return "storyview";
    }


    @PostMapping("/tempsave")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> tempsave(@RequestBody SaveRequestDTO saveRequestDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 스토리북 저장
            Integer tempId = editorService.tempsave(saveRequestDTO);

            // 미디어 테이블에 저장
            editorService.tempsavemedia(tempId, saveRequestDTO.getContent());
            response.put("status", "success");
            response.put("tempId", tempId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 스토리북 리스트 불러오기
     * @param model
     * @return
     */
    @GetMapping("/storylist")
    public String storylist(Model model){

        List<StorybookListDTO> storybookList = editorService.getStorybookList();
        model.addAttribute("storylist", storybookList);
        return "storylist";

    }

    /**
     * 수정 페이지 이동
     * @param booknum 해당 스토리북 넘버
     * @param model 해당 스토리북 제목+내용
     * @return 수정 페이지
     */
    @GetMapping("/edit")
    public String edit(@RequestParam(value = "booknum", required = false) Integer booknum,
                       @RequestParam(value = "tempId", required = false) Integer tempId,
                       Model model) {
        if (booknum != null) {
            StorybookDTO dto = editorService.getstory(booknum);
            if (dto != null) model.addAttribute("post", dto);
        }

        if (tempId != null) {
            TempsaveDTO tempdto = editorService.gettemp(tempId);
            if (tempdto != null) model.addAttribute("temp", tempdto);
        }

        return "edit";
    }


    /**
     * 게시글 삭제
     * @param booknum 삭제하고자 하는 스토리북 넘버
     * @return 스토리북 리스트
     */
    @GetMapping("/delete")
    public String delete(@RequestParam("booknum") Integer booknum){
        editorService.delete(booknum);
        return "redirect:/storylist";
    }

    @GetMapping("/tempdel")
    public String tempdel(@RequestParam("tempId") Integer tempId){
        editorService.tempdel(tempId);
        return "redirect:/editor";
    }

    /**
     * 에디터 마이페이지 불러오기
     * @return 에디터 마이페이지
     */
    @GetMapping("/editor/mypage")
    public String editorMypage(Model model){
        List<StorybookListDTO> storybookList = editorService.getStorybookList();
        model.addAttribute("storylist", storybookList.stream().limit(3).toList());
        return "editor-mypage";
    }





}
