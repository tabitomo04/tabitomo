// 로그인 폼 처리
window.handleLoginSubmit = function(event) {
    event.preventDefault();
    const form = event.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    // 로딩 상태 설정
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 로그인 중...';
    
    // 폼 제출 (비동기 처리)
    fetch(form.action, {
        method: 'POST',
        body: new FormData(form),
        headers: {
            'Accept': 'text/html',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        if (response.redirected) {
            window.location.href = response.url;
        } else {
            return response.text().then(html => {
                document.documentElement.innerHTML = html;
            });
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('로그인 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
    })
    .finally(() => {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    });
    
    return false;
}

// 비밀번호 표시/숨기기 토글
function setupPasswordToggles() {
    // 로그인 폼 비밀번호 토글
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.querySelector('i').classList.toggle('fa-eye');
            this.querySelector('i').classList.toggle('fa-eye-slash');
        });
    }
    
    // 새 비밀번호 토글
    const toggleNewPassword = document.getElementById('toggleNewPassword');
    const newPasswordInput = document.getElementById('newPassword');
    
    if (toggleNewPassword && newPasswordInput) {
        toggleNewPassword.addEventListener('click', function() {
            const type = newPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            newPasswordInput.setAttribute('type', type);
            this.querySelector('i').classList.toggle('fa-eye');
            this.querySelector('i').classList.toggle('fa-eye-slash');
        });
    }
    
    // 비밀번호 확인 토글
    const toggleConfirmPassword = document.getElementById('toggleConfirmPassword');
    const confirmPasswordInput = document.getElementById('confirmNewPassword');
    
    if (toggleConfirmPassword && confirmPasswordInput) {
        toggleConfirmPassword.addEventListener('click', function() {
            const type = confirmPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            confirmPasswordInput.setAttribute('type', type);
            this.querySelector('i').classList.toggle('fa-eye');
            this.querySelector('i').classList.toggle('fa-eye-slash');
        });
    }
}

// 비밀번호 재설정 관련 변수
let verificationCode = '';
let countdownInterval;
let currentStep = 1;
const totalSteps = 3;

// 모달 초기화
function resetPasswordModal() {
    currentStep = 1;
    resetEmail = '';
    clearInterval(countdownInterval);
    
    // 입력 필드 초기화
    document.getElementById('resetEmail').value = '';
    document.getElementById('verificationCode').value = '';
    document.getElementById('newPassword').value = '';
    document.getElementById('confirmNewPassword').value = '';
    
    // 에러 메시지 초기화
    document.getElementById('emailError').textContent = '';
    document.getElementById('codeError').textContent = '';
    document.getElementById('passwordError').textContent = '';
    
    // 카운트다운 초기화
    const countdownElement = document.getElementById('countdown');
    if (countdownElement) {
        countdownElement.textContent = '03:00';
    }
    
    // 단계 업데이트
    updateSteps();
}

// 단계 업데이트
function updateSteps() {
    // 단계 표시기 업데이트
    for (let i = 1; i <= totalSteps; i++) {
        const stepElement = document.getElementById(`step${i}`);
        if (stepElement) {
            stepElement.classList.remove('active', 'completed');
            if (i < currentStep) {
                stepElement.classList.add('completed');
            } else if (i === currentStep) {
                stepElement.classList.add('active');
            }
        }
    }
    
    // 진행 바 업데이트
    const progressBar = document.getElementById('stepProgress');
    if (progressBar) {
        const progress = ((currentStep - 1) / (totalSteps - 1)) * 100;
        progressBar.style.width = `${progress}%`;
    }
    
    // 컨텐츠 표시/숨김
    const stepContents = ['step1Content', 'step2Content', 'step3Content'];
    stepContents.forEach((id, index) => {
        const element = document.getElementById(id);
        if (element) {
            element.style.display = (index + 1 === currentStep) ? 'block' : 'none';
        }
    });
    
    // 버튼 표시/숨김
    const prevBtn = document.getElementById('prevStepBtn');
    const nextBtn = document.getElementById('nextStepBtn');
    const completeBtn = document.getElementById('completeBtn');
    const cancelBtn = document.querySelector('#passwordModal .btn-secondary[data-bs-dismiss="modal"]');
    
    if (prevBtn) prevBtn.style.display = currentStep > 1 ? 'block' : 'none';
    if (nextBtn) nextBtn.style.display = currentStep < 3 ? 'block' : 'none';
    if (completeBtn) completeBtn.style.display = currentStep === 3 ? 'block' : 'none';
    if (cancelBtn) cancelBtn.style.display = currentStep === 1 ? 'block' : 'none';
    
    // 첫 단계로 돌아가면 카운트다운 초기화
    if (currentStep === 1) {
        clearInterval(countdownInterval);
        const countdownElement = document.getElementById('countdown');
        if (countdownElement) {
            countdownElement.textContent = '03:00';
        }
    }
}

// 다음 단계로 이동
function goToNextStep() {
    if (!validateCurrentStep()) {
        return false;
    }
    
    // 현재 단계에 따른 추가 유효성 검사
    if (currentStep === 1) {
        // 이메일 인증 단계
        const email = document.getElementById('resetEmail').value.trim();
        if (!validateEmail(email)) {
            showError('유효한 이메일 주소를 입력해주세요.', 'emailError');
            return false;
        }
        resetEmail = email; // 이메일 저장
        sendVerificationCode(); // 인증번호 발송
        return; // sendVerificationCode에서 성공 시 다음 단계로 이동
    } 
    else if (currentStep === 2) {
        // 인증번호 확인 단계
        const code = document.getElementById('verificationCode').value.trim();
        if (!code) {
            showError('인증번호를 입력해주세요.', 'codeError');
            return false;
        }
        verifyCode(); // 인증번호 확인
        return; // verifyCode에서 성공 시 다음 단계로 이동
    }
    
    // 다음 단계로 이동
    if (currentStep < totalSteps) {
        currentStep++;
        updateSteps();
    }
    return true;
}

// 이전 단계로 이동
function goToPrevStep() {
    if (currentStep > 1) {
        currentStep--;
        updateSteps();
        
        // 1단계로 돌아가면 카운트다운 중지
        if (currentStep === 1) {
            clearInterval(countdownInterval);
            const countdownElement = document.getElementById('countdown');
            if (countdownElement) {
                countdownElement.textContent = '03:00';
            }
        }
    }
}

// 현재 단계의 유효성 검사
function validateCurrentStep() {
    if (currentStep === 1) {
        const email = document.getElementById('resetEmail').value.trim();
        if (!email) {
            showError('이메일을 입력해주세요.', 'emailError');
            return false;
        }
        if (!validateEmail(email)) {
            showError('유효한 이메일 주소를 입력해주세요.', 'emailError');
            return false;
        }
    } else if (currentStep === 2) {
        const code = document.getElementById('verificationCode').value.trim();
        if (!code) {
            showError('인증번호를 입력해주세요.', 'codeError');
            return false;
        }
    } else if (currentStep === 3) {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmNewPassword').value;
        
        if (!newPassword) {
            showError('새 비밀번호를 입력해주세요.', 'passwordError');
            return false;
        }
        
        if (newPassword.length < 8) {
            showError('비밀번호는 8자 이상이어야 합니다.', 'passwordError');
            return false;
        }
        
        if (newPassword !== confirmPassword) {
            showError('비밀번호가 일치하지 않습니다.', 'passwordError');
            return false;
        }
    }
    
    return true;
}

// 이메일 유효성 검사
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// 비밀번호 유효성 검사
function validatePassword(password) {
    // 영문, 숫자, 특수문자 포함 8자 이상
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    return passwordRegex.test(password);
}

// 인증번호 발송
async function sendVerificationCode() {
    const email = document.getElementById('resetEmail').value.trim();
    const sendBtn = document.getElementById('sendVerificationBtn');
    const emailError = document.getElementById('emailError');
    
    // 이메일 유효성 검사
    if (!email) {
        emailError.textContent = '이메일을 입력해주세요.';
        emailError.style.display = 'block';
        return;
    }
    
    if (!validateEmail(email)) {
        emailError.textContent = '유효한 이메일 주소를 입력해주세요.';
        emailError.style.display = 'block';
        return;
    }
    
    // 버튼 로딩 상태 설정
    const originalText = sendBtn.innerHTML;
    sendBtn.disabled = true;
    sendBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 전송 중...';
    
    try {
        // 실제 API 호출로 대체해야 함
        const response = await fetch('/api/auth/send-verification', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        if (!response.ok) {
            throw new Error('인증번호 발송에 실패했습니다.');
        }

        const data = await response.json();
        verificationCode = data.verificationCode || '123456'; // 테스트용 코드 (실제로는 서버에서 받아옴)
        
        // 다음 단계로 이동
        goToNextStep();
        
        // 인증번호 입력 필드로 포커스 이동
        setTimeout(() => {
            document.getElementById('verificationCode').focus();
        }, 100);
    } catch (error) {
        console.error('Error:', error);
        emailError.textContent = error.message || '인증번호 발송 중 오류가 발생했습니다.';
        emailError.style.display = 'block';
    } finally {
        // 버튼 상태 복원
        sendBtn.disabled = false;
        sendBtn.innerHTML = originalText;
    }
}

// 인증번호 확인
function verifyCode() {
    const code = document.getElementById('verificationCode').value.trim();
    const codeError = document.getElementById('codeError');
    
    if (!code) {
        codeError.textContent = '인증번호를 입력해주세요.';
        return false;
    }
    
    if (code !== verificationCode) {
        codeError.textContent = '인증번호가 일치하지 않습니다.';
        return false;
    }
    
    // 인증 성공
    codeError.textContent = '';
    goToNextStep();
    return true;
}

// 비밀번호 재설정
async function resetPassword() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmNewPassword').value;
    const passwordError = document.getElementById('passwordError');
    const completeBtn = document.getElementById('completeBtn');
    
    // 버튼 로딩 상태 설정
    if (completeBtn) {
        const originalText = completeBtn.innerHTML;
        completeBtn.disabled = true;
        completeBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
    }
    
    // 에러 메시지 초기화
    passwordError.textContent = '';
    passwordError.style.display = 'none';
    
    // 비밀번호 유효성 검사
    if (!newPassword || !confirmPassword) {
        showError('비밀번호를 입력해주세요.', 'passwordError');
        if (completeBtn) {
            completeBtn.disabled = false;
            completeBtn.innerHTML = '완료';
        }
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showError('비밀번호가 일치하지 않습니다.', 'passwordError');
        if (completeBtn) {
            completeBtn.disabled = false;
            completeBtn.innerHTML = '완료';
        }
        return;
    }
    
    if (!validatePassword(newPassword)) {
        showError('영문, 숫자, 특수문자를 포함하여 8자 이상 입력해주세요.', 'passwordError');
        if (completeBtn) {
            completeBtn.disabled = false;
            completeBtn.innerHTML = '완료';
        }
        return;
    }
    
    // 서버로 비밀번호 재설정 요청
    try {
        const response = await fetch('/api/auth/reset-password', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
            },
            body: JSON.stringify({
                email: resetEmail || document.getElementById('resetEmail').value.trim(),
                newPassword: newPassword,
                verificationCode: verificationCode
            })
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || '비밀번호 재설정에 실패했습니다.');
        }
        
        if (data.success) {
            // 성공 메시지 표시
            alert('비밀번호가 성공적으로 변경되었습니다. 새로운 비밀번호로 로그인해주세요.');
            
            // 모달 닫기
            const modal = bootstrap.Modal.getInstance(document.getElementById('passwordModal'));
            if (modal) {
                modal.hide();
            }
            
            // 폼 초기화
            resetPasswordModal();
        } else {
            throw new Error(data.message || '비밀번호 재설정에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('비밀번호 재설정 오류:', error);
        showError(error.message || '비밀번호 재설정 중 오류가 발생했습니다.', 'passwordError');
    } finally {
        if (completeBtn) {
            completeBtn.disabled = false;
            completeBtn.innerHTML = '완료';
        }
    }
}

// 카운트다운 시작
function startCountdown() {
    let timeLeft = 180; // 3분 (180초)
    const countdownElement = document.getElementById('countdown');
    
    clearInterval(countdownInterval);
    
    countdownInterval = setInterval(() => {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        countdownElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        if (timeLeft <= 0) {
            clearInterval(countdownInterval);
            document.getElementById('codeError').textContent = '인증 시간이 만료되었습니다. 다시 시도해주세요.';
        }
        
        timeLeft--;
    }, 1000);
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 비밀번호 토글 설정
    setupPasswordToggles();
    
    // 비밀번호 재설정 모달 이벤트 리스너
    const passwordModal = document.getElementById('passwordModal');
    if (passwordModal) {
        // 모달이 열릴 때 초기화
        passwordModal.addEventListener('show.bs.modal', resetPasswordModal);
        
        // 다음 버튼 클릭 이벤트
        const nextStepBtn = document.getElementById('nextStepBtn');
        if (nextStepBtn) {
            nextStepBtn.addEventListener('click', function() {
                if (currentStep === 2) {
                    if (!verifyCode()) return;
                }
                goToNextStep();
            });
        }
        
        // 이전 버튼 클릭 이벤트
        const prevStepBtn = document.getElementById('prevStepBtn');
        if (prevStepBtn) {
            prevStepBtn.addEventListener('click', goToPrevStep);
        }
        
        // 인증번호 발송 버튼 클릭 이벤트
        const sendVerificationBtn = document.getElementById('sendVerificationBtn');
        if (sendVerificationBtn) {
            sendVerificationBtn.addEventListener('click', sendVerificationCode);
        }
        
        // 인증번호 재전송 버튼 클릭 이벤트
        const resendCodeBtn = document.getElementById('resendCodeBtn');
        if (resendCodeBtn) {
            resendCodeBtn.addEventListener('click', sendVerificationCode);
        }
        
        // 비밀번호 재설정 버튼 클릭 이벤트
        const resetPasswordBtn = document.getElementById('resetPasswordBtn');
        if (resetPasswordBtn) {
            resetPasswordBtn.addEventListener('click', resetPassword);
        }
        
        // 엔터 키로 폼 제출 방지
        const verificationCodeInput = document.getElementById('verificationCode');
        if (verificationCodeInput) {
            verificationCodeInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    if (currentStep === 2) {
                        if (verifyCode()) {
                            goToNextStep();
                        }
                    }
                }
            });
        }
    }
});
