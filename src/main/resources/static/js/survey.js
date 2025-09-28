/**
 * Survey Modal Handling
 * This script handles the display of the survey modal based on the user's questionnaire completion status.
 */

document.addEventListener('DOMContentLoaded', function() {
    // Check if the survey modal element exists on the page
    const surveyModal = document.getElementById('surveyModal');
    if (!surveyModal) return;

    // Get the current session data
    fetch('/auth/session')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch session data');
            }
            return response.json();
        })
        .then(data => {
            console.log('Session data:', data);
            
            // Check if the user has completed the questionnaire
            const showQuestionnaire = data.showQuestionnairePrompt === true && 
                                    data.questionnaireCompleted === false;
            
            console.log('Should show questionnaire prompt:', showQuestionnaire);
            
            // Only show the modal if the user needs to complete the questionnaire
            if (showQuestionnaire) {
                // Initialize and show the modal
                const modal = new bootstrap.Modal(surveyModal);
                modal.show();
                
                // Log for debugging
                console.log('Showing survey modal for user:', data.email || 'unknown');
            }
        })
        .catch(error => {
            console.error('Error checking questionnaire status:', error);
        });

    // Handle the survey start button click
    const startSurveyBtn = document.getElementById('startSurveyBtn');
    if (startSurveyBtn) {
        startSurveyBtn.addEventListener('click', function() {
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
        });
    }
});
