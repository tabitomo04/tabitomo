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
@RequestMapping("/storybook")
public class EditorController {


    @Autowired
    private EditorService editorService;

    // 에디터 페이지 열기
    @GetMapping("/editor")
    public String editor() {
        return "editor";
    }

    // 스토리북 작성 페이지 (리다이렉트용)
    @GetMapping("/write")
    public String write() {
        return "redirect:/storybook/editor";
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
     * @param model 해당 스토리북 제목+내용 및 좋아요 상태
     * @return 스토리북 출력 페이지
     */
    @GetMapping("/view")
    public String view(@RequestParam("booknum") Integer booknum, Model model) {
        StorybookDTO dto = editorService.getstory(booknum);
        boolean isLiked = editorService.isLikedByCurrentUser(booknum);
        
        model.addAttribute("post", dto);
        model.addAttribute("liked", isLiked);
        return "storybook";
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
    @GetMapping("/")
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

    /**
     * 좋아요 추가/제거
     * @param booknum 스토리북 번호
     * @return 처리 결과 및 현재 좋아요 수
     */
    @PostMapping("/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likePost(@RequestParam("booknum") Integer booknum) {
        Map<String, Object> response = new HashMap<>();
        try {
            int likeCount = editorService.addLike(booknum);
            response.put("success", true);
            response.put("likes", likeCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error liking post: " + booknum, e);
            response.put("success", false);
            response.put("message", "좋아요 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 좋아요 취소
     * @param booknum 스토리북 번호
     * @return 처리 결과 및 현재 좋아요 수
     */
    @PostMapping("/unlike")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlikePost(@RequestParam("booknum") Integer booknum) {
        Map<String, Object> response = new HashMap<>();
        try {
            int likeCount = editorService.removeLike(booknum);
            response.put("success", true);
            response.put("likes", likeCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error unliking post: " + booknum, e);
            response.put("success", false);
            response.put("message", "좋아요 취소 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 사용자의 좋아요 여부 확인
     * @param booknum 스토리북 번호
     * @return 좋아요 여부
     */
    @GetMapping("/check-like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkLike(@RequestParam("booknum") Integer booknum) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isLiked = editorService.isLikedByCurrentUser(booknum);
            response.put("liked", isLiked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking like status for post: " + booknum, e);
            response.put("liked", false);
            return ResponseEntity.ok(response);
        }
    }

}
