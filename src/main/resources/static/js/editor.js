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

// 이미지 업로드 어댑터 설정
class CustomUploadAdapter {
    constructor(loader) {
        this.loader = loader;
    }

    upload() {
        return this.loader.file
            .then(file => new Promise((resolve, reject) => {
                const formData = new FormData();
                formData.append('upload', file);
                
                // Use PathConstants for the upload URL
                const uploadUrl = PathConstants.API.UPLOAD;

                const xhr = new XMLHttpRequest();
                xhr.open('POST', uploadUrl, true);
                xhr.setRequestHeader('X-CSRF-TOKEN', document.querySelector('meta[name="_csrf"]').getAttribute('content'));
                
                xhr.onload = () => {
                    const response = JSON.parse(xhr.responseText);
                    if (xhr.status === 200 && response.uploaded) {
                        resolve({
                            default: response.url
                        });
                    } else {
                        reject(response.error?.message || '이미지 업로드에 실패했습니다.');
                    }
                };

                xhr.onerror = () => reject('네트워크 오류가 발생했습니다.');
                xhr.send(formData);
            }));
    }
}

const editorConfig = {
    toolbar: {
        items: [
            'undo', 'redo', '|',
            'heading', '|',
            'fontFamily', 'fontSize', 'fontColor', 'fontBackgroundColor', 'highlight', '|',
            'bold', 'italic', 'underline', 'strikethrough', '|',
            'link', 'blockQuote', '|',
            'bulletedList', 'numberedList', 'todoList', '|',
            'imageUpload', 'imageInsert', 'mediaEmbed', '|',
            'indent', 'outdent', '|',
            'alignment', '|',
            'horizontalLine', '|',
            'code', 'codeBlock', '|',
            'removeFormat'
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
		uploadUrl: PathConstants.API.UPLOAD,
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
        
        // 에디터에 커스텀 업로드 어댑터 설정
        editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
            return new CustomUploadAdapter(loader);
        };
        
        // 에디터에 데이터 로드 (수정 모드일 경우)
        const bookNum = document.getElementById('bookNum')?.value;
        if (bookNum) {
            loadStorybook(editor, bookNum);
        }
    })
    .catch(error => {
        console.error('에디터 초기화 중 오류가 발생했습니다.', error);
        alert('에디터를 로드하는 중 오류가 발생했습니다. 페이지를 새로고침 해주세요.');
    });

// 스토리북 저장 함수
function saveStorybook(editor, isTempSave) {
    const title = document.getElementById('title').value;
    const subtitle = document.getElementById('subtitle').value;
    const content = editor.getData();
    const bookNum = document.getElementById('bookNum')?.value;
    
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
        savetype: isTempSave ? 'temp' : 'save',
        booknum: bookNum ? parseInt(bookNum) : null,
        tempId: document.getElementById('tempId')?.value
    };
    
    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    // 로딩 상태 표시
    const saveButton = document.querySelector(isTempSave ? '.tempButton' : '.saveButton');
    const originalText = saveButton.textContent;
    saveButton.disabled = true;
    saveButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 저장 중...';
    
    // API 호출
    const saveUrl = isTempSave ? PathConstants.STORYBOOK.TEMPSAVE : PathConstants.STORYBOOK.BASE + '/save';
    fetch(saveUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(saveData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw new Error(err.message || '서버 오류가 발생했습니다.'); });
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            alert(isTempSave ? '임시저장되었습니다.' : '저장되었습니다.');
            
            // 수정 모드가 아니고 새로 저장하는 경우에만 URL 업데이트
            if (!bookNum && data.booknum) {
                const newUrl = window.location.pathname + (window.location.search ? '&' : '?') + 'bookNum=' + data.booknum;
                window.history.replaceState({}, '', newUrl);
            }
            
            if (!isTempSave && data.booknum) {
                // 저장 성공 시 상세 페이지로 이동
                window.location.href = '/storybook/' + data.booknum;
            } else if (data.tempId) {
                // 임시저장 ID 업데이트
                const tempIdInput = document.getElementById('tempId') || document.createElement('input');
                tempIdInput.type = 'hidden';
                tempIdInput.id = 'tempId';
                tempIdInput.value = data.tempId;
                document.querySelector('form').appendChild(tempIdInput);
            }
        } else {
            throw new Error(data.message || '저장에 실패했습니다.');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('저장 중 오류가 발생했습니다: ' + error.message);
    })
    .finally(() => {
        // 버튼 상태 복원
        saveButton.disabled = false;
        saveButton.textContent = originalText;
    });
}

// 스토리북 로드 함수 (수정 모드용)
function loadStorybook(editor, bookNum) {
    fetch(PathConstants.STORYBOOK.DETAIL(bookNum), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('스토리북을 불러오는데 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        if (data) {
            document.getElementById('title').value = data.title || '';
            document.getElementById('subtitle').value = data.subtitle || '';
            editor.setData(data.content || '');
            
            // 임시저장 ID가 있는 경우 설정
            if (data.tempId) {
                const tempIdInput = document.createElement('input');
                tempIdInput.type = 'hidden';
                tempIdInput.id = 'tempId';
                tempIdInput.value = data.tempId;
                document.querySelector('form').appendChild(tempIdInput);
            }
        }
    })
    .catch(error => {
        console.error('Error loading storybook:', error);
        alert('스토리북을 불러오는 중 오류가 발생했습니다.');
    });
}
