const {
	ClassicEditor,
	Autoformat,
	AutoImage,
	Autosave,
	BlockQuote,
	Bold,
	Essentials,
	FontBackgroundColor,
	FontColor,
	FontFamily,
	FontSize,
	Heading,
	Highlight,
	ImageBlock,
	ImageCaption,
	ImageInline,
	ImageInsert,
	ImageInsertViaUrl,
	ImageResize,
	ImageStyle,
	ImageTextAlternative,
	ImageToolbar,
	ImageUpload,
	Indent,
	IndentBlock,
	Italic,
	Link,
	LinkImage,
	List,
	MediaEmbed,
	Paragraph,
	SimpleUploadAdapter,
	TextTransformation,
	TodoList,
	Underline
} = window.CKEDITOR;

const editorConfig = {
	toolbar: {
		items: [
			'undo',
			'redo',
			'|',
			'heading',
			'|',
			'fontFamily',
			'fontSize',
			'fontColor',
			'fontBackgroundColor',
			'highlight',
			'|',
			'bold',
			'italic',
			'underline',
			'link',
			'|',
			'bulletedList',
			'numberedList',
			'todoList',
			'|',
			'imageUpload',
			'imageInsert',
			'blockQuote',
			'mediaEmbed',
			'|',
			'indent',
			'outdent'
		]
	},
	language: 'ko',
	image: {
		toolbar: [
			'imageTextAlternative',
			'imageStyle:inline',
			'imageStyle:block',
			'imageStyle:side',
			'linkImage'
		]
	},
	simpleUpload: {
		// 업로드 URL 설정 (백엔드 엔드포인트에 맞게 수정 필요)
		uploadUrl: '/api/upload',
		withCredentials: true,
		headers: {
			'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
		}
	},
	mediaEmbed: {
		previewsInData: true
	}
};

// CKEditor 초기화
ClassicEditor
    .create(document.querySelector('#input-contents'), editorConfig)
    .then(editor => {
        window.editor = editor;
        
        // 저장 버튼 클릭 이벤트
        document.querySelector('.saveButton').addEventListener('click', () => {
            saveStorybook(editor, false);
        });
        
        // 임시저장 버튼 클릭 이벤트
        document.querySelector('.tempButton').addEventListener('click', () => {
            saveStorybook(editor, true);
        });
    })
    .catch(error => {
        console.error('There was a problem initializing the editor.', error);
    });

// 스토리북 저장 함수
function saveStorybook(editor, isTempSave) {
    const title = document.getElementById('title').value;
    const subtitle = document.getElementById('subtitle').value;
    const content = editor.getData();
    
    if (!title) {
        alert('제목을 입력해주세요.');
        return;
    }
    
    if (!content) {
        alert('내용을 입력해주세요.');
        return;
    }
    
    const saveData = {
        title: title,
        subtitle: subtitle,
        content: content,
        savetype: isTempSave ? 'temp' : 'save'
    };
    
    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    // API 호출
    fetch('/storybook/save', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(saveData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(isTempSave ? '임시저장되었습니다.' : '저장되었습니다.');
            if (!isTempSave) {
                // 저장 성공 시 상세 페이지로 이동
                window.location.href = '/storybook/' + data.booknum;
            }
        } else {
            alert('저장 중 오류가 발생했습니다: ' + (data.message || ''));
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('저장 중 오류가 발생했습니다.');
    });
}
