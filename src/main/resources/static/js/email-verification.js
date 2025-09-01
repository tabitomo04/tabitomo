// Email Verification Module
class EmailVerification {
    constructor() {
        this.verificationCode = '';
        this.countdownInterval = null;
        this.timeLeft = 180; // 3 minutes in seconds
        
        // Initialize event listeners
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Email input validation
        $(document).on('input', '#email', this.handleEmailInput.bind(this));
        
        // Form submission
        $(document).on('submit', '#registerForm', this.handleFormSubmit.bind(this));
        
        // Password validation
        $(document).on('input', '#password', this.validatePassword.bind(this));
    }

    handleEmailInput() {
        const email = $(this).val().trim();
        const $sendBtn = $('#sendVerificationBtn');
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const isValid = emailRegex.test(email);

        $sendBtn.prop('disabled', !isValid);
        
        // Show/hide error message
        if (email === '') {
            $('#emailError').hide();
        } else if (!isValid) {
            this.showError('emailError', '유효한 이메일 주소를 입력해주세요.');
        } else {
            $('#emailError').hide();
        }
        
        // Reset verification if email changes
        if ($('#emailVerified').val() === 'true') {
            this.resetVerification();
        }
    }

    // Send verification code
    sendVerificationCode() {
        const email = $('#email').val().trim();
        const $btn = $('#sendVerificationBtn');
        
        // Show loading state
        $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 전송 중...');

        // Call API to send verification code
        $.ajax({
            url: '/api/email/send-verification',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ email: email }),
            success: (response) => {
                // Store the verification code
                this.verificationCode = response.verificationCode || '';
                
                // Show verification section
                $('#verificationSection').slideDown();
                $('#verificationCode').prop('disabled', false).focus();
                $('#verifyBtn').prop('disabled', false);
                
                // Start countdown
                this.startCountdown();
                
                // Show success message
                this.showMessage('인증번호가 전송되었습니다. 이메일을 확인해주세요.', 'success');
            },
            error: (xhr) => {
                const errorMsg = xhr.responseJSON?.message || '인증번호 전송에 실패했습니다.';
                this.showError('emailError', errorMsg);
            },
            complete: () => {
                $btn.prop('disabled', false).text('인증번호 재전송');
            }
        });
    }

    // Verify code
    verifyCode() {
        const inputCode = $('#verificationCode').val().trim();
        const $btn = $('#verifyBtn');
        
        if (!inputCode) {
            this.showError('verificationMessage', '인증번호를 입력해주세요.');
            return;
        }
        
        // Show loading state
        $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 확인 중...');
        
        // In a real app, you would verify the code with your backend
        // For demo purposes, we'll just check if it matches the sent code
        setTimeout(() => {
            if (inputCode === this.verificationCode) {
                // Verification successful
                clearInterval(this.countdownInterval);
                
                // Update UI for success
                $('#emailVerified').val('true');
                $('#verificationCode').prop('disabled', true);
                $btn.removeClass('btn-primary')
                    .addClass('btn-success')
                    .html('인증완료');
                
                // Disable email field and send button
                $('#email').prop('readonly', true);
                $('#sendVerificationBtn').prop('disabled', true);
                
                // Show success message
                this.showMessage('이메일 인증이 완료되었습니다.', 'success');
                
                // Hide countdown
                $('#countdown').text('');
            } else {
                this.showError('verificationMessage', '인증번호가 일치하지 않습니다.');
                $btn.prop('disabled', false).text('인증하기');
            }
        }, 500);
    }

    handleFormSubmit(event) {
        // Check if email is verified
        if ($('#emailVerified').val() !== 'true') {
            event.preventDefault();
            this.showError('verificationMessage', '이메일 인증을 완료해주세요.');
            $('html, body').animate({
                scrollTop: $('#email').offset().top - 100
            }, 500);
            return;
        }
        
        // Other validations will be handled by HTML5 validation
        if (!event.currentTarget.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }
        
        $(event.currentTarget).addClass('was-validated');
    }

    validatePassword() {
        const password = $(this).val();
        const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
        
        if (password.length > 0) {
            if (password.length < 8) {
                this.setCustomValidity('비밀번호는 8자 이상이어야 합니다.');
            } else if (!regex.test(password)) {
                this.setCustomValidity('영문, 숫자, 특수문자를 모두 포함해야 합니다.');
            } else {
                this.setCustomValidity('');
            }
        }
    }

    startCountdown() {
        this.timeLeft = 180; // Reset to 3 minutes
        
        // Clear any existing interval
        if (this.countdownInterval) {
            clearInterval(this.countdownInterval);
        }
        
        // Initial display
        this.updateCountdown();
        
        // Update countdown every second
        this.countdownInterval = setInterval(() => {
            this.timeLeft--;
            this.updateCountdown();
            
            if (this.timeLeft <= 0) {
                clearInterval(this.countdownInterval);
                $('#verificationCode').prop('disabled', true);
                $('#verifyBtn').prop('disabled', true);
                this.showError('verificationMessage', '인증 시간이 만료되었습니다. 다시 시도해주세요.');
            }
        }, 1000);
    }

    updateCountdown() {
        const minutes = Math.floor(this.timeLeft / 60);
        const remainingSeconds = this.timeLeft % 60;
        $('#countdown').text(`${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`);
    }

    resetVerification() {
        $('#emailVerified').val('false');
        $('#verificationSection').hide();
        $('#verificationCode').val('').prop('disabled', true);
        $('#verifyBtn')
            .prop('disabled', true)
            .removeClass('btn-success')
            .addClass('btn-primary')
            .text('인증하기');
        $('#verificationMessage').hide();
        $('#countdown').text('');
        clearInterval(this.countdownInterval);
    }

    showError(elementId, message) {
        $('#' + elementId).text(message).removeClass('text-success').addClass('text-danger').show();
    }
    
    showMessage(message, type) {
        $('#verificationMessage')
            .text(message)
            .removeClass('text-danger text-success')
            .addClass('text-' + type)
            .show();
    }
}

// Initialize email verification when document is ready
$(document).ready(function() {
    window.emailVerification = new EmailVerification();
});
