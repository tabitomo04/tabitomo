document.addEventListener('DOMContentLoaded', function() {
    // 모달 요소 가져오기
    const passwordModal = document.getElementById('passwordModal');
    if (!passwordModal) return;

    const modal = new bootstrap.Modal(passwordModal);
    const step1 = document.getElementById('step1');
    const step2 = document.getElementById('step2');
    const step3 = document.getElementById('step3');
    const prevBtn = document.getElementById('prevStepBtn');
    const nextBtn = document.getElementById('nextStepBtn');
    const resetBtn = document.getElementById('resetPasswordBtn');
    const sendVerificationBtn = document.getElementById('sendVerificationBtn');
    const resendCodeBtn = document.getElementById('resendCodeBtn');
    const resetEmail = document.getElementById('resetEmail');
    const verificationCode = document.getElementById('verificationCode');
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    const countdownElement = document.getElementById('countdown');
    const errorAlert = document.getElementById('errorAlert');
    const errorMessage = document.getElementById('errorMessage');
    
    let currentStep = 1;
    let countdownInterval;
    let timeLeft = 180; // 3분 (초 단위)
    let verificationSent = false;

    // 모달이 열릴 때 초기화
    passwordModal.addEventListener('show.bs.modal', function() {
        resetForm();
        showStep(1);
    });

    // 모달이 닫힐 때 정리
    passwordModal.addEventListener('hidden.bs.modal', function() {
        clearInterval(countdownInterval);
    });

    // 이전 버튼 클릭
    prevBtn.addEventListener('click', function() {
        if (currentStep > 1) {
            showStep(currentStep - 1);
        }
    });

    // 다음 버튼 클릭
    nextBtn.addEventListener('click', function() {
        if (validateStep(currentStep)) {
            showStep(currentStep + 1);
        }
    });

    // 인증번호 전송 버튼 클릭
    sendVerificationBtn.addEventListener('click', function() {
        if (!resetEmail.value.trim()) {
            showError('이메일을 입력해주세요.', 'resetEmail');
            return;
        }

        if (!isValidEmail(resetEmail.value.trim())) {
            showError('유효한 이메일 주소를 입력해주세요.', 'resetEmail');
            return;
        }

        // TODO: 서버로 이메일 전송 요청
        sendVerificationCode(resetEmail.value.trim());
    });

    // 인증번호 재전송 버튼 클릭
    resendCodeBtn.addEventListener('click', function(e) {
        e.preventDefault();
        if (!verificationSent) return;
        
        // TODO: 서버로 재전송 요청
        sendVerificationCode(resetEmail.value.trim());
    });

    // 비밀번호 재설정 버튼 클릭
    resetBtn.addEventListener('click', function() {
        if (!validateStep(3)) return;
        
        // TODO: 서버로 비밀번호 재설정 요청
        resetPassword();
    });

    // 비밀번호 보기/숨기기 토글
    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);
            
            // 아이콘 업데이트
            const icon = this.querySelector('i');
            icon.classList.toggle('fa-eye');
            icon.classList.toggle('fa-eye-slash');
        });
    });

    // 비밀번호 강도 표시
    if (newPassword) {
        newPassword.addEventListener('input', function() {
            updatePasswordStrength(this.value);
        });
    }

    // 단계 표시 함수
    function showStep(step) {
        // 현재 단계 숨기기
        document.querySelector(`#step${currentStep}`).style.display = 'none';
        
        // 새 단계 표시
        currentStep = step;
        document.querySelector(`#step${currentStep}`).style.display = 'block';
        
        // 단계 표시기 업데이트
        updateStepIndicator();
        
        // 버튼 상태 업데이트
        updateButtons();
        
        // 첫 단계에서 이메일 입력 필드에 포커스
        if (currentStep === 1) {
            resetEmail.focus();
        }
        // 두 번째 단계에서 인증번호 입력 필드에 포커스
        else if (currentStep === 2) {
            startCountdown();
            verificationCode.focus();
        }
        // 세 번째 단계에서 새 비밀번호 필드에 포커스
        else if (currentStep === 3) {
            clearInterval(countdownInterval);
            newPassword.focus();
        }
    }

    // 단계 유효성 검사
    function validateStep(step) {
        if (step === 1) {
            if (!resetEmail.value.trim()) {
                showError('이메일을 입력해주세요.', 'resetEmail');
                return false;
            }
            if (!isValidEmail(resetEmail.value.trim())) {
                showError('유효한 이메일 주소를 입력해주세요.', 'resetEmail');
                return false;
            }
            return true;
        } else if (step === 2) {
            if (!verificationCode.value.trim()) {
                showError('인증번호를 입력해주세요.', 'verificationCode');
                return false;
            }
            // TODO: 인증번호 유효성 검사 로직 추가
            return true;
        } else if (step === 3) {
            if (!newPassword.value) {
                showError('새 비밀번호를 입력해주세요.', 'newPassword');
                return false;
            }
            if (!isValidPassword(newPassword.value)) {
                showError('비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.', 'newPassword');
                return false;
            }
            if (newPassword.value !== confirmPassword.value) {
                showError('비밀번호가 일치하지 않습니다.', 'confirmPassword');
                return false;
            }
            return true;
        }
        return true;
    }

    // 단계 표시기 업데이트
    function updateStepIndicator() {
        // 모든 단계에서 active 클래스 제거
        document.querySelectorAll('.step').forEach((step, index) => {
            step.classList.remove('active');
            if (index + 1 < currentStep) {
                step.classList.add('completed');
            } else if (index + 1 === currentStep) {
                step.classList.add('active');
            }
        });
        
        // 진행 바 업데이트
        const progress = ((currentStep - 1) / 2) * 100;
        document.querySelectorAll('.step-line-progress').forEach(progressBar => {
            progressBar.style.width = `${progress}%`;
        });
    }

    // 버튼 상태 업데이트
    function updateButtons() {
        // 이전 버튼
        if (currentStep === 1) {
            prevBtn.style.display = 'none';
        } else {
            prevBtn.style.display = 'inline-block';
        }
        
        // 다음/완료 버튼
        if (currentStep < 3) {
            nextBtn.style.display = 'inline-block';
            resetBtn.style.display = 'none';
        } else {
            nextBtn.style.display = 'none';
            resetBtn.style.display = 'inline-block';
        }
    }

    // 카운트다운 시작
    function startCountdown() {
        clearInterval(countdownInterval);
        timeLeft = 180; // 3분으로 초기화
        updateCountdownDisplay();
        
        countdownInterval = setInterval(() => {
            timeLeft--;
            updateCountdownDisplay();
            
            if (timeLeft <= 0) {
                clearInterval(countdownInterval);
                // TODO: 인증 시간 초과 처리
                showAlert('인증 시간이 만료되었습니다. 다시 시도해주세요.');
            }
        }, 1000);
    }
    
    // 카운트다운 표시 업데이트
    function updateCountdownDisplay() {
        if (!countdownElement) return;
        
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        countdownElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }
    
    // 이메일 유효성 검사
    function isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }
    
    // 비밀번호 유효성 검사
    function isValidPassword(password) {
        // 8자 이상, 영문, 숫자, 특수문자 포함
        const re = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
        return re.test(password);
    }
    
    // 비밀번호 강도 업데이트
    function updatePasswordStrength(password) {
        if (!password) {
            document.querySelector('.password-strength .progress-bar').style.width = '0%';
            document.getElementById('passwordStrength').textContent = '약함';
            return;
        }
        
        let strength = 0;
        // 길이 검사
        if (password.length >= 8) strength += 25;
        // 소문자 포함
        if (/[a-z]/.test(password)) strength += 25;
        // 대문자 포함
        if (/[A-Z]/.test(password)) strength += 15;
        // 숫자 포함
        if (/\d/.test(password)) strength += 15;
        // 특수문자 포함
        if (/[^A-Za-z0-9]/.test(password)) strength += 20;
        
        // 진행바 업데이트
        const progressBar = document.querySelector('.password-strength .progress-bar');
        progressBar.style.width = `${strength}%`;
        
        // 강도 텍스트 업데이트
        const strengthText = document.getElementById('passwordStrength');
        if (strength < 40) {
            strengthText.textContent = '약함';
            progressBar.className = 'progress-bar bg-danger';
        } else if (strength < 70) {
            strengthText.textContent = '보통';
            progressBar.className = 'progress-bar bg-warning';
        } else {
            strengthText.textContent = '강함';
            progressBar.className = 'progress-bar bg-success';
        }
    }
    
    // 에러 메시지 표시
    function showError(message, fieldId) {
        // 기존 에러 메시지 제거
        const existingError = document.querySelector(`#${fieldId} ~ .invalid-feedback`);
        if (existingError) {
            existingError.remove();
        }
        
        // 새 에러 메시지 추가
        const input = document.getElementById(fieldId);
        if (!input) return;
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback d-block';
        errorDiv.textContent = message;
        
        input.classList.add('is-invalid');
        input.parentNode.insertBefore(errorDiv, input.nextSibling);
        
        // 포커스 이동
        input.focus();
        
        // 5초 후 에러 메시지 제거
        setTimeout(() => {
            input.classList.remove('is-invalid');
            if (errorDiv.parentNode) {
                errorDiv.remove();
            }
        }, 5000);
    }
    
    // 경고 메시지 표시
    function showAlert(message, isError = true) {
        if (!errorAlert || !errorMessage) return;
        
        errorMessage.textContent = message;
        errorAlert.className = `alert ${isError ? 'alert-danger' : 'alert-success'} mt-4 d-flex align-items-center`;
        errorAlert.style.display = 'flex';
        
        // 5초 후에 자동으로 숨기기
        setTimeout(() => {
            errorAlert.style.display = 'none';
        }, 5000);
    }
    
    // 폼 초기화
    function resetForm() {
        currentStep = 1;
        timeLeft = 180;
        verificationSent = false;
        clearInterval(countdownInterval);
        
        // 입력 필드 초기화
        if (resetEmail) resetEmail.value = '';
        if (verificationCode) verificationCode.value = '';
        if (newPassword) newPassword.value = '';
        if (confirmPassword) confirmPassword.value = '';
        
        // 에러 메시지 초기화
        document.querySelectorAll('.invalid-feedback').forEach(el => el.remove());
        document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        
        // 경고 메시지 초기화
        if (errorAlert) errorAlert.style.display = 'none';
        
        // 비밀번호 강도 표시 초기화
        if (document.querySelector('.password-strength .progress-bar')) {
            document.querySelector('.password-strength .progress-bar').style.width = '0%';
            document.getElementById('passwordStrength').textContent = '약함';
        }
    }
    
    // 인증번호 전송
    function sendVerificationCode(email) {
        // TODO: 서버로 인증번호 전송 요청
        console.log('인증번호 전송 요청:', email);
        
        // 임시: 성공 가정
        verificationSent = true;
        showAlert('인증번호가 전송되었습니다. 이메일을 확인해주세요.', false);
        
        // 다음 단계로 이동
        showStep(2);
    }
    
    // 비밀번호 재설정
    function resetPassword() {
        // TODO: 서버로 비밀번호 재설정 요청
        console.log('비밀번호 재설정 요청:', {
            email: resetEmail.value.trim(),
            code: verificationCode.value.trim(),
            newPassword: newPassword.value
        });
        
        // 임시: 성공 가정
        showAlert('비밀번호가 성공적으로 변경되었습니다. 새로운 비밀번호로 로그인해주세요.', false);
        
        // 모달 닫기
        const modal = bootstrap.Modal.getInstance(passwordModal);
        if (modal) modal.hide();
    }
});
