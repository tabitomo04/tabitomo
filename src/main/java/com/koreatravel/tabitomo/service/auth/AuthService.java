package com.koreatravel.tabitomo.service.auth;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberRegisterDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.exception.DuplicateEmailException;
import com.koreatravel.tabitomo.exception.DuplicateNicknameException;
import com.koreatravel.tabitomo.exception.LoginException;

import java.util.Optional;

/**
 * Authentication and authorization service interface.
 * Handles user registration, login, logout, and account management.
 */
public interface AuthService {
    /**
     * Register a new user account.
     *
     * @param registerDTO the registration data
     * @return the email of the registered user
     * @throws DuplicateEmailException if the email is already registered
     * @throws DuplicateNicknameException if the nickname is already in use
     */
    String register(MemberRegisterDTO registerDTO) throws DuplicateEmailException, DuplicateNicknameException;

    /**
     * Authenticate a user and create a new session.
     *
     * @param email the user's email
     * @param password the user's password
     * @return the authenticated user's profile
     * @throws LoginException if authentication fails
     */
    MemberProfileDTO login(String email, String password) throws LoginException;

    /**
     * Invalidate the current user's session.
     */
    void logout();

    /**
     * Get the currently authenticated user's profile.
     *
     * @return the user's profile if authenticated, empty otherwise
     */
    Optional<MemberProfileDTO> getCurrentUserProfile();

    /**
     * Verify a user's email address using a verification token.
     *
     * @param token the verification token
     * @return true if verification was successful, false otherwise
     */
    boolean verifyEmail(String token);

    /**
     * Initiate the password reset process by sending a reset email.
     *
     * @param email the email address of the account to reset
     * @return true if the reset email was sent, false otherwise
     */
    boolean sendPasswordResetEmail(String email);

    /**
     * Reset a user's password using a reset token.
     *
     * @param token the password reset token
     * @param newPassword the new password
     * @return true if the password was reset successfully, false otherwise
     */
    boolean resetPassword(String token, String newPassword);
    
    /**
     * Load a user by email for authentication purposes.
     *
     * @param email the user's email
     * @return the user entity if found
     */
    Optional<MemberEntity> loadUserByEmail(String email);
}
