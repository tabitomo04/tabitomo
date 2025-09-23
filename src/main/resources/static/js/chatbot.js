// Initialize when DOM is loaded
// 파일을 로드하는 역할

    document.addEventListener('DOMContentLoaded', function() {

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