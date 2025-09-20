package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
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

    // 프로필 이미지 파일 확장자 허용 목록
    private static final List<String> ALLOWED_EXTENSIONS = List.of("png", "jpg", "jpeg", "gif");

    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final int MAX_ATTEMPTS = 5;  // 최대 시도 횟수

    // 프로필 이미지 저장 경로
    private final Path rootLocation = Paths.get("uploads/profile");

    @Transactional(readOnly = true)
    public List<MemberEntity> findAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MemberEntity findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + email));
    }

    @Transactional
    public void updateMemberByAdmin(String email, String nickname, String role, boolean isActive) {
        MemberEntity member = findMemberByEmail(email);
        member.setNickname(nickname);
        member.setRole(role);
        member.setActive(isActive);
        memberRepository.save(member);
    }

    @Transactional
    public void deleteMemberByAdmin(String email) {
        MemberEntity member = findMemberByEmail(email);
        memberRepository.delete(member);
    }

    @Transactional
    public void toggleUserActiveState(String email) {
        MemberEntity member = findMemberByEmail(email);
        member.setActive(!member.isActive());
        memberRepository.save(member);
    }

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
    /**
     * 프로필 정보 업데이트
     * @param memberId 업데이트할 회원 ID
     * @param profileDTO 업데이트할 프로필 정보가 담긴 DTO
     * @return 업데이트된 회원 프로필 DTO
     */
    @Transactional
    public MemberProfileDTO updateProfile(UUID memberId, MemberProfileDTO profileDTO) {
        // 회원 정보 조회
        MemberEntity member = findById(memberId);

        // 닉네임 업데이트 (중복 검사는 컨트롤러에서 처리)
        if (profileDTO.getNickname() != null && !profileDTO.getNickname().isBlank()) {
            member.setNickname(profileDTO.getNickname().trim());
        }

        // 개인 정보 업데이트
        if (profileDTO.getDateOfBirth() != null) {
            member.setDateOfBirth(profileDTO.getDateOfBirth());
        }

        if (profileDTO.getGender() != null) {
            member.setGender(profileDTO.getGender());
        }

        // 국가 정보 업데이트
        if (profileDTO.getCountryCode() != null && !profileDTO.getCountryCode().isBlank()) {
            try {
                // 국가 코드로 국가 조회 시도
                CountryEntity country = countryRepository.findByCountryCode(profileDTO.getCountryCode())
                    .orElseGet(() -> {
                        // 국가 코드로 찾지 못하면 한국어 이름으로 시도
                        return countryRepository.findByNameKr(profileDTO.getCountryCode())
                            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 국가 코드 또는 이름입니다: " + profileDTO.getCountryCode()));
                    });
                member.setCountry(country);

                // DTO에 국가 이름 설정
                profileDTO.setCountryName(country.getNameKr());
            } catch (Exception e) {
                log.error("국가 정보 업데이트 중 오류 발생: {}", e.getMessage(), e);
                throw new IllegalArgumentException("국가 정보를 업데이트하는 중 오류가 발생했습니다.", e);
            }
        }

        // 선호 언어 업데이트
        if (profileDTO.getPreferredLanguageId() != null) {
            try {
                LanguageEntity language = languageRepository.findById(profileDTO.getPreferredLanguageId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 언어 ID입니다: " + profileDTO.getPreferredLanguageId()));
                member.setPreferredLanguage(language);

                // DTO에 언어 이름 설정
                profileDTO.setPreferredLanguageName(language.getNameNative());
            } catch (Exception e) {
                log.error("선호 언어 업데이트 중 오류 발생: {}", e.getMessage(), e);
                throw new IllegalArgumentException("선호 언어를 업데이트하는 중 오류가 발생했습니다.", e);
            }
        }

        // 추가 정보 업데이트 (showGender, showAge 등)
        // MemberEntity에 해당 필드가 없으므로 주석 처리
        // if (profileDTO.getShowGender() != null) {
        //     member.setShowGender(profileDTO.getShowGender());
        // }
        //
        // if (profileDTO.getShowAge() != null) {
        //     member.setShowAge(profileDTO.getShowAge());
        // }

        // 엔티티 저장
        MemberEntity updatedMember = memberRepository.save(member);
        log.info("회원 프로필 업데이트 완료: {}", updatedMember.getId());

        // DTO로 변환하여 반환
        return convertToDTO(updatedMember);
    }

    /**
     * 프로필 이미지 업데이트
     */
    @Transactional
    public String updateProfileImage(UUID memberId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
            originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. PNG, JPG, JPEG, GIF 파일만 업로드 가능합니다.");
        }

        // 업로드 디렉토리 생성
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        // 새 파일명 생성 (UUID + 확장자)
        String newFilename = UUID.randomUUID() + "." + fileExtension;
        Path destinationFile = rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

        // 파일 저장
        file.transferTo(destinationFile);

        // 회원 프로필 이미지 URL 업데이트
        MemberEntity member = findById(memberId);
        String newImageUrl = "/uploads/profile/" + newFilename;
        member.setProfileImageUrl(newImageUrl);
        memberRepository.save(member);

        return newImageUrl;
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

    /**
     * 회원 프로필 조회
     */
    @Transactional(readOnly = true)
    public MemberProfileDTO getMemberProfile(UUID memberId) {
        MemberEntity member = findById(memberId);
        return convertToDTO(member);
    }

    /**
     * MemberEntity를 MemberProfileDTO로 변환
     */
    private MemberProfileDTO convertToDTO(MemberEntity member) {
        return MemberProfileDTO.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .profileImageUrl(member.getProfileImageUrl())
            // MemberEntity에 getIntroduction() 메서드가 없으므로 주석 처리
            // .introduction(member.getIntroduction())
            .role(member.getRole())
            .dateOfBirth(member.getDateOfBirth())
            .gender(member.getGender())
            .countryCode(member.getCountry() != null ? member.getCountry().getCountryCode() : null)
            .countryName(member.getCountry() != null ? member.getCountry().getNameKr() : null)
            .preferredLanguageId(member.getPreferredLanguage() != null ? member.getPreferredLanguage().getLanguageId() : null)
            .preferredLanguageName(member.getPreferredLanguage() != null ? member.getPreferredLanguage().getNameNative() : null)
            // MemberEntity에 해당 필드들이 없으므로 기본값으로 설정
            .showGender(true)  // 기본값으로 true 설정
            .showAge(true)     // 기본값으로 true 설정
            .isActive(member.isActive())
            .questionnaireCompleted(member.isQuestionnaireCompleted())
            .createdAt(member.getCreatedAt())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    /**
     * 닉네임 사용 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname, UUID currentUserId) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }

        // 현재 사용자의 닉네임과 동일한 경우 사용 가능
        if (currentUserId != null) {
            MemberEntity currentUser = memberRepository.findById(currentUserId).orElse(null);
            if (currentUser != null && nickname.equals(currentUser.getNickname())) {
                return true;
            }
        }

        // 다른 사용자가 사용 중인 닉네임인지 확인
        return !memberRepository.existsByNickname(nickname);
    }

}
