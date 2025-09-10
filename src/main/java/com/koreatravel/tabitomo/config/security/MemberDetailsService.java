package com.koreatravel.tabitomo.config.security;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Custom UserDetailsService implementation for loading user details during authentication.
 * This service is used by Spring Security to load user details from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String USER_NOT_FOUND_MSG = "error.user.not.found";
    private static final String ACCOUNT_INACTIVE_MSG = "error.account.inactive";
    private static final String EMAIL_NOT_VERIFIED_MSG = "error.email.not.verified";
    private static final String DATABASE_ERROR_MSG = "error.database.access";

    /**
     * Load user details by email address.
     *
     * @param email the email address of the user to load
     * @return UserDetails containing the user's information and authorities
     * @throws UsernameNotFoundException if the user is not found or cannot be authenticated
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        try {
            MemberEntity member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User not found: {}", email);
                        return new UsernameNotFoundException(USER_NOT_FOUND_MSG);
                    });
            
            if (!member.isActive()) {
                log.warn("Inactive user attempted login: {}", email);
                throw new UsernameNotFoundException(ACCOUNT_INACTIVE_MSG);
            }
            
            if (!member.isEmailVerified()) {
                log.warn("Unverified email attempted login: {}", email);
                throw new UsernameNotFoundException(EMAIL_NOT_VERIFIED_MSG);
            }
            
            log.debug("Successfully loaded user: {} (ID: {})", email, member.getId());
            return createUserDetails(member);
            
        } catch (DataAccessException e) {
            String errorMsg = String.format("Database error while loading user: %s", email);
            log.error(errorMsg, e);
            throw new UsernameNotFoundException(DATABASE_ERROR_MSG, e);
        }
    }
    
    /**
     * Creates a UserDetails object from a MemberEntity.
     *
     * @param member the member entity to convert
     * @return UserDetails implementation with the member's information
     */
    private UserDetails createUserDetails(MemberEntity member) {
        // Get user role from the enum, defaulting to ROLE_USER if not set
        String role = member.getRole() != null ? 
                member.getRole().name() :  // Get the enum name which includes ROLE_ prefix
                DEFAULT_ROLE;
                
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(role)
        );
        
        log.debug("Creating UserDetails for user: {} with role: {}", member.getEmail(), role);
        
        return new MemberDetails(
            member.getEmail(),
            member.getPassword(),
            member.isActive(),
            true, // accountNonExpired
            true, // credentialsNonExpired
            !member.isLocked(), // accountNonLocked
            authorities,
            member
        );
    }
}
