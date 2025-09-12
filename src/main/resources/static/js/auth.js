/**
 * Authentication JavaScript for TabiTomo
 * Handles client-side form validation, API calls, and user interactions
 * for authentication-related pages (login, signup, password reset, etc.)
 * 
 * Uses PathConstants for API endpoints to maintain consistency with the backend.
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize password visibility toggles
    initPasswordToggles();
    
    // Initialize form validation
    initFormValidation();
    
    // Initialize country and language selects if they exist on the page
    if (document.getElementById('country') || document.getElementById('language')) {
        initCountryAndLanguageSelects();
    }
    
    // Initialize email domain handling if on signup page
    if (document.getElementById('emailDomain')) {
        initEmailDomainHandler();
    }
    
    // Initialize password strength meter if on signup or reset password page
    if (document.getElementById('password')) {
        initPasswordStrengthMeter();
    }
});

/**
 * Initialize password visibility toggles
 */
function initPasswordToggles() {
    document.querySelectorAll('.password-toggle').forEach(toggle => {
        toggle.addEventListener('click', function() {
            const input = this.previousElementSibling;
            const icon = this.querySelector('i');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('bi-eye');
                icon.classList.add('bi-eye-slash');
                this.setAttribute('aria-label', '비밀번호 숨기기');
            } else {
                input.type = 'password';
                icon.classList.remove('bi-eye-slash');
                icon.classList.add('bi-eye');
                this.setAttribute('aria-label', '비밀번호 보기');
            }
        });
    });
}

/**
 * Initialize form validation
 */
function initFormValidation() {
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    const forms = document.querySelectorAll('.needs-validation');
    
    // Loop over them and prevent submission
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
}

/**
 * Initialize country and language select elements
 */
function initCountryAndLanguageSelects() {
    // In a real app, you would fetch these from your API
    const countries = [
        { id: 1, name: '대한민국', code: 'KR' },
        { id: 2, name: '미국', code: 'US' },
        { id: 3, name: '일본', code: 'JP' },
        { id: 4, name: '중국', code: 'CN' },
        { id: 5, name: '베트남', code: 'VN' },
        { id: 6, name: '태국', code: 'TH' },
        { id: 7, name: '필리핀', code: 'PH' },
        { id: 8, name: '인도네시아', code: 'ID' },
        { id: 9, name: '말레이시아', code: 'MY' },
        { id: 10, name: '싱가포르', code: 'SG' }
    ];
    
    const languages = [
        { id: 1, name: '한국어', code: 'ko' },
        { id: 2, name: 'English', code: 'en' },
        { id: 3, name: '日本語', code: 'ja' },
        { id: 4, name: '中文', code: 'zh' },
        { id: 5, name: 'Tiếng Việt', code: 'vi' },
        { id: 6, name: 'ภาษาไทย', code: 'th' },
        { id: 7, name: 'Filipino', code: 'fil' },
        { id: 8, name: 'Bahasa Indonesia', code: 'id' },
        { id: 9, name: 'Bahasa Melayu', code: 'ms' }
    ];
    
    // Populate country select
    const countrySelect = document.getElementById('country');
    const languageSelect = document.getElementById('language');
    
    // Add loading state
    if (countrySelect) countrySelect.disabled = true;
    if (languageSelect) languageSelect.disabled = true;
    
    // Fetch countries and languages from the API using PathConstants
    Promise.all([
        fetch(PathConstants.API.REFERENCE.COUNTRIES).then(res => res.json()),
        fetch(PathConstants.API.REFERENCE.LANGUAGES).then(res => res.json())
    ]).then(([countries, languages]) => {
        // Populate country select
        countries.forEach(country => {
            const option = document.createElement('option');
            option.value = country.id;
            option.textContent = country.name;
            countrySelect.appendChild(option);
        });
        
        // Populate language select
        languages.forEach(language => {
            const option = document.createElement('option');
            option.value = language.id;
            option.textContent = language.name;
            languageSelect.appendChild(option);
        });
        
        // Remove loading state
        if (countrySelect) countrySelect.disabled = false;
        if (languageSelect) languageSelect.disabled = false;
    });
}

/**
 * Initialize email domain handler for signup form
 */
function initEmailDomainHandler() {
    const emailInput = document.getElementById('email');
    const emailDomain = document.getElementById('emailDomain');
    
    if (!emailInput || !emailDomain) return;
    
    // When domain is selected, update email input
    emailDomain.addEventListener('change', function() {
        if (this.value) {
            const emailParts = emailInput.value.split('@');
            emailInput.value = emailParts[0] + this.value;
        }
    });
    
    // When user types in email input, update domain select if it matches a known domain
    emailInput.addEventListener('blur', function() {
        const email = this.value.trim();
        if (!email) return;
        
        const atIndex = email.lastIndexOf('@');
        if (atIndex === -1) return;
        
        const domain = email.substring(atIndex);
        const domainOption = Array.from(emailDomain.options).find(option => option.value === domain);
        
        if (domainOption) {
            emailDomain.value = domain;
        } else if (emailDomain.value) {
            emailDomain.value = ''; // Reset to "직접입력"
        }
    });
}

/**
 * Initialize password strength meter
 */
function initPasswordStrengthMeter() {
    const passwordInput = document.getElementById('password');
    const strengthMeter = document.createElement('div');
    strengthMeter.className = 'password-strength mt-2';
    const strengthBar = document.createElement('div');
    strengthBar.className = 'password-strength-bar';
    strengthMeter.appendChild(strengthBar);
    
    const strengthText = document.createElement('small');
    strengthText.className = 'd-block mt-1 text-muted';
    
    passwordInput.parentNode.insertBefore(strengthMeter, passwordInput.nextSibling);
    passwordInput.parentNode.insertBefore(strengthText, strengthMeter.nextSibling);
    
    passwordInput.addEventListener('input', function() {
        const password = this.value;
        const strength = calculatePasswordStrength(password);
        
        // Update strength meter
        strengthMeter.className = 'password-strength mt-2';
        strengthBar.style.width = '0%';
        
        // Animate the width
        setTimeout(() => {
            strengthBar.style.width = strength.percentage + '%';
            
            if (strength.score <= 2) {
                strengthMeter.classList.add('weak');
                strengthText.textContent = '약한 비밀번호';
                strengthText.className = 'd-block mt-1 text-danger';
            } else if (strength.score <= 3) {
                strengthMeter.classList.add('medium');
                strengthText.textContent = '보통 수준의 비밀번호';
                strengthText.className = 'd-block mt-1 text-warning';
            } else {
                strengthMeter.classList.add('strong');
                strengthText.textContent = '강력한 비밀번호';
                strengthText.className = 'd-block mt-1 text-success';
            }
        }, 10);
    });
}

/**
 * Calculate password strength
 * @param {string} password - The password to check
 * @returns {object} - Object containing score and percentage
 */
function calculatePasswordStrength(password) {
    let score = 0;
    let feedback = [];
    
    if (!password) {
        return { score: 0, percentage: 0, feedback };
    }
    
    // Length check
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    
    // Contains lowercase
    if (/[a-z]/.test(password)) score++;
    
    // Contains uppercase
    if (/[A-Z]/.test(password)) score++;
    
    // Contains number
    if (/\d/.test(password)) score++;
    
    // Contains special character
    if (/[^A-Za-z0-9]/.test(password)) score++;
    
    // Calculate percentage (max 100%)
    const percentage = Math.min(100, Math.round((score / 6) * 100));
    
    return { score, percentage, feedback };
}

/**
 * Show an error message
 * @param {string} message - The error message to display
 * @param {HTMLElement} element - The element to show the error for
 */
function showError(message, element) {
    // If element is provided, show error next to it
    if (element) {
        // Remove any existing error messages
        const existingError = element.parentNode.querySelector('.invalid-feedback');
        if (existingError) {
            existingError.textContent = message;
            return;
        }
        
        // Add error class to input
        element.classList.add('is-invalid');
        
        // Create and append error message
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        errorDiv.textContent = message;
        
        // Insert after the input
        element.parentNode.insertBefore(errorDiv, element.nextSibling);
    } else {
        // Show as alert if no element is provided
        alert(message);
    }
}

/**
 * Clear all error messages
 * @param {HTMLElement} form - The form to clear errors from
 */
function clearErrors(form) {
    // Remove all error messages
    form.querySelectorAll('.invalid-feedback').forEach(el => el.remove());
    
    // Remove error classes from inputs
    form.querySelectorAll('.is-invalid').forEach(el => {
        el.classList.remove('is-invalid');
    });
}

/**
 * Handle form submission with fetch API
 * @param {Event} event - The form submit event
 * @param {string} url - The URL to submit to
 * @param {Function} onSuccess - Callback for successful submission
 * @param {Function} onError - Callback for submission error
 */
function handleFormSubmit(event, url, onSuccess, onError) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    const submitButton = form.querySelector('button[type="submit"]');
    const originalButtonText = submitButton ? submitButton.innerHTML : '';
    
    // Show loading state
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
    }
    
    // Clear previous errors
    clearErrors(form);
    
    // Use PathConstants if url is a key in PathConstants
    const finalUrl = PathConstants[url] || url;
    
    // Convert form data to JSON
    const jsonData = {};
    formData.forEach((value, key) => {
        jsonData[key] = value;
    });
    
    // Submit the form data
    fetch(finalUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(jsonData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw err;
            });
        }
        return response.json();
    })
    .then(data => {
        if (onSuccess) onSuccess(data);
    })
    .catch(error => {
        console.error('Error:', error);
        if (onError) {
            onError(error);
        } else {
            // Default error handling
            showError(error.message || '오류가 발생했습니다. 다시 시도해주세요.');
        }
    })
    .finally(() => {
        // Reset button state
        submitButton.disabled = false;
        submitButton.innerHTML = originalButtonText;
    });
}

// Export functions for use in inline scripts
window.authUtils = {
    showError,
    clearErrors,
    handleFormSubmit,
    calculatePasswordStrength
};
