package com.koreatravel.tabitomo.controller.storybook;


import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.storybook.SaveRequestDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.service.storybook.EditorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class EditorController {


    @Autowired
    private EditorService editorService;

    // 에디터 페이지 열기
    @GetMapping(PathConstants.STORYBOOK_WRITE)
    public String editor() {
        return "editor";
    }


    /**
     * 저장 컨트롤러 (새 스토리북 저장, 게시글 수정 후 저장, 임시저장 글 저장)
     * @param saveRequestDTO 저장하고자 하는 내용
     * @return
     */
    @PostMapping(PathConstants.STORYBOOK_SAVE)
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
    @GetMapping(PathConstants.STORYBOOK_DETAIL)
    public String view(@RequestParam("booknum") Integer booknum, Model model) {
        StorybookDTO dto = editorService.getstory(booknum);
        model.addAttribute("post",dto);
        return "storyview";
    }

    /**
     * 임시저장본 저장
     * @param saveRequestDTO 임시저장하고자 하는 내용
     * @return
     */
    @PostMapping(PathConstants.STORYBOOK_TEMPSAVE)
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
    @GetMapping(PathConstants.STORYBOOK_LIST)
    public String storylist(Model model,
                            @RequestParam(defaultValue = "random") String sort,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "6") int size){

        if(sort == null) sort = "random";

        if("random".equals(sort)) {
            List<StorybookListDTO> storybookList = editorService.getStorybookList(sort);
            model.addAttribute("storylist", storybookList);
            model.addAttribute("pagelist", null);
        }
        else {
            Page<StorybookListDTO> hotORnewList = editorService.gethotORnewList(sort, page, size);
            model.addAttribute("pagelist", hotORnewList.getContent());
            model.addAttribute("sort", sort);
            //페이징
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", hotORnewList.getTotalPages());
            model.addAttribute("totalElement", hotORnewList.getTotalElements());
            model.addAttribute("hasNext", hotORnewList.hasNext());
            model.addAttribute("hasPrevious", hotORnewList.hasPrevious());

        }
        return "storylist";

    }

    /**
     * 수정 페이지 이동
     * @param booknum 해당 스토리북 넘버
     * @param model 해당 스토리북 제목+내용
     * @return 수정 페이지
     */
    @GetMapping(PathConstants.STORYBOOK_UPDATE)
    public String edit(@RequestParam(value = "booknum", required = false) Integer booknum,
                       @RequestParam(value = "tempId", required = false) Integer tempId,
                       Model model) {
        // 저장된 것 수정
        if (booknum != null) {
            StorybookDTO dto = editorService.getstory(booknum);
            if (dto != null) model.addAttribute("post", dto);
        }

        // 임시저장본 수정
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
    @GetMapping(PathConstants.STORYBOOK_DELETE)
    public String delete(@RequestParam("booknum") Integer booknum){
        editorService.delete(booknum);
        return "redirect:/storybook/list";
    }

    @GetMapping(PathConstants.STORYBOOK_TEMPDELETE)
    public String tempdel(@RequestParam("tempId") Integer tempId){
        editorService.tempdel(tempId);
        return "redirect:/mypage";
    }

    /**
     * 마이페이지 불러오기
     * @return 마이페이지
     */
    @GetMapping("/mypage")
    public String mypage(Model model,@RequestParam(defaultValue = "false") boolean all){

        // 스토리북 리스트
        List<StorybookListDTO> storybookList = editorService.getMyStorybookList();
        if (!all) {
            storybookList = storybookList.stream().limit(3).toList();
        }
        model.addAttribute("storylist", storybookList);
        model.addAttribute("all", all);

        // 임시저장 리스트
        List<TempsaveDTO> tempsaveList = editorService.getTempsaveList();
        model.addAttribute("templist",tempsaveList);
        return "mypage";
    }


    /**
     *  좋아요 토글 on
     * @param booknum
     * @AuthenticationPrincipal UserDetailsImpl userDetails
     * @return
     */
    @PostMapping("/like")
    public ResponseEntity<Map<String, Object>> like(
            @RequestParam Integer booknum,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int likes = editorService.likeBook(booknum, userDetails.getId());
        return ResponseEntity.ok(Map.of("likes", likes, "liked", true));
    }

    /**
     * 좋아요 토글 off
     * @param booknum
     * @AuthenticationPrincipal UserDetailsImpl userDetails
     * @return
     */
    @PostMapping("/unlike")
    public ResponseEntity<Map<String, Object>> unlike(
            @RequestParam Integer booknum,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int likes = editorService.unlikeBook(booknum, userDetails.getId());
        return ResponseEntity.ok(Map.of("likes", likes, "liked", false));
    }

    /**
     * 이메일과 booknum이 좋아요테이블에 있는지 확인(좋아요 한 적이 있으면 좋아요 상태 유지를 위해)
     * @param booknum
     * @AuthenticationPrincipal UserDetailsImpl userDetails
     * @return
     */
    @GetMapping("/isLiked")
    public ResponseEntity<Map<String, Object>> isLiked(
            @RequestParam Integer booknum,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean liked = editorService.isLiked(booknum, userDetails.getId());
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @ResponseBody
    @GetMapping("/storybook/getTagifyList")
    public List<String> getTagifyList(String value) {
        List<String> taglist = editorService.gettaglist();
        return taglist;
    }
    
}
