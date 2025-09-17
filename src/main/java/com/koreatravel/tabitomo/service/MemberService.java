package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // 추가된 import
import java.io.IOException; // 추가된 import
import java.nio.file.Files; // 추가된 import
import java.nio.file.Path; // 추가된 import
import java.nio.file.Paths; // 추가된 import
import java.util.UUID; // 추가된 import

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    // 프로필 이미지 저장 경로 (실제 운영 환경에 맞게 변경해야 함)
    private final String uploadDir = "uploads/profiles";

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원가입 처리 메서드
    public void register(MemberEntity member) {
        memberRepository.save(member);
    }

    /**
     * 닉네임 중복 확인
     * @param nickname 확인할 닉네임
     * @return 중복되면 true, 아니면 false
     */
    public boolean checkNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 로그인 처리 메서드
    public MemberEntity login(String memberId, String password) {
        MemberEntity member = memberRepository.findByEmail(memberId).orElse(null);
        if (member != null && member.getPassword().equals(password)) {
            return member;
        }
        return null;
    }

    // 회원 ID로 회원 정보 조회
    public MemberEntity getMemberById(String memberId) {
        return memberRepository.findByEmail(memberId).orElse(null);
    }

    /**
     * 회원 정보 업데이트
     * @param memberDTO 업데이트할 회원 정보 DTO
     * @param profileImageFile 프로필 이미지 파일
     * @throws IOException 파일 업로드 실패 시 발생
     */
    public void updateMember(MemberDTO memberDTO, MultipartFile profileImageFile) throws IOException {
        // 기존 회원 정보 가져오기
        MemberEntity existingMember = memberRepository.findByEmail(memberDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // DTO의 정보로 엔티티 업데이트
        if (memberDTO.getNickname() != null && !memberDTO.getNickname().isEmpty()) {
            existingMember.setNickname(memberDTO.getNickname());
        }
        if (memberDTO.getStatusMessage() != null) {
            existingMember.setStatusMessage(memberDTO.getStatusMessage());
        }
        if (memberDTO.getHashtags() != null) {
            existingMember.setHashtags(memberDTO.getHashtags());
        }

        // 프로필 이미지 업데이트 (파일이 존재하면)
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            String newProfileImagePath = saveProfileImage(profileImageFile);
            existingMember.setProfileImagePath(newProfileImagePath);
        }

        memberRepository.save(existingMember);
    }

    // 프로필 이미지 저장 메서드
    private String saveProfileImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") != -1 && originalFilename.lastIndexOf(".") != 0) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);
        return "/" + uploadDir + "/" + newFilename; // DB에 저장할 경로 반환
    }

    public MemberEntity getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}