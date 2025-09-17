package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.koreatravel.tabitomo.domain.entity.member.MemberId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CountryRepository countryRepository;
    private final LanguageRepository languageRepository;
    
    @Value("${app.upload.dir:uploads/profiles}")
    private String uploadDir;
    
    /**
     * 모든 국가 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CountryDTO> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(country -> new CountryDTO(
                        country.getCountryId(),
                        country.getCountryName(),  // Using the single name field for both KO and EN
                        country.getCountryName()   // Using the same name for both KO and EN
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * 모든 언어 목록 조회
     */
    @Transactional(readOnly = true)
    public List<LanguageDTO> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(language -> new LanguageDTO(
                        language.getLanguageId(),
                        language.getNameNative(),
                        language.getNameEn()
                ))
                .collect(Collectors.toList());
    }
    
    public MemberEntity findById(MemberId id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id.getId()));
    }
    
    public MemberEntity findById(Long id) {
        return findById(new MemberId(id));
    }

    public MemberEntity findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + email));
    }

    @Transactional
    public MemberEntity updateMember(MemberEntity member) {
        return memberRepository.save(member);
    }

    
    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 이미 존재하는 이메일이면 true, 아니면 false
     */
    public boolean isEmailExists(String email) {
        return memberRepository.existsByEmail(email);
    }
    
    /**
     * 닉네임 중복 확인
     * @param nickname 확인할 닉네임
     * @return 이미 존재하는 닉네임이면 true, 아니면 false
     */
    public boolean isNicknameExists(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
    
    /**
     * 프로필 이미지 저장
     * @param file 업로드된 이미지 파일
     * @return 저장된 파일 경로
     * @throws IOException 파일 저장 실패 시 발생
     */
    public String saveProfileImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);
        
        return "/" + uploadDir + "/" + newFilename;
    }
    
    /**
     * 회원 프로필 업데이트
     * @param memberId 회원 ID
     * @param profileDTO 업데이트할 프로필 정보
     * @param profileImageFile 프로필 이미지 파일 (선택사항)
     * @return 업데이트된 회원 엔티티
     * @throws IOException 파일 업로드 실패 시 발생
     */
    /**
     * 회원 프로필 업데이트
     * @param memberId 회원 ID
     * @param profileDTO 업데이트할 프로필 정보
     * @param profileImageFile 프로필 이미지 파일 (선택사항)
     * @return 업데이트된 회원 엔티티
     * @throws IOException 파일 업로드 실패 시 발생
     */
    @Transactional
    public MemberEntity updateProfile(Long memberId, MemberProfileDTO profileDTO, MultipartFile profileImageFile) throws IOException {
        MemberEntity member = findById(memberId);
        
        if (profileDTO.getNickname() != null && !profileDTO.getNickname().isEmpty()) {
            member.setNickname(profileDTO.getNickname());
        }
        
        // 상태 메시지 업데이트 (null이 아닌 경우에만 업데이트)
        if (profileDTO.getStatusMessage() != null) {
            member.setStatusMessage(profileDTO.getStatusMessage());
        }
        
        // 프로필 이미지 업데이트 (파일이 제공된 경우에만)
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            String newProfileImageUrl = saveProfileImage(profileImageFile);
            member.setProfileImageUrl(newProfileImageUrl);
        }
        
        return memberRepository.save(member);
    }
    
    /**
     * 회원 프로필 조회
     */
    public MemberProfileDTO getMemberProfile(String memberId) {
        MemberEntity member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        
        return MemberProfileDTO.builder()
                .id(member.getId().getId()) // Get the actual Long ID from MemberId
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .statusMessage(member.getStatusMessage())
                .role(member.getRole())
                // TODO: 나머지 필드 설정
                .build();
    }

}

