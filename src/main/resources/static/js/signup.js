// 전역 변수
let isEmailChecked = false;
let isNicknameChecked = false;
let isEmailVerified = false;
let verificationTimer = null;
let timeLeft = 0;

// 이메일 주소 조합
function combineEmail() {
    const emailId = document.getElementById('emailId').value.trim();
    const emailDomain = document.getElementById('emailDomain').value.trim();
    
    if (!emailId || !emailDomain) {
        return '';
    }
    
    return `${emailId}@${emailDomain}`;
}

// 메시지 표시 함수
function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    element.textContent = message;
    element.className = 'validation-message ' + (type === 'error' ? 'error' : 'success');
    
    // 5초 후에 메시지 숨기기 (에러 메시지만 해당)
    if (type === 'error') {
        setTimeout(() => {
            element.textContent = '';
            element.className = 'validation-message';
        }, 5000);
    }
}

// 이메일 중복 확인 리셋
function resetEmailCheck() {
    isEmailChecked = false;
    isEmailVerified = false;
    document.getElementById('emailCheckResult').textContent = '';
    document.getElementById('emailCheckResult').className = 'validation-message';
    document.getElementById('emailVerifyBtn').style.display = 'none';
}

// 닉네임 중복 확인 리셋
function resetNicknameCheck() {
    isNicknameChecked = false;
    const resultDiv = document.getElementById('nicknameCheckResult');
    if (resultDiv) {
        resultDiv.textContent = '';
        resultDiv.className = 'validation-message';
    }
}

// 이메일 중복 확인
function checkEmail() {
    const email = combineEmail();
    
    if (!email) {
        showMessage('emailCheckResult', '이메일을 입력해주세요.', 'error');
        return false;
    }
    
    // 로딩 상태 표시
    const checkBtn = document.getElementById('emailCheckBtn');
    const originalText = checkBtn.innerHTML;
    checkBtn.disabled = true;
    checkBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 확인 중...';
    
    // 이메일 형식 검증
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    if (!emailRegex.test(email)) {
        showMessage('emailCheckResult', '유효하지 않은 이메일 형식입니다.', 'error');
        checkBtn.disabled = false;
        checkBtn.innerHTML = originalText;
        return;
    }
    
    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    
    // 이메일 중복 확인
    fetch('/member/api/check-email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            [csrfHeader]: csrfToken
        },
        credentials: 'same-origin',
        body: JSON.stringify({ email: email })
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || '이메일 확인 중 오류가 발생했습니다.');
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.available) {
            // 이메일 사용 가능한 경우
            showMessage('emailCheckResult', '사용 가능한 이메일입니다. 인증하기 버튼을 클릭해주세요.', 'success');
            isEmailChecked = true;
            isEmailVerified = false;
            
            // 인증 버튼 활성화
            const verifyBtn = document.getElementById('emailVerifyBtn');
            if (verifyBtn) {
                verifyBtn.style.display = 'inline-block';
                verifyBtn.disabled = false;
                // 클릭 이벤트 리스너 추가
                verifyBtn.onclick = function() { requestEmailVerification(); };
            }
            
            // 전역 변수에 이메일 저장
            window.currentEmail = email;
        } else {
            // 이메일이 이미 사용 중인 경우
            showMessage('emailCheckResult', data.message || '이미 사용 중인 이메일입니다.', 'error');
            isEmailChecked = false;
            isEmailVerified = false;
        }
    })
    .catch(error => {
        console.error('이메일 확인 중 오류:', error);
        showMessage('emailCheckResult', error.message || '이메일 확인 중 오류가 발생했습니다.', 'error');
    })
    .finally(() => {
        const checkBtn = document.getElementById('emailCheckBtn');
        if (checkBtn) {
            checkBtn.disabled = false;
            checkBtn.innerHTML = '중복확인';
        }
    });
}

// 닉네임 중복 확인
function checkNickname() {
    const nicknameInput = document.getElementById('nickname');
    const nickname = nicknameInput ? nicknameInput.value.trim() : '';
    
    if (!nickname) {
        showMessage('nicknameCheckResult', '닉네임을 입력해주세요.', 'error');
        isNicknameChecked = false;
        return false;
    }
    
    // 로딩 상태 표시
    const checkBtn = document.getElementById('nicknameCheckBtn');
    const originalText = checkBtn.innerHTML;
    checkBtn.disabled = true;
    checkBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 확인 중...';
    
    // 결과 메시지 초기화
    const resultDiv = document.getElementById('nicknameCheckResult');
    if (resultDiv) {
        resultDiv.textContent = '';
        resultDiv.className = 'validation-message';
    }
    
    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    
    // 중복 확인 요청
    return fetch('/member/api/check-nickname', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            [csrfHeader]: csrfToken
        },
        credentials: 'same-origin',
        body: JSON.stringify({ nickname: nickname })
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || '닉네임 확인 중 오류가 발생했습니다.');
            });
        }
        return response.json();
    })
    .then(data => {
        if (!data.available) {
            // 닉네임이 이미 사용 중인 경우
            const errorMessage = data.message || '이미 사용 중인 닉네임입니다.';
            showMessage('nicknameCheckResult', errorMessage, 'error');
            isNicknameChecked = false;
            
            // 실패 시 버튼 스타일 초기화
            const checkBtn = document.getElementById('nicknameCheckBtn');
            if (checkBtn) {
                checkBtn.classList.remove('btn-success');
                checkBtn.innerHTML = '중복 확인';
                checkBtn.disabled = false;
            }
            
            // 세션 스토리지에서 이전에 저장된 닉네임 제거
            sessionStorage.removeItem('lastCheckedNickname');
            
            return false;
        } else {
            // 닉네임 사용 가능한 경우
            isNicknameChecked = true;
            
            // 성공 메시지 표시
            const resultDiv = document.getElementById('nicknameCheckResult');
            if (resultDiv) {
                resultDiv.textContent = data.message || '사용 가능한 닉네임입니다.';
                resultDiv.className = 'validation-message success';
            }
            
            // 버튼 스타일 변경
            const checkBtn = document.getElementById('nicknameCheckBtn');
            if (checkBtn) {
                checkBtn.classList.add('btn-success', 'text-white');
                checkBtn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> 확인 완료';
                checkBtn.style.color = 'white';
                checkBtn.disabled = false;
            }
            
            // 성공한 닉네임 저장
            sessionStorage.setItem('lastCheckedNickname', nickname);
            
            return true;
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('nicknameCheckResult', error.message || '닉네임 확인 중 오류가 발생했습니다.', 'error');
        isNicknameChecked = false;
        
        // 에러 발생 시 버튼 상태 초기화
        const checkBtn = document.getElementById('nicknameCheckBtn');
        if (checkBtn) {
            checkBtn.disabled = false;
            checkBtn.innerHTML = '중복 확인';
            checkBtn.classList.remove('btn-success');
        }
        
        // 세션 스토리지에서 이전에 저장된 닉네임 제거
        sessionStorage.removeItem('lastCheckedNickname');
        
        return false;
    });
}

// 이메일 확인 상태 초기화
function resetEmailCheck() {
    isEmailChecked = false;
    isEmailVerified = false;
    const emailVerifyBtn = document.getElementById('emailVerifyBtn');
    if (emailVerifyBtn) {
        emailVerifyBtn.style.display = 'none';
    }
    const verifyCompleteBtn = document.getElementById('verifyCompleteBtn');
    if (verifyCompleteBtn) {
        verifyCompleteBtn.style.display = 'none';
    }
    const emailCheckResult = document.getElementById('emailCheckResult');
    if (emailCheckResult) {
        emailCheckResult.textContent = '';
        emailCheckResult.className = 'validation-message';
    }
    sessionStorage.removeItem('lastVerifiedEmail');
}

// 닉네임 확인 상태 초기화
function resetNicknameCheck() {
    isNicknameChecked = false;
    const checkBtn = document.getElementById('nicknameCheckBtn');
    if (checkBtn) {
        checkBtn.classList.remove('btn-success');
        checkBtn.innerHTML = '중복 확인';
    }
    const resultDiv = document.getElementById('nicknameCheckResult');
    if (resultDiv) {
        resultDiv.textContent = '';
        resultDiv.className = 'validation-message';
    }
    sessionStorage.removeItem('lastCheckedNickname');
}

// 페이지 로드 시 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    // 이메일 인증 버튼 클릭 이벤트 리스너 등록
    const emailVerifyBtn = document.getElementById('emailVerifyBtn');
    if (emailVerifyBtn) {
        // 기존에 등록된 이벤트 리스너 제거 (중복 방지)
        emailVerifyBtn.replaceWith(emailVerifyBtn.cloneNode(true));
        
        // 새 이벤트 리스너 등록
        document.getElementById('emailVerifyBtn').addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            requestEmailVerification();
        });
    }
    
    // 닉네임 입력 필드에 변경 이벤트 리스너 추가
    const nicknameInput = document.getElementById('nickname');
    if (nicknameInput) {
        // 페이지 로드 시 이전에 확인한 닉네임이 있으면 상태 복원
        const lastCheckedNickname = sessionStorage.getItem('lastCheckedNickname');
        if (lastCheckedNickname && nicknameInput.value.trim() === lastCheckedNickname) {
            isNicknameChecked = true;
            const checkBtn = document.getElementById('nicknameCheckBtn');
            if (checkBtn) {
                checkBtn.classList.add('btn-success');
                checkBtn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> 확인 완료';
            }
            const resultDiv = document.getElementById('nicknameCheckResult');
            if (resultDiv) {
                resultDiv.textContent = '사용 가능한 닉네임입니다.';
                resultDiv.className = 'validation-message success';
            }
        }
        
        // 닉네임이 변경되면 중복 확인 상태 초기화
        nicknameInput.addEventListener('input', function() {
            const currentNickname = this.value.trim();
            const lastCheckedNickname = sessionStorage.getItem('lastCheckedNickname');
            
            // 이전에 확인한 닉네임과 다른 경우에만 상태 초기화
            if (lastCheckedNickname && currentNickname !== lastCheckedNickname) {
                isNicknameChecked = false;
                const checkBtn = document.getElementById('nicknameCheckBtn');
                if (checkBtn) {
                    checkBtn.classList.remove('btn-success');
                    checkBtn.innerHTML = '중복 확인';
                }
                
                // 결과 메시지 초기화
                const resultDiv = document.getElementById('nicknameCheckResult');
                if (resultDiv) {
                    resultDiv.textContent = '';
                    resultDiv.className = 'validation-message';
                }
            } else if (lastCheckedNickname && currentNickname === lastCheckedNickname) {
                // 이전에 확인한 닉네임과 같은 경우 상태 복원
                isNicknameChecked = true;
                const checkBtn = document.getElementById('nicknameCheckBtn');
                if (checkBtn) {
                    checkBtn.classList.add('btn-success');
                    checkBtn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> 확인 완료';
                }
                const resultDiv = document.getElementById('nicknameCheckResult');
                if (resultDiv) {
                    resultDiv.textContent = '사용 가능한 닉네임입니다.';
                    resultDiv.className = 'validation-message success';
                }
            }
        });
    }
});

// 이메일 인증 요청
function requestEmailVerification() {
    // 이미 요청 중인 경우 중복 실행 방지
    if (window.isEmailVerificationInProgress) {
        console.log('이미 이메일 인증 요청이 진행 중입니다.');
        return;
    }
    
    const email = window.currentEmail || combineEmail();
    
    // 이메일 형식 검증
    if (!email) {
        showMessage('emailCheckResult', '이메일을 입력해주세요.', 'error');
        return;
    }
    
    // 이메일 형식 검증 정규식
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    if (!emailRegex.test(email)) {
        showMessage('emailCheckResult', '유효하지 않은 이메일 형식입니다.', 'error');
        return;
    }
    
    // 로딩 상태 표시
    const verifyBtn = document.getElementById('emailVerifyBtn');
    if (!verifyBtn) return;
    
    const originalText = verifyBtn.innerHTML;
    verifyBtn.disabled = true;
    verifyBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 전송 중...';
    
    // 요청 중 상태 표시
    window.isEmailVerificationInProgress = true;
    
    // 서버에 인증 이메일 요청
    console.log('Sending verification email to:', email);
    
    fetch('/api/email/send-verification', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        },
        body: JSON.stringify({ email: email })
    })
    .then(async response => {
        console.log('Response status:', response.status);
        const responseData = await response.json().catch(() => ({}));
        
        if (!response.ok) {
            console.error('Error response:', responseData);
            const errorMessage = responseData.message || 
                               (response.status === 400 ? '잘못된 요청입니다. 입력값을 확인해주세요.' : 
                               response.status === 500 ? '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.' : 
                               '인증 이메일 전송에 실패했습니다.');
            throw new Error(errorMessage);
        }
        
        if (!responseData.success) {
            throw new Error(responseData.message || '인증 이메일 전송에 실패했습니다.');
        }
        
        return responseData;
    })
    .then(data => {
        console.log('Email sent successfully:', data);
        showMessage('emailCheckResult', '인증 이메일이 전송되었습니다. 이메일을 확인해주세요.', 'success');
        openVerificationModal();
    })
    .catch(error => {
        console.error('Error details:', {
            message: error.message,
            name: error.name,
            stack: error.stack
        });
        showMessage('emailCheckResult', `오류: ${error.message}`, 'error');
    })
    .finally(() => {
        if (verifyBtn) {
            verifyBtn.disabled = false;
            verifyBtn.innerHTML = originalText || '인증하기';
        }
    });
}

// 이메일 인증 모달 열기
function openVerificationModal() {
    const modal = new bootstrap.Modal(document.getElementById('emailVerificationModal'));
    modal.show();
    startVerificationTimer();
}

// 인증 타이머 시작
function startVerificationTimer() {
    clearInterval(verificationTimer);
    timeLeft = 180; // 3분으로 초기화
    updateTimerDisplay();
    
    verificationTimer = setInterval(() => {
        timeLeft--;
        updateTimerDisplay();
        
        if (timeLeft <= 0) {
            clearInterval(verificationTimer);
            const timerElement = document.getElementById('verificationTimer');
            if (timerElement) {
                timerElement.textContent = '인증 시간이 만료되었습니다.';
                timerElement.style.color = '#dc3545';
            }
        }
    }, 1000);
}

// 타이머 표시 업데이트
function updateTimerDisplay() {
    const timerElement = document.getElementById('verificationTimer');
    if (timerElement) {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        timerElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }
}

// 인증번호 재전송
function resendVerificationCode() {
    clearInterval(verificationTimer);
    document.getElementById('verificationCode').value = '';
    document.getElementById('verificationMessage').textContent = '';
    requestEmailVerification();
}

// 이메일 인증 확인
function verifyEmail() {
    const email = window.currentEmail || combineEmail();
    const code = document.getElementById('verificationCode').value.trim();
    
    if (!code || code.length !== 6) {
        showMessage('verificationMessage', '6자리 인증 코드를 정확히 입력해주세요.', 'error');
        return;
    }
    
    const verifyBtn = document.getElementById('verifyBtn');
    const originalText = verifyBtn.innerHTML;
    verifyBtn.disabled = true;
    verifyBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 확인 중...';
    
    // 서버에 인증 코드 확인 요청
    fetch('/api/email/verify', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        },
        body: JSON.stringify({
            email: email,
            code: code
        })
    })
    .then(async response => {
        const responseData = await response.json().catch(() => ({}));
        
        if (!response.ok) {
            throw new Error(responseData.message || '인증에 실패했습니다. 코드를 확인해주세요.');
        }
        
        if (!responseData.success) {
            throw new Error(responseData.message || '인증에 실패했습니다.');
        }
        
        return responseData;
    })
    .then(data => {
        // 인증 성공
        isEmailVerified = true;
        showMessage('emailCheckResult', '이메일 인증이 완료되었습니다.', 'success');
        
        // 모달 닫기
        const modalEl = document.getElementById('emailVerificationModal');
        if (modalEl) {
            const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
            modal.hide();
            
            // 모달이 완전히 닫힌 후 실행
            modalEl.addEventListener('hidden.bs.modal', function onModalHidden() {
                // 백드롭(모달 배경) 제거
                const backdrop = document.querySelector('.modal-backdrop');
                if (backdrop) {
                    backdrop.remove();
                }
                // body에서 모달 관련 클래스 제거
                document.body.classList.remove('modal-open');
                document.body.style.overflow = '';
                document.body.style.paddingRight = '';
                
                // 이벤트 리스너 제거
                modalEl.removeEventListener('hidden.bs.modal', onModalHidden);
            }, { once: true });
        }
        
        // 인증 완료 상태 저장
        sessionStorage.setItem('emailVerified', 'true');
        
        // 인증 버튼 비활성화 및 스타일 변경
        const verifyBtn = document.getElementById('emailVerifyBtn');
        if (verifyBtn) {
            verifyBtn.disabled = true;
            verifyBtn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> 인증 완료';
            verifyBtn.classList.remove('btn-outline-success');
            verifyBtn.classList.add('btn-success');
        }
        
        // 폼에 이메일 값 설정 (보안을 위해 hidden 필드에 저장)
        const emailField = document.getElementById('email');
        if (emailField) {
            emailField.value = email;
        }
    })
    .catch(error => {
        console.error('이메일 인증 오류:', error);
        showMessage('verificationMessage', `오류: ${error.message || '인증 중 오류가 발생했습니다.'}`, 'error');
    })
    .finally(() => {
        if (verifyBtn) {
            verifyBtn.disabled = false;
            verifyBtn.innerHTML = originalText;
        }
    });
}

// 폼 제출 시 로딩 상태 설정
function setLoading(isLoading) {
    const submitBtn = document.querySelector('button[type="submit"]');
    const cancelBtn = document.querySelector('a.btn-outline-secondary');
    
    if (isLoading) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
        if (cancelBtn) cancelBtn.classList.add('disabled');
    } else {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '회원가입 완료';
        if (cancelBtn) cancelBtn.classList.remove('disabled');
    }
}

// 폼 제출 시 유효성 검사
function handleSubmit(event) {
    // 기본 제출 방지 (수동으로 폼을 제출하기 위함)
    event.preventDefault();
    
    // 이메일 검증
    const email = combineEmail();
    if (!email) {
        showMessage('emailCheckResult', '이메일을 입력해주세요.', 'error');
        return false;
    }
    
    // 이메일 인증 여부 확인
    if (!isEmailVerified) {
        showMessage('emailCheckResult', '이메일 인증이 완료되지 않았습니다. 인증을 완료해주세요.', 'error');
        return false;
    }
    
    // 이메일 중복 확인 여부 검증
    if (!isEmailChecked) {
        showMessage('emailCheckResult', '이메일 중복 확인을 해주세요.', 'error');
        return false;
    }
    
    // 이메일 인증 여부 검증
    if (!isEmailVerified) {
        showMessage('emailCheckResult', '이메일 인증을 완료해주세요.', 'error');
        return false;
    }
    
    // 비밀번호 검증
    const password = document.getElementById('password').value;
    if (!password) {
        showMessage('password', '비밀번호를 입력해주세요.', 'error');
        return false;
    }
    
    // 비밀번호 확인
    const confirmPassword = document.getElementById('confirmPassword').value;
    if (password !== confirmPassword) {
        showMessage('confirmPassword', '비밀번호가 일치하지 않습니다.', 'error');
        return false;
    }
    
    // 닉네임 검증
    const nicknameInput = document.getElementById('nickname');
    const nickname = nicknameInput ? nicknameInput.value.trim() : '';
    
    if (!nickname) {
        showMessage('nicknameCheckResult', '닉네임을 입력해주세요.', 'error');
        return false;
    }
    
    // 닉네임 중복 확인 여부 검증
    const lastCheckedNickname = sessionStorage.getItem('lastCheckedNickname');
    if (!lastCheckedNickname || lastCheckedNickname !== nickname) {
        showMessage('nicknameCheckResult', '닉네임 중복 확인을 해주세요.', 'error');
        // 중복 확인 버튼이 있다면 포커스 이동
        const checkBtn = document.getElementById('nicknameCheckBtn');
        if (checkBtn) {
            checkBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return false;
    }
    
    // 국가 선택 검증
    const countryId = document.getElementById('countryId').value;
    if (!countryId) {
        return false;
    }
    
    // 언어 선택 검증
    const languageId = document.getElementById('languageId').value;
    if (!languageId) {
        alert('선호 언어를 선택해주세요.');
        return false;
    }
    
    // 성별 선택 검증
    const gender = document.querySelector('input[name="gender"]:checked');
    if (!gender) {
        alert('성별을 선택해주세요.');
        return false;
    }
    
    // 모든 검증 통과 시 폼 제출
    setLoading(true);
    
    // 폼 제출
    const form = event.target;
    const formData = new FormData(form);
    
    fetch(form.action, {
        method: 'POST',
        body: formData,
        headers: {
            'Accept': 'text/html',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        if (response.redirected) {
            // Get the nickname from the form
            const nicknameInput = document.getElementById('nickname');
            const nickname = nicknameInput ? encodeURIComponent(nicknameInput.value.trim()) : '';
            
            // Add nickname as a query parameter to the redirect URL
            const redirectUrl = new URL(response.url, window.location.origin);
            if (nickname) {
                redirectUrl.searchParams.append('nickname', nickname);
            }
            
            // Redirect to the URL with the nickname parameter
            window.location.href = redirectUrl.toString();
        } else {
            return response.text().then(html => {
                document.documentElement.innerHTML = html;
            });
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
    })
    .finally(() => {
        setLoading(false);
    });
    
    return false;
}

// Handle country selection
function handleCountryChange() {
    const countrySelect = document.getElementById('countryId');
    const countryCodeInput = document.getElementById('countryCode');
    const selectedOption = countrySelect.options[countrySelect.selectedIndex];
    
    if (selectedOption && selectedOption.dataset.countryCode) {
        countryCodeInput.value = selectedOption.dataset.countryCode;
    }
}

// Set maximum date to today for date of birth
function setMaxDate() {
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    if (!dateOfBirthInput) return;
    
    if (!dateOfBirthInput.max) {
        // Set max date to today
        const today = new Date();
        const dd = String(today.getDate()).padStart(2, '0');
        const mm = String(today.getMonth() + 1).padStart(2, '0'); // January is 0!
        const yyyy = today.getFullYear();
        
        // Set max date to today
        dateOfBirthInput.max = `${yyyy}-${mm}-${dd}`;
        
        // Set min date to 100 years ago
        const minDate = new Date();
        minDate.setFullYear(yyyy - 100);
        const minYyyy = minDate.getFullYear();
        dateOfBirthInput.min = `${minYyyy}-01-01`;
    }
    
    // Validate age (at least 14 years old)
    validateAge();
}

// Validate that user is at least 14 years old
function validateAge() {
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    const errorElement = document.getElementById('dateOfBirthError');
    
    if (!dateOfBirthInput.value) {
        errorElement.textContent = '생년월일을 선택해주세요.';
        dateOfBirthInput.setCustomValidity('생년월일을 선택해주세요.');
        return false;
    }
    
    const selectedDate = new Date(dateOfBirthInput.value);
    const today = new Date();
    let age = today.getFullYear() - selectedDate.getFullYear();
    const monthDiff = today.getMonth() - selectedDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < selectedDate.getDate())) {
        age--;
    }
    
    if (age < 14) {
        errorElement.textContent = '만 14세 이상만 가입 가능합니다.';
        dateOfBirthInput.setCustomValidity('만 14세 이상만 가입 가능합니다.');
        return false;
    } else {
        errorElement.textContent = '';
        dateOfBirthInput.setCustomValidity('');
        return true;
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // Initialize country select
    const countrySelect = document.getElementById('countryId');
    
    // Initialize date of birth field
    setMaxDate();
    if (countrySelect) {
        countrySelect.addEventListener('change', handleCountryChange);
        
        // Trigger change event to set initial value
        if (countrySelect.value) {
            handleCountryChange();
        }
    }
    // 이메일 입력 필드에 이벤트 리스너 추가
    const emailId = document.getElementById('emailId');
    const emailDomain = document.getElementById('emailDomain');
    const nickname = document.getElementById('nickname');
    const emailCheckBtn = document.querySelector('button[onclick="checkEmail()"]');
    const nicknameCheckBtn = document.getElementById('nicknameCheckBtn');
    const emailVerifyBtn = document.getElementById('emailVerifyBtn');
    const verifyBtn = document.getElementById('verifyBtn');
    const resendBtn = document.querySelector('.btn-outline-secondary[onclick="resendVerificationCode()"]');
    
    if (emailId && emailDomain) {
        emailId.addEventListener('change', resetEmailCheck);
        emailDomain.addEventListener('change', resetEmailCheck);
    }
    
    if (nickname) {
        nickname.addEventListener('keyup', resetNicknameCheck);
    }
    
    if (emailCheckBtn) {
        emailCheckBtn.addEventListener('click', checkEmail);
        emailCheckBtn.onclick = null; // 기존 인라인 핸들러 제거
    }
    
    if (nicknameCheckBtn) {
        nicknameCheckBtn.addEventListener('click', checkNickname);
        nicknameCheckBtn.onclick = null; // 기존 인라인 핸들러 제거
    }
    
    if (emailVerifyBtn) {
        emailVerifyBtn.addEventListener('click', requestEmailVerification);
        emailVerifyBtn.onclick = null; // 기존 인라인 핸들러 제거
    }
    
    if (verifyBtn) {
        verifyBtn.addEventListener('click', verifyEmail);
        verifyBtn.onclick = null; // 기존 인라인 핸들러 제거
    }
    
    if (resendBtn) {
        resendBtn.addEventListener('click', resendVerificationCode);
        resendBtn.onclick = null; // 기존 인라인 핸들러 제거
    }
    
    // 폼 제출 핸들러
    const form = document.querySelector('form[onsubmit="return handleSubmit()"]');
    if (form) {
        form.onsubmit = handleSubmit;
    }
});
