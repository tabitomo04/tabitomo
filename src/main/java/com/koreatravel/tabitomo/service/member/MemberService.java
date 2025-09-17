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
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CountryRepository countryRepository;
    private final LanguageRepository languageRepository;
    
    @Value("${app.upload.dir:uploads/profiles}")
    private String uploadDir;
    
    private static final int MAX_ATTEMPTS = 5;  // 최대 시도 횟수
    
    /**
     * 모든 국가 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CountryDTO> getAllCountries() {
        List<CountryDTO> countries = countryRepository.findAll().stream()
                .map(country -> new CountryDTO(
                        country.getCountryId(),
                        country.getCountryCode(),  // 국가 코드 (예: "KR", "US")
                        country.getNameKr(),       // 한국어 이름
                        country.getNameEn()        // 영어 이름
                ))
                .collect(Collectors.toList());
        
        // Log the countries for debugging
        log.info("Loaded {} countries: {}", countries.size(), 
            countries.stream()
                .map(c -> c.getCountryId() + ":" + c.getCountryCode() + "-" + c.getCountryNameKo() + "(" + c.getCountryNameEn() + ")")
                .collect(Collectors.joining(", ")));
            
        return countries;
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
    
    public MemberEntity findById(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
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
    @Transactional(readOnly = true)
    public boolean isNicknameExists(String nickname) {
        try {
            if (nickname == null) {
                log.warn("Nickname is null");
                throw new IllegalArgumentException("닉네임을 입력해주세요.");
            }
            
            nickname = nickname.trim();
            log.info("Checking if nickname exists: {}", nickname);
            
            // Validate nickname length
            if (nickname.isEmpty()) {
                log.warn("Nickname is empty");
                throw new IllegalArgumentException("닉네임을 입력해주세요.");
            }
            
            if (nickname.length() > 50) {
                log.warn("Nickname too long: {} (length: {})", nickname, nickname.length());
                throw new IllegalArgumentException("닉네임은 50자 이내로 입력해주세요.");
            }
            
            log.debug("Checking if nickname exists: {}", nickname);
            try {
                boolean exists = memberRepository.existsByNickname(nickname);
                log.info("Nickname check completed - exists: {} for nickname: {}", exists, nickname);
                return exists;
            } catch (Exception e) {
                log.error("Database error when checking nickname: " + nickname, e);
                throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다.", e);
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error in isNicknameExists: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in isNicknameExists: " + e.getMessage(), e);
            throw new RuntimeException("닉네임 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
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
     * @return 업데이트된 회원 프로필 DTO
     * @throws IOException 파일 업로드 실패 시 발생
     */
    @Transactional
    public MemberProfileDTO updateProfile(UUID memberId, MemberProfileDTO profileDTO, MultipartFile profileImageFile) throws IOException {
        MemberEntity member = findById(memberId);
        
        if (profileDTO.getNickname() != null && !profileDTO.getNickname().isEmpty()) {
            member.setNickname(profileDTO.getNickname());
        }
        
        // 프로필 이미지 업데이트 (파일이 제공된 경우에만)
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            String newProfileImageUrl = saveProfileImage(profileImageFile);
            member.setProfileImageUrl(newProfileImageUrl);
        }
        
        // 업데이트된 회원 정보 저장
        MemberEntity updatedMember = memberRepository.save(member);
        
        return MemberProfileDTO.builder()
                .id(updatedMember.getId())
                .email(updatedMember.getEmail())
                .nickname(updatedMember.getNickname())
                .profileImageUrl(updatedMember.getProfileImageUrl())
                .role(updatedMember.getRole())
                .build();
    }
    
    /**
     * 회원 프로필 조회
     */
    /**
     * UUID를 생성하여 중복되지 않는 ID를 가진 회원을 저장합니다.
     * @param member 저장할 회원 엔티티
     * @return 저장된 회원 엔티티
     * @throws IllegalStateException 최대 시도 횟수 내에 고유한 ID를 생성하지 못한 경우
     */
    @Transactional
    public MemberEntity generateAndSaveMember(MemberEntity member) {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            // UUID 생성
            UUID uuid = UUID.randomUUID();
            
            // ID 중복 확인
            if (!memberRepository.existsById(uuid)) {
                member.setId(uuid);
                return memberRepository.save(member);
            }
            attempts++;
        }
        throw new IllegalStateException("고유한 ID를 생성하는 데 실패했습니다. 다시 시도해주세요.");
    }
    
    public MemberProfileDTO getMemberProfile(UUID memberId) {
        MemberEntity member = findById(memberId);
        
        return MemberProfileDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .role(member.getRole())
                // TODO: 나머지 필드 설정
                .build();
    }

}

