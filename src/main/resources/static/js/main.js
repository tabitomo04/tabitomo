
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

const LICENSE_KEY =
    'eyJhbGciOiJFUzI1NiJ9.eyJleHAiOjE3ODY5MjQ3OTksImp0aSI6Ijg3MjAwYWNhLTBmMzItNGZmOC05MDM5LThlMmY3ZDk0MzI1NSIsImxpY2Vuc2VkSG9zdHMiOlsiMTI3LjAuMC4xIiwibG9jYWxob3N0IiwiMTkyLjE2OC4qLioiLCIxMC4qLiouKiIsIjE3Mi4qLiouKiIsIioudGVzdCIsIioubG9jYWxob3N0IiwiKi5sb2NhbCJdLCJ1c2FnZUVuZHBvaW50IjoiaHR0cHM6Ly9wcm94eS1ldmVudC5ja2VkaXRvci5jb20iLCJkaXN0cmlidXRpb25DaGFubmVsIjpbImNsb3VkIiwiZHJ1cGFsIl0sImxpY2Vuc2VUeXBlIjoiZGV2ZWxvcG1lbnQiLCJmZWF0dXJlcyI6WyJEUlVQIiwiRTJQIiwiRTJXIl0sInZjIjoiOWVlOWQ5MDgifQ.CwfJ4oBTl42JVwiBiw5r57tNeR9TvREoOUnLOmM8esrk8Y7Qh1rxVgFvWS2dajTI8N13y7yPG7cn2_Kf0iLkDg'

const editorConfig = {
	toolbar: {
		items: [
			'undo',
			'redo',
			'|',
			'heading',
			'|',
			'fontSize',
			'fontFamily',
			'fontColor',
			'fontBackgroundColor',
			'|',
			'bold',
			'italic',
			'underline',
			'|',
			'link',
			'insertImage',
			'highlight',
			'blockQuote',
			'|',
			'bulletedList',
			'outdent',
			'indent'
		],
		shouldNotGroupWhenFull: false
	},
	plugins: [
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
	],
	fontFamily: {
		supportAllValues: true
	},
	fontSize: {
		options: [10, 12, 14, 'default', 18, 20, 22],
		supportAllValues: true
	},
	heading: {
		options: [
			{
				model: 'paragraph',
				title: 'Paragraph',
				class: 'ck-heading_paragraph'
			},
			{
				model: 'heading1',
				view: 'h1',
				title: 'Heading 1',
				class: 'ck-heading_heading1'
			},
			{
				model: 'heading2',
				view: 'h2',
				title: 'Heading 2',
				class: 'ck-heading_heading2'
			},
			{
				model: 'heading3',
				view: 'h3',
				title: 'Heading 3',
				class: 'ck-heading_heading3'
			},
			{
				model: 'heading4',
				view: 'h4',
				title: 'Heading 4',
				class: 'ck-heading_heading4'
			},
			{
				model: 'heading5',
				view: 'h5',
				title: 'Heading 5',
				class: 'ck-heading_heading5'
			},
			{
				model: 'heading6',
				view: 'h6',
				title: 'Heading 6',
				class: 'ck-heading_heading6'
			}
		]
	},
	image: {
		toolbar: [
			'toggleImageCaption',
			'imageTextAlternative',
			'|',
			'imageStyle:inline',
			'imageStyle:wrapText',
			'imageStyle:breakText',
			'|',
			'resizeImage'
		]
	},
	language: 'ko',
	licenseKey: LICENSE_KEY,
	link: {
		addTargetToExternalLinks: true,
		defaultProtocol: 'https://',
		decorators: {
			toggleDownloadable: {
				mode: 'manual',
				label: 'Downloadable',
				attributes: {
					download: 'file'
				}
			}
		}
	},
	menuBar: {
		isVisible: true
	},
    placeholder: '당신의 여행을 기록해보세요!',
	table: {
		contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells']
	},
	simpleUpload: {
    		uploadUrl: '/upload'
    		}
    };


let editorInstance;

    // CKEditor 초기화
    ClassicEditor.create(document.querySelector('#input-contents'), {
        ...editorConfig,
        spellcheck: false,
        simpleUpload:{
            uploadUrl : 'http://localhost:8080/upload'
        },
        mediaEmbed: {
            previewsInData: true
        }
    })
        .then(editor => {
            editorInstance = editor;
        })
        .catch(error => {
            console.error(error);
        });


 //유효성 검사
   $(document).ready(function() {
    // 변수 선언: 요소 자체를 변수로 저장
    const titleInput = $('#title');

        // 제목 입력란 blur 이벤트
        titleInput.on('blur', function() {
            if ($(this).val().trim() === '') {
                alert('제목은 필수입력입니다');
            }
        });

    // 저장 버튼 클릭 이벤트
    $('.saveButton').on('click', function() {
        // 클릭 이벤트 시점에 최신 값 가져오기
        const title = $('#title').val();
        const subtitle = $('#subtitle').val();
        const content = editorInstance.getData();

        // 제목 유효성 검사
        if (title === '') {
            alert('제목은 필수입력입니다');
            return false;
        }

        // 내용 유효성 검사
        if (content === '') {
            alert('내용을 입력해주세요.');
            return false;
        }

        const saveRequestDTO = {
                booknum: $('#booknum').val() || null,
                tempId: $('#tempId').val() || null,
                savetype: $('#savetype').val(),
                title: $('#title').val(),
                subtitle: $('#subtitle').val(),
                content: editorInstance.getData()
            };


        // 모든 유효성 검사 통과 후 AJAX 호출
        $.ajax({
            url: '/storybook/save',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(saveRequestDTO),
            success: function(response) {
                if (response.status === "success" && response.booknum) {
                    alert('저장되었습니다.');
                    isEditing = false;
                    window.location.href = '/storybook/detail?booknum=' + response.booknum;
                } else {
                    alert('저장 완료. 그러나 게시물번호를 받을 수 없습니다.');
                }
            },
            error: function(xhr, status, error) {
                alert('저장 실패');
                console.error(error);
            }
        });
    });
});


    // 임시저장 버튼 클릭
    $('.tempButton').on('click', function() {
          const title = $('#title').val();
          const subtitle = $('#subtitle').val();
          const content = editorInstance.getData();

    // 제목 유효성 검사
      if (title === '') {
          alert('제목은 필수입력입니다');
          return false;
      }

      // 내용 유효성 검사
      if (content === '') {
          alert('내용을 입력해주세요.');
          return false;
      }

        const saveRequestDTO = {
                booknum: $('#booknum').val() || null,
                tempId: $('#tempId').val() || null,
                savetype: $('#savetype').val(),
                title: $('#title').val(),
                subtitle: $('#subtitle').val(),
                content: editorInstance.getData()
                  };


      $.ajax ( {
          url: '/storybook/tempsave',       // 서버 API 주소
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(saveRequestDTO),
             success: function(response) {
                if (response.status === "success" && response.tempId) {
                    alert('임시저장되었습니다.');
                    isEditing = false;
                    window.location.href = '/mypage';
                } else {
                    alert('저장 완료. 그러나 게시물번호를 받을 수 없습니다.');
                        }
             },
             error: function(xhr, status, error) {
                alert('저장 실패');
                console.error(error);
             }
      });

    });



/* storyview.html JS */

// 유튜브 출력 코드
    $(document).ready(function() {
        document.querySelectorAll('oembed[url]').forEach(element => {
            var url = element.getAttribute('url');
            var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
            var matchs = url.match(regExp);
            if(matchs && matchs[7]){
                element.parentElement.outerHTML = `
                <div style="position: relative; height: 0; padding-bottom: 56.25%;">
                    <iframe src="https://www.youtube.com/embed/${matchs[7]}?rel=0"
                            style="position: absolute; width: 100%; height: 100%; top: 0; left: 0;"
                            frameborder="0" allow="autoplay; encrypted-media" allowfullscreen>
                    </iframe>
                </div>`;
            }
        });
    });

    // 메뉴 클릭 시 토글메뉴 열고 닫기
    function toggleDropdown(el) {
    const menu = el.nextElementSibling;
    if (menu.style.display === 'block') {
        menu.style.display = 'none';
    } else {
        // 다른 열려 있는 메뉴 닫기
        //document.querySelectorAll('.dropdown-content').forEach(m => m.style.display = 'none');
        menu.style.display = 'block';
    }
}

    // 다른 부분 클릭 시 토글메뉴 닫기
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.more-btn')) {
            document.querySelectorAll('.dropdown-content').forEach(m => m.style.display = 'none');
        }
    });


