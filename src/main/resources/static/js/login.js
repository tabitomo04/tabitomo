// 전역 변수 선언
let currentStep = 1;
const totalSteps = 3; // 총 단계 수 (이메일 입력, 인증번호 확인, 비밀번호 재설정)
let verificationCode = '';
let resetEmail = '';
let countdownInterval = null; // 카운트다운 인터벌을 저장하기 위한 전역 변수

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
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 비밀번호 토글 설정
    setupPasswordToggles();
    
    // 자동 로그인 체크박스 상태에 따라 이메일 저장 체크박스 상태 변경
    const autoLoginCheckbox = document.getElementById('autoLogin');
    const rememberEmailCheckbox = document.getElementById('rememberEmail');
    
    if (autoLoginCheckbox && rememberEmailCheckbox) {
        autoLoginCheckbox.addEventListener('change', function() {
            if (this.checked) {
                rememberEmailCheckbox.checked = true;
                rememberEmailCheckbox.disabled = true;
            } else {
                rememberEmailCheckbox.disabled = false;
            }
        });
        
        // 페이지 로드 시 자동 로그인 체크 여부에 따라 이메일 저장 체크박스 상태 설정
        if (autoLoginCheckbox.checked) {
            rememberEmailCheckbox.checked = true;
            rememberEmailCheckbox.disabled = true;
        }
    }
    
    // 로그인 폼 에러 메시지 초기화 (로그인 폼에만 해당)
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    
    if (emailError) emailError.textContent = '';
    if (passwordError) passwordError.textContent = '';
});

// 단계 업데이트
function updateSteps() {
    console.log('Updating steps to:', currentStep);
    
    // 모든 단계 숨기기
    for (let i = 1; i <= totalSteps; i++) {
        const stepElement = document.getElementById(`step${i}`);
        if (stepElement) {
            stepElement.classList.add('d-none');
        }
    }
    
    // 현재 단계만 보이기
    const currentStepElement = document.getElementById(`step${currentStep}`);
    if (currentStepElement) {
        currentStepElement.classList.remove('d-none');
    }
    
    // 버튼 상태 업데이트
    const prevBtn = document.getElementById('prevStepBtn');
    const nextBtn = document.getElementById('nextStepBtn');
    const confirmBtn = document.getElementById('confirmBtn');
    const resetBtn = document.getElementById('resetPasswordBtn');
    const cancelBtn = document.querySelector('#passwordModal .btn-secondary[data-bs-dismiss="modal"]');
    
    // 버튼 표시/숨김 설정
    if (prevBtn) {
        prevBtn.style.display = currentStep > 1 ? 'inline-block' : 'none';
    }
    
    if (nextBtn) {
        nextBtn.style.display = currentStep < 3 ? 'inline-block' : 'none';
    }
    
    if (confirmBtn) {
        confirmBtn.style.display = 'none'; // 기본적으로 숨김
    }
    
    if (resetBtn) {
        // 3단계(비밀번호 재설정)에서만 버튼 표시
        if (currentStep === 3) {
            resetBtn.classList.remove('d-none');
            resetBtn.style.display = 'inline-block';
            // 비밀번호 재설정 버튼에 클릭 이벤트 추가
            resetBtn.onclick = function() {
                resetPassword();
            };
        } else {
            resetBtn.classList.add('d-none');
            resetBtn.style.display = 'none';
        }
    }
    
    if (cancelBtn) {
        cancelBtn.style.display = currentStep === 1 ? 'inline-block' : 'none';
    }
    
    // 진행 바 업데이트
    const progressBar = document.getElementById('stepProgress');
    if (progressBar) {
        const progress = ((currentStep - 1) / (totalSteps - 1)) * 100;
        progressBar.style.width = `${progress}%`;
    }
    
    // 첫 단계로 돌아가면 카운트다운 초기화
    if (currentStep === 1) {
        stopCountdown();
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
        return false; // sendVerificationCode에서 성공 시 다음 단계로 이동
    } 
    else if (currentStep === 2) {
        // 인증번호 확인 단계
        const code = document.getElementById('verificationCode').value.trim();
        if (!code) {
            showError('인증번호를 입력해주세요.', 'codeError');
            return false;
        }
        verifyCode(); // 인증번호 확인
        return false; // verifyCode에서 성공 시 다음 단계로 이동
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

// 비밀번호 유효성 검사 (8자 이상, 영문과 숫자만 허용)
function validatePassword(password) {
    // 영문과 숫자만 허용, 최소 8자 이상
    const passwordRegex = /^[A-Za-z0-9]{8,}$/;
    return passwordRegex.test(password);
}

// 인증번호 발송
// 전역 변수로 요청 중인지 여부를 추적
let isSendingVerification = false;

async function sendVerificationCode() {
    // 이미 처리 중인 경우 무시
    if (isSendingVerification) {
        console.log('이미 인증번호 발송이 진행 중입니다.');
        return;
    }
    
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
    
    // 처리 중 플래그 설정
    isSendingVerification = true;
    
    // 재시도 관련 변수
    const maxRetries = 5;
    let retryCount = 0;
    let lastError = null;
    
    try {
        // 재시도 루프
        while (retryCount < maxRetries) {
            try {
                console.log(`시도 ${retryCount + 1}/${maxRetries}: 인증번호 발송 요청`);
                
                // 인증번호 발송 API 호출 (JSON 사용)
                const response = await fetch('/api/email/send-verification', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ email: email })
                }).catch(error => {
                    console.error('네트워크 오류 발생:', error);
                    throw new Error('서버에 연결할 수 없습니다. 인터넷 연결을 확인해주세요.');
                });
                
                // 서버 응답이 없는 경우
                if (!response) {
                    throw new Error('서버로부터 응답을 받지 못했습니다.');
                }
                
                const contentType = response.headers.get('content-type');
                
                if (contentType && contentType.includes('application/json')) {
                    const data = await response.json();
                    
                    if (!response.ok) {
                        // 404 오류는 존재하지 않는 이메일로 간주
                        if (response.status === 404) {
                            throw new Error('존재하지 않는 이메일 주소입니다.');
                        }
                        
                        // 그 외 오류는 재시도
                        throw new Error(data.message || '인증번호 발송에 실패했습니다.');
                    }
                    
                    // 성공 시
                    console.log('인증번호 발송 성공');
                    verificationCode = data.code || data.verificationCode || '123456';
                    
                    // 성공 메시지 표시
                    if (emailError) {
                        emailError.textContent = '인증번호가 발송되었습니다. 이메일을 확인해주세요.';
                        emailError.style.color = '#28a745';
                        emailError.style.display = 'block';
                    }
                    
                    // 다음 단계로 이동 (2단계: 인증번호 입력)
                    currentStep = 2;
                    updateSteps();
                    
                    // 이메일 입력 필드 비활성화
                    const emailInput = document.getElementById('resetEmail');
                    if (emailInput) {
                        emailInput.disabled = true;
                    }
                    
                    // 인증번호 입력 필드로 포커스 이동
                    setTimeout(() => {
                        const codeInput = document.getElementById('verificationCode');
                        if (codeInput) {
                            codeInput.focus();
                        }
                        // 카운트다운 시작
                        startCountdown();
                    }, 100);
                    
                    // 성공했으므로 함수 종료
                    return;
                    
                } else {
                    // JSON이 아닌 응답인 경우
                    const text = await response.text();
                    console.error('Non-JSON response:', text.substring(0, 200));
                    throw new Error('서버에서 예상치 못한 응답을 받았습니다.');
                }
                
            } catch (error) {
                console.error(`시도 ${retryCount + 1} 실패:`, error);
                lastError = error;
                
                // 네트워크 오류 또는 서버 연결 오류인 경우 즉시 중단
                if (error.message.includes('서버에 연결') || 
                    error.message.includes('Failed to fetch') || 
                    error.message.includes('NETWORK_ERR')) {
                    console.error('네트워크 오류로 인해 재시도를 중단합니다.');
                    break;
                }
                
                // 마지막 시도가 아니면 잠시 대기 후 재시도
                if (retryCount < maxRetries - 1) {
                    // 1초 대기
                    await new Promise(resolve => setTimeout(resolve, 1000));
                    retryCount++;
                    continue;
                }
                
                // 모든 재시도 실패 시
                throw error;
            }
        }
        
        // 모든 재시도가 실패한 경우
        throw lastError || new Error('인증번호 발송에 실패했습니다.');
        
    } catch (error) {
        console.error('최종 오류:', error);
        if (emailError) {
            emailError.textContent = error.message || '인증번호 발송에 실패했습니다.\n잠시 후 다시 시도해주세요.';
            emailError.style.display = 'block';
            emailError.style.color = '#dc3545'; // 빨간색으로 오류 표시
        }
        
        // 사용자에게 알림
        const errorMsg = error.message.includes('존재하지 않는') || 
                        error.message.includes('서버에 연결') ||
                        error.message.includes('Failed to fetch')
            ? error.message 
            : '인증번호 발송에 여러 번 실패했습니다.\n잠시 후 다시 시도해주세요.';
        
        // 알림창 표시 (사용자 경험을 위해 주석 처리)
        // alert(errorMsg);
        
    } finally {
        // 처리 완료 후 플래그 해제
        isSendingVerification = false;
        
        // 버튼 상태 복원 (타이머가 설정되지 않은 경우에만)
        if (sendBtn && !sendBtn.disabled) {
            sendBtn.disabled = false;
            sendBtn.innerHTML = '인증번호 전송';
            
            // 오류가 발생했고, 네트워크 연결 오류가 아닌 경우에만 재시도 타이머 시작
            if (lastError && !lastError.message.includes('서버에 연결')) {
                sendBtn.disabled = true;
                let cooldown = 30; // 30초로 줄임 (테스트용)
                
                const timer = setInterval(() => {
                    const minutes = Math.floor(cooldown / 60);
                    const seconds = cooldown % 60;
                    sendBtn.textContent = `재시도 (${minutes}:${seconds.toString().padStart(2, '0')})`;
                    
                    if (cooldown <= 0) {
                        clearInterval(timer);
                        sendBtn.disabled = false;
                        sendBtn.textContent = '인증번호 전송';
                    } else {
                        cooldown--;
                    }
                }, 1000);
            }
        }
    }
}

// 인증번호 확인
function verifyCode() {
    return new Promise((resolve, reject) => {
        const code = document.getElementById('verificationCode').value.trim();
        const email = document.getElementById('resetEmail').value.trim();
        const verifyBtn = document.getElementById('verifyBtn');
        
        // 유효성 검사
        if (!code) {
            showVerificationStatus('인증번호를 입력해주세요.', 'text-danger');
            reject(new Error('인증번호를 입력해주세요.'));
            return;
        }
        
        if (code.length !== 6) {
            showVerificationStatus('인증번호 6자리를 입력해주세요.', 'text-danger');
            reject(new Error('인증번호 6자리를 입력해주세요.'));
            return;
        }
        
        if (!email) {
            showVerificationStatus('이메일을 찾을 수 없습니다. 처음부터 다시 시도해주세요.', 'text-danger');
            reject(new Error('이메일을 찾을 수 없습니다.'));
            return;
        }
        
        // 버튼 로딩 상태 설정
        let originalBtnText = '';
        if (verifyBtn) {
            originalBtnText = verifyBtn.innerHTML;
            verifyBtn.disabled = true;
            verifyBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 확인 중...';
        }
        
        // 인증번호 검증 API 호출
        fetch('/api/email/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({
                email: email,
                code: code
            })
        })
        .then(async response => {
            const contentType = response.headers.get('content-type');
            
            if (!response.ok) {
                // 에러 응답 처리
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || '인증에 실패했습니다.');
                } else {
                    const text = await response.text();
                    console.error('Non-JSON error response:', text.substring(0, 200));
                    throw new Error(`서버 오류 (${response.status}): ${response.statusText}`);
                }
            }
            
            // 성공 응답 처리
            if (contentType && contentType.includes('application/json')) {
                return response.json();
            } else {
                const text = await response.text();
                console.error('Non-JSON success response:', text.substring(0, 200));
                throw new Error('서버에서 예상치 못한 응답을 받았습니다.');
            }
        })
        .then(data => {
            if (data && data.success) {
                showVerificationStatus('인증이 완료되었습니다.', 'text-success');
                
                // 인증 성공 후 다음 단계로 이동
                setTimeout(() => {
                    currentStep = 3; // 비밀번호 재설정 단계로 이동
                    updateSteps();
                }, 1000);
                
                resolve(true);
            } else {
                throw new Error(data.message || '인증에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('인증 오류:', error);
            const errorMessage = error.message || '인증 중 오류가 발생했습니다.';
            showVerificationStatus(errorMessage, 'text-danger');
            
            // 인증 실패 시 인증번호 입력 필드 초기화 및 포커스
            const codeInput = document.getElementById('verificationCode');
            if (codeInput) {
                codeInput.value = '';
                codeInput.focus();
            }
            
            reject(error);
        })
        .finally(() => {
            // 버튼 상태 복원
            if (verifyBtn) {
                verifyBtn.disabled = false;
                verifyBtn.innerHTML = originalBtnText || '인증 확인';
            }
        });
    });
}
// 에러 메시지를 모달에 표시하는 헬퍼 함수
function showErrorInModal(message) {
    console.log('Error message to show:', message);
    // 모달 내부의 에러 알림 요소 찾기
    const modal = document.getElementById('passwordModal');
    if (!modal) {
        console.error('Password modal not found');
        return;
    }
    
    const errorAlert = modal.querySelector('#errorAlert');
    if (!errorAlert) {
        console.error('Error alert element not found in modal');
        return;
    }
    
    const errorMessage = modal.querySelector('#errorMessage');
    if (errorMessage) {
        errorMessage.textContent = message;
        errorAlert.classList.remove('d-none');
        // 5초 후 에러 메시지 숨기기
        setTimeout(() => {
            errorAlert.classList.add('d-none');
        }, 5000);
        
        // 모달이 보이도록 스크롤 조정
        errorAlert.scrollIntoView({ behavior: 'smooth', block: 'center' });
    } else {
        console.error('Error message element not found in modal');
    }
}

// 완료 버튼 상태를 초기화하는 헬퍼 함수
function resetCompleteButton(button) {
    if (button) {
        button.disabled = false;
        button.innerHTML = '완료';
    }
}

// 비밀번호 재설정
async function resetPassword() {
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmPassword')?.value;
    const completeBtn = document.getElementById('completeBtn');
    
    // 버튼 로딩 상태 설정
    if (completeBtn) {
        completeBtn.disabled = true;
        completeBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
    }
    
    // 에러 메시지 초기화
    showErrorInModal('');
    
    // 비밀번호 필드가 없는 경우
    if (!newPassword || !confirmPassword) {
        showErrorInModal('비밀번호를 입력해주세요.');
        resetCompleteButton(completeBtn);
        return;
    }
    
    // 비밀번호 길이 확인 (8자 이상)
    if (newPassword.length < 8) {
        showErrorInModal('비밀번호는 8자 이상 입력해주세요.');
        resetCompleteButton(completeBtn);
        return;
    }
    
    // 비밀번호 확인
    if (newPassword !== confirmPassword) {
        showErrorInModal('비밀번호가 일치하지 않습니다.');
        resetCompleteButton(completeBtn);
        return;
    }
    
    // 비밀번호 형식 확인 (영문, 숫자만 허용)
    if (!/^[A-Za-z0-9]+$/.test(newPassword)) {
        showErrorInModal('비밀번호는 영문과 숫자만 사용할 수 있습니다.');
        resetCompleteButton(completeBtn);
        return;
    }
    
    // 에러 메시지 초기화
    const errorAlert = document.querySelector('#passwordModal .alert.alert-danger');
    if (errorAlert) {
        errorAlert.classList.add('d-none');
    }
    
    // 서버로 비밀번호 재설정 요청
    try {
        const formData = new URLSearchParams();
        formData.append('email', resetEmail || document.getElementById('resetEmail').value.trim());
        formData.append('newPassword', newPassword);
        
        const response = await fetch('/auth/reset-password', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
            },
            body: formData
        });
        
        let data;
        try {
            data = await response.json();
        } catch (e) {
            throw new Error('서버 응답을 처리할 수 없습니다.');
        }
        
        if (!response.ok) {
            // 서버에서 반환한 오류 메시지가 있으면 사용하고, 없으면 기본 메시지 표시
            const errorMessage = data && data.message ? data.message : '비밀번호 재설정에 실패했습니다.';
            
            // 에러 메시지가 있는 경우에만 표시
            if (errorMessage) {
                // 에러 메시지를 모달에 직접 표시
                const modal = document.getElementById('passwordModal');
                if (modal) {
                    // 기존 에러 메시지 제거
                    const existingAlert = modal.querySelector('.alert.alert-danger');
                    if (existingAlert) {
                        existingAlert.remove();
                    }
                    
                    // 새 에러 메시지 생성
                    const errorAlert = document.createElement('div');
                    errorAlert.className = 'alert alert-danger';
                    errorAlert.role = 'alert';
                    errorAlert.innerHTML = `
                        <i class="fas fa-exclamation-circle me-2"></i>
                        ${errorMessage}
                    `;
                    
                    // 모달 바디의 첫 번째 자식으로 추가
                    const modalBody = modal.querySelector('.modal-body');
                    if (modalBody) {
                        const firstChild = modalBody.firstChild;
                        if (firstChild) {
                            modalBody.insertBefore(errorAlert, firstChild);
                        } else {
                            modalBody.appendChild(errorAlert);
                        }
                        
                        // 5초 후 에러 메시지 숨기기
                        setTimeout(() => {
                            errorAlert.remove();
                        }, 5000);
                    }
                }
            }
            
            console.error('비밀번호 재설정 실패:', errorMessage);
            return; // 함수 종료
        }
        
        if (data.success) {
            // 모든 단계 숨기기
            for (let i = 1; i <= totalSteps; i++) {
                const stepElement = document.getElementById(`step${i}`);
                if (stepElement) stepElement.classList.add('d-none');
            }
            
            // 모든 버튼 숨기기
            const buttons = document.querySelectorAll('#passwordModal .btn:not(.btn-close)');
            buttons.forEach(btn => {
                btn.style.display = 'none';
            });
            
            // 성공 메시지 표시
            const successHtml = `
                <div class="text-center py-4">
                    <div class="mb-3">
                        <i class="fas fa-check-circle text-success" style="font-size: 4rem;"></i>
                    </div>
                    <h5 class="mb-3">비밀번호 변경 완료</h5>
                    <p class="text-muted">비밀번호가 성공적으로 변경되었습니다.</p>
                    <button type="button" class="btn btn-primary mt-3" id="closeModalBtn">확인</button>
                </div>
            `;
            
            // 모달 바디에 성공 메시지 표시
            const modalBody = document.querySelector('#passwordModal .modal-body');
            if (modalBody) {
                modalBody.innerHTML = successHtml;
                
                // 확인 버튼에 이벤트 리스너 추가
                document.getElementById('closeModalBtn').addEventListener('click', function() {
                    const modal = bootstrap.Modal.getInstance(document.getElementById('passwordModal'));
                    if (modal) {
                        modal.hide();
                        // 모달이 닫힌 후 폼 초기화
                        modal._element.addEventListener('hidden.bs.modal', function onModalHidden() {
                            // 폼 초기화
                            const form = document.getElementById('resetPasswordForm');
                            if (form) {
                                form.reset();
                            }
                            
                            // 모달 내용 초기화
                            const modalBody = document.querySelector('#passwordModal .modal-body');
                            if (modalBody) {
                                modalBody.innerHTML = '';
                            }
                            
                            // 모달 닫기
                            const modalInstance = bootstrap.Modal.getInstance(document.getElementById('passwordModal'));
                            if (modalInstance) {
                                modalInstance.hide();
                            }
                            
                            // 페이지 새로고침으로 완전히 초기화
                            window.location.reload();
                        }, { once: true });
                    }
                });
            }
        } else {
            throw new Error(data.message || '비밀번호 재설정에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('비밀번호 재설정 오류:', error);
        
        // 에러 메시지를 모달에 직접 표시
        const modal = document.getElementById('passwordModal');
        if (modal) {
            // 기존 에러 메시지 제거
            const existingAlert = modal.querySelector('.alert.alert-danger');
            if (existingAlert) {
                existingAlert.remove();
            }
            
            // 새 에러 메시지 생성
            const errorMessage = error.message || '비밀번호 재설정 중 오류가 발생했습니다.';
            const errorAlert = document.createElement('div');
            errorAlert.className = 'alert alert-danger';
            errorAlert.role = 'alert';
            errorAlert.innerHTML = `
                <i class="fas fa-exclamation-circle me-2"></i>
                ${errorMessage}
            `;
            
            // 모달 바디의 첫 번째 자식으로 추가
            const modalBody = modal.querySelector('.modal-body');
            if (modalBody && modalBody.firstChild) {
                modalBody.insertBefore(errorAlert, modalBody.firstChild);
                
                // 5초 후 에러 메시지 숨기기
                setTimeout(() => {
                    errorAlert.remove();
                }, 5000);
            }
        }
    } finally {
        if (completeBtn) {
            completeBtn.disabled = false;
            completeBtn.innerHTML = '완료';
        }
    }
}


// 카운트다운 시작
function startCountdown() {
    // 기존 인터벌이 있으면 정리
    stopCountdown();

    let timeLeft = 180; // 3분 (180초)
    const countdownElement = document.getElementById('countdown');
    const resendBtn = document.getElementById('resendCodeBtn');
    const resendLink = document.getElementById('resendLink');
    const codeError = document.getElementById('codeError');
    
    // 초기 상태 설정
    if (resendBtn) resendBtn.disabled = true;
    if (resendLink) resendLink.classList.add('d-none');
    if (codeError) codeError.textContent = '';
    
    // 디버깅을 위한 로그
    console.log('Starting countdown...');
    
    // 카운트다운 시작
    countdownInterval = setInterval(() => {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        
        // 시간 표시 (00:00 형식)
        if (countdownElement) {
            countdownElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }
        
        // 시간 종료 시
        if (timeLeft <= 0) {
            stopCountdown();
            if (countdownElement) countdownElement.textContent = '00:00';
            if (resendBtn) resendBtn.disabled = false;
            if (resendLink) resendLink.classList.remove('d-none');
            if (codeError) codeError.textContent = '인증 시간이 만료되었습니다. 재전송 버튼을 눌러주세요.';
        }
        
        timeLeft--;
    }, 1000);
}

// 인증 상태 메시지 표시
function showVerificationStatus(message, className) {
    const statusElement = document.getElementById('verificationStatus');
    if (statusElement) {
        statusElement.textContent = message;
        statusElement.className = 'small ' + className;
    }
}

// 카운트다운 정리 함수
function stopCountdown() {
    if (countdownInterval) {
        console.log('Stopping countdown...');
        clearInterval(countdownInterval);
        countdownInterval = null;
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 비밀번호 토글 설정
    setupPasswordToggles();
    
    // 자동 로그인 체크박스 상태에 따라 이메일 저장 체크박스 상태 변경
    const autoLoginCheckbox = document.getElementById('autoLogin');
    const rememberEmailCheckbox = document.getElementById('rememberEmail');
    
    if (autoLoginCheckbox && rememberEmailCheckbox) {
        autoLoginCheckbox.addEventListener('change', function() {
            if (this.checked) {
                rememberEmailCheckbox.checked = true;
                rememberEmailCheckbox.disabled = true;
            } else {
                rememberEmailCheckbox.disabled = false;
            }
        });
        
        // 페이지 로드 시 자동 로그인 체크 여부에 따라 이메일 저장 체크박스 상태 설정
        if (autoLoginCheckbox.checked) {
            rememberEmailCheckbox.checked = true;
            rememberEmailCheckbox.disabled = true;
        }
    }
        
        // 이전 버튼 클릭 이벤트
        const prevStepBtn = document.getElementById('prevStepBtn');
        if (prevStepBtn) {
            prevStepBtn.addEventListener('click', goToPrevStep);
        }
        
        // 인증번호 발송 버튼 클릭 이벤트
        const sendVerificationBtn = document.getElementById('sendVerificationBtn');
        if (sendVerificationBtn) {
            sendVerificationBtn.addEventListener('click', function(e) {
                e.preventDefault();
                sendVerificationCode();
            });
        }
        
        // 인증번호 재전송 버튼 클릭 이벤트
        const resendCodeBtn = document.getElementById('resendCodeBtn');
        const resendLink = document.getElementById('resendLink');
        
        function handleResendClick(e) {
            if (e) e.preventDefault();
            console.log('Resend button clicked');
            // 인증번호 재발송
            sendVerificationCode().then(() => {
                // 인증번호 발송 성공 후에만 카운트다운 시작
                startCountdown();
            });
        }
        
        if (resendCodeBtn) {
            resendCodeBtn.addEventListener('click', handleResendClick);
        }
        
        if (resendLink) {
            resendLink.addEventListener('click', handleResendClick);
        }
        
        // 인증번호 입력 필드 및 확인 버튼 설정
        const verificationCodeInput = document.getElementById('verificationCode');
        const verifyCodeBtn = document.getElementById('verifyCodeBtn');
        const confirmBtn = document.getElementById('confirmBtn');
        const verificationStatus = document.getElementById('verificationStatus');
        
        // 확인 버튼 클릭 이벤트 (비밀번호 재설정 완료)
        if (confirmBtn) {
            confirmBtn.addEventListener('click', function() {
                resetPassword();
            });
        }
        
        // 비밀번호 재설정 버튼 클릭 이벤트
        const resetPasswordBtn = document.getElementById('resetPasswordBtn');
        if (resetPasswordBtn) {
            resetPasswordBtn.addEventListener('click', function() {
                resetPassword();
            });
        }
        
        if (verificationCodeInput && verifyCodeBtn) {
            // 인증 확인 버튼 클릭 이벤트
            verifyCodeBtn.addEventListener('click', function() {
                const code = verificationCodeInput.value.trim();
                if (!code) {
                    showVerificationStatus('인증번호를 입력해주세요.', 'text-danger');
                    return;
                }
                if (code.length !== 6) {
                    showVerificationStatus('인증번호 6자리를 입력해주세요.', 'text-danger');
                    return;
                }
                
                // 인증번호 검증 로직
                verifyCode().then(isValid => {
                    if (isValid) {
                        showVerificationStatus('인증이 완료되었습니다.', 'text-success');
                        // 다음 단계로 이동
                        currentStep++;
                        updateSteps();
                    } else {
                        showVerificationStatus('인증번호가 일치하지 않습니다.', 'text-danger');
                    }
                });
            });
            
            // 엔터 키로 폼 제출 방지 및 인증 시도
            verificationCodeInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    if (currentStep === 2) {
                        verifyCode().then(isValid => {
                            if (isValid) {
                                currentStep++;
                                updateSteps();
                            }
                        });
                    }
                }
            });
        }
    }
);
