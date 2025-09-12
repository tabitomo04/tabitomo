 // YouTube URL 처리 유틸리티
const MediaUtils = {
    // YouTube URL에서 비디오 ID 추출
    extractYoutubeVideoId: function(url) {
        if (!url) return null;
        
        const regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
        const match = url.match(regExp);
        return (match && match[7]) ? match[7] : null;
    },
    
    // YouTube URL을 iframe으로 변환
    convertYoutubeLinkToIframe: function(url, options = {}) {
        const videoId = this.extractYoutubeVideoId(url);
        if (!videoId) return '';
        
        const { width = '100%', height = '100%', autoplay = 0, rel = 0 } = options;
        const embedUrl = `https://www.youtube.com/embed/${videoId}?rel=${rel}&autoplay=${autoplay}`;
        
        return `
            <div class="youtube-embed" style="position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; max-width: 100%;">
                <iframe 
                    src="${embedUrl}"
                    style="position: absolute; top: 0; left: 0; width: ${width}; height: ${height};"
                    frameborder="0" 
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                    allowfullscreen>
                </iframe>
            </div>`;
    },
    
    // 페이지 내 모든 YouTube oembed 요소 변환
    convertAllYoutubeEmbeds: function() {
        document.querySelectorAll('oembed[url]').forEach(element => {
            const url = element.getAttribute('url');
            const iframeHtml = this.convertYoutubeLinkToIframe(url);
            if (iframeHtml) {
                element.outerHTML = iframeHtml;
            }
        });
    },
    
    // 미디어 URL을 적절한 임베드로 변환 (향후 확장 가능)
    convertMediaUrl: function(url, options = {}) {
        if (this.isYoutubeUrl(url)) {
            return this.convertYoutubeLinkToIframe(url, options);
        }
        // 다른 미디어 공급자에 대한 처리 추가 가능 (Vimeo, Dailymotion 등)
        return '';
    },
    
    // URL이 YouTube URL인지 확인
    isYoutubeUrl: function(url) {
        return url && (url.includes('youtube.com') || url.includes('youtu.be'));
    }
};

// 페이지 로드 시 자동으로 YouTube 임베드 변환
if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', function() {
        MediaUtils.convertAllYoutubeEmbeds();
    });
}

// 전역에서 사용할 수 있도록 내보내기
if (typeof window !== 'undefined') {
    window.MediaUtils = MediaUtils;
}

export default MediaUtils;