// Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        // Show survey modal if the flag is set
        const surveyModal = document.getElementById('surveyModal');
        if (surveyModal) {
            const modal = new bootstrap.Modal(surveyModal);
            modal.show();

            // Clear the flag if user dismisses the modal
            surveyModal.addEventListener('hidden.bs.modal', function() {
                fetch('/auth/clear-questionnaire-prompt', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                    },
                    credentials: 'same-origin'
                });
            });
        }

        // 챗봇 iframe 토글 기능
        const chatbotToggle = document.getElementById('chatbotToggle');
        const chatbotContainer = document.getElementById('chatbotIframeContainer');
        const closeChatbot = document.getElementById('closeChatbot');

        // Toggle chatbot visibility
        if (chatbotToggle) {
            chatbotToggle.addEventListener('click', function() {
                const isActive = chatbotContainer.classList.toggle('active');
                // 닫기 버튼 표시/숨김
                if (isActive) {
                    closeChatbot.style.display = 'flex';
                } else {
                    closeChatbot.style.display = 'none';
                }
            });
        }

        // Close chatbot
        if (closeChatbot) {
            closeChatbot.addEventListener('click', function(e) {
                e.stopPropagation(); // 이벤트 버블링 방지
                chatbotContainer.classList.remove('active');
                closeChatbot.style.display = 'none';
            });
        }

        // Close when clicking outside
        document.addEventListener('click', function(event) {
            if (chatbotContainer &&
                !chatbotContainer.contains(event.target) &&
                !chatbotToggle.contains(event.target) &&
                !closeChatbot.contains(event.target) &&
                chatbotContainer.classList.contains('active')) {
                chatbotContainer.classList.remove('active');
                closeChatbot.style.display = 'none';
            }
        });

        // 초기에 닫기 버튼 숨기기
        if (closeChatbot) {
            closeChatbot.style.display = 'none';
        }
    });

    // 메시지 추가
    function appendMessage(sender, text) {
        const chatBody = document.getElementById('chatbotBody');
        const messageDiv = document.createElement('div');
        messageDiv.className = `chatbot-message ${sender}-message`;

        if (sender === 'user') {
            messageDiv.style.justifyContent = 'flex-end';
            messageDiv.innerHTML = `
                <div class="chatbot-text">
                    <p style="background-color: #e3f2fd;">${text}</p>
                </div>
                <div class="chatbot-avatar" style="background-color: #6c757d; margin-right: 0; margin-left: 10px;">
                    <i class="fas fa-user"></i>
                </div>
            `;
        } else {
            messageDiv.innerHTML = `
                <div class="chatbot-avatar">
                    <i class="fas fa-robot"></i>
                </div>
                <div class="chatbot-text">
                    <p>${text}</p>
                </div>
            `;
        }

        chatBody.appendChild(messageDiv);
        chatBody.scrollTop = chatBody.scrollHeight;
    }