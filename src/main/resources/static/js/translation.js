document.addEventListener('DOMContentLoaded', function () {
    const currentLangKey = 'selectedLanguage';

    window.translationManager = {
        currentLanguage: localStorage.getItem(currentLangKey) || 'ko',
        isReady: false,

        async translatePage(targetLang, rootElement = document) {
            if (!targetLang || targetLang === 'ko') {
                this.restoreOriginalText(rootElement);
                return;
            }

            const elementsToTranslate = rootElement.querySelectorAll('[data-translatable]');
            const placeholderElements = rootElement.querySelectorAll('[data-translatable-placeholder]');

            const texts = [];
            elementsToTranslate.forEach(el => {
                if (!el.dataset.originalText) el.dataset.originalText = el.textContent.trim();
                if (el.dataset.originalText) texts.push(el.dataset.originalText);
            });
            placeholderElements.forEach(el => {
                if (!el.dataset.originalPlaceholder) el.dataset.originalPlaceholder = el.placeholder.trim();
                if (el.dataset.originalPlaceholder) texts.push(el.dataset.originalPlaceholder);
            });

            if (texts.length === 0) return;

            try {
                const response = await fetch('/api/translate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ texts: texts, targetLanguage: targetLang }),
                });

                if (!response.ok) throw new Error('Translation API request failed');

                const result = await response.json();
                let translatedIndex = 0;

                elementsToTranslate.forEach(el => {
                    if (el.dataset.originalText) {
                        el.textContent = result.translatedTexts[translatedIndex++];
                    }
                });
                placeholderElements.forEach(el => {
                    if (el.dataset.originalPlaceholder) {
                        el.placeholder = result.translatedTexts[translatedIndex++];
                    }
                });

            } catch (error) {
                console.error('Translation error:', error);
            }
        },

        restoreOriginalText(rootElement = document) {
            rootElement.querySelectorAll('[data-translatable]').forEach(el => {
                if (el.dataset.originalText) el.textContent = el.dataset.originalText;
            });
            rootElement.querySelectorAll('[data-translatable-placeholder]').forEach(el => {
                if (el.dataset.originalPlaceholder) el.placeholder = el.dataset.originalPlaceholder;
            });
        },

        applyCurrentLanguage(rootElement = document) {
            const savedLang = localStorage.getItem(currentLangKey) || 'ko';
            this.currentLanguage = savedLang;
            document.documentElement.lang = savedLang;
            
            if (savedLang && savedLang !== 'ko') {
                this.translatePage(savedLang, rootElement);
            } else {
                this.restoreOriginalText(rootElement);
            }
        }
    };

    // --- Event Handling ---
    const dropdown = document.getElementById('translation-dropdown');
    if (dropdown) {
        dropdown.addEventListener('click', function (e) {
            const target = e.target;
            if (target.classList.contains('dropdown-item')) {
                e.preventDefault();
                const selectedLang = target.dataset.lang;
                localStorage.setItem(currentLangKey, selectedLang);
                window.translationManager.currentLanguage = selectedLang;

                if (selectedLang === 'ko') {
                    window.translationManager.restoreOriginalText(document);
                } else {
                    window.translationManager.translatePage(selectedLang, document);
                }
                
                // Announce the language change to other scripts
                document.dispatchEvent(new CustomEvent('languageChanged', { detail: { lang: selectedLang } }));
            }
        });
    }

    // --- Initial Load ---
    setTimeout(() => {
        window.translationManager.applyCurrentLanguage(document);
        window.translationManager.isReady = true;
        // Announce that the translation manager is ready
        document.dispatchEvent(new Event('translationManagerReady'));
    }, 100);
});
