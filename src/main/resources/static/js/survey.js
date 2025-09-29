/**
 * Survey Modal Handling
 * This script handles the display of the survey modal based on the user's questionnaire completion status.
 */

document.addEventListener('DOMContentLoaded', function() {
    // Check if the survey modal element exists on the page
    const surveyModal = document.getElementById('surveyModal');
    if (!surveyModal) return;

    // Check if we've already run the questionnaire check in this session
    if (sessionStorage.getItem('questionnaireCheckDone')) {
        console.log('Questionnaire check already performed in this session');
        return;
    }

    // Function to check and update questionnaire status
    function checkQuestionnaireStatus() {
        // Add cache-busting parameter to prevent caching
        const timestamp = new Date().getTime();
        
        // Get the current session data with cache control
        fetch(`/auth/session?_=${timestamp}`, {
            method: 'GET',
            headers: {
                'Cache-Control': 'no-cache, no-store, must-revalidate',
                'Pragma': 'no-cache',
                'Expires': '0'
            },
            credentials: 'same-origin' // Include cookies with the request
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Session data:', data);
            
            // Mark that we've performed the check for this session
            sessionStorage.setItem('questionnaireCheckDone', 'true');
            
            // Check if the user is authenticated and questionnaire data is available
            if (!data.isAuthenticated) {
                console.log('User is not authenticated, skipping questionnaire check');
                return;
            }
            
            // Check if the user has completed the questionnaire
            const showQuestionnaire = data.isAuthenticated && 
                                   data.showQuestionnairePrompt === true && 
                                   data.questionnaireCompleted === false;
            
            console.log('Questionnaire prompt status:', 
                       'show:', showQuestionnaire, 
                       'authenticated:', data.isAuthenticated,
                       'prompt:', data.showQuestionnairePrompt,
                       'completed:', data.questionnaireCompleted);
            
            // Only show the modal if the user needs to complete the questionnaire
            if (showQuestionnaire) {
                // Initialize and show the modal
                const modal = new bootstrap.Modal(surveyModal, {
                    backdrop: 'static', // Prevent closing by clicking outside
                    keyboard: false     // Prevent closing with ESC key
                });
                modal.show();
                
                // Log for debugging
                console.log('Showing survey modal for user:', data.email || 'unknown');
            } else if (data.isAuthenticated && data.questionnaireCompleted) {
                // If questionnaire is completed, ensure the modal is hidden
                const modal = bootstrap.Modal.getInstance(surveyModal);
                if (modal) {
                    modal.hide();
                }
            }
        })
        .catch(error => {
            console.error('Error checking questionnaire status:', error);
        });
    }

    // Initial check
    checkQuestionnaireStatus();

    // Set up periodic check every 30 seconds (adjust as needed)
    const checkInterval = setInterval(checkQuestionnaireStatus, 30000);

    // Handle the survey start button click
    const startSurveyBtn = document.getElementById('startSurveyBtn');
    if (startSurveyBtn) {
        startSurveyBtn.addEventListener('click', function() {
            // Hide the modal before redirecting
            const modal = bootstrap.Modal.getInstance(surveyModal);
            if (modal) {
                modal.hide();
            }
            window.location.href = '/question/start';
        });
    }

    // Handle the close button
    const closeSurveyBtn = document.getElementById('closeSurveyBtn');
    if (closeSurveyBtn) {
        closeSurveyBtn.addEventListener('click', function() {
            const modal = bootstrap.Modal.getInstance(surveyModal);
            if (modal) {
                modal.hide();
            }
            
            // Update the session to indicate the user has seen the prompt
            fetch('/api/update-session-prompt', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin',
                body: JSON.stringify({ showQuestionnairePrompt: false })
            })
            .then(response => response.json())
            .then(data => {
                console.log('Session updated:', data);
            })
            .catch(error => {
                console.error('Error updating session:', error);
            });
        });
    }
    
    // Handle modal hidden event to prevent reopening
    surveyModal.addEventListener('hidden.bs.modal', function () {
        // Clean up the modal instance
        const modal = bootstrap.Modal.getInstance(surveyModal);
        if (modal) {
            modal.dispose();
        }
    });
});
