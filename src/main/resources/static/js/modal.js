 // 모달창 js

 // 작성 도중 페이지를 떠날 때 모달
      let isEditing = false;

      // 입력 감지 (제목, 부제목, 내용)
      function checkEditing() {
          const title = document.querySelector('#title').value.trim();
          const subtitle = document.querySelector('#subtitle').value.trim();
          const content = editorInstance.getData().trim();


      // 셋 다 공란이면 false(페이지 떠나기 가능), 하나라도 입력되면 true(모달창 활성화)
      isEditing = (title !== '' || subtitle !== '' || content !== '');
      console.log(isEditing);
      }

      // 이벤트 등록
      // 입력 감지되면 checkEditing() 함수로 이동
      document.querySelector('#title').addEventListener('input', checkEditing);
      document.querySelector('#subtitle').addEventListener('input', checkEditing);
      // 에디터 데이터 내용 변경 감지
      editorInstance.model.document.on('change:data', () => {checkEditing();});

      // beforeunload 이벤트 (페이지를 떠날 때 경고창)
      window.addEventListener('beforeunload', function(e) {
          // isEditing = true인 경우 경고띄움
          if (isEditing) {
              e.preventDefault();
              e.returnValue = '';
          }
      });

      // 페이지 떠날 시 임시저장을 추천하는 커스텀 모달
      function LeaveModal(callback) {
          const modal = document.getElementById('leaveModal');
          const msg = document.getElementById('modalMessage');
          modal.style.display = 'block';
          msg.textContent = "계속 작성하시겠습니까?";

          const yesBtn = document.getElementById('yesBtn');
          const noBtn = document.getElementById('noBtn');
          // Yes → 작성 페이지에서 벗어나지 않음
          yesBtn.onclick = () => {
              modal.style.display = 'none';
              callback('yes');
          };

          // No → "임시저장 하시겠습니까?"
          noBtn.onclick = () => {
              msg.textContent = "임시저장 하시겠습니까?";
              // Yes → 임시저장 후 이동
              yesBtn.onclick = () => {
                  modal.style.display = 'none';
                  isEditing = false;
                  callback('saveTemp');
              };
              // No → 저장하지 않고 바로 이동
              noBtn.onclick = () => {
                  modal.style.display = 'none';
                  isEditing = false;
                  callback('no');
              };
          };
      }
      // 페이지 떠날 시 저장을 추천하는 커스텀 모달
      function LeaveSaveModal(callback) {
                const modal = document.getElementById('leavesaveModal');
                const msg = document.getElementById('modalsaveMessage');
                modal.style.display = 'block';
                msg.textContent = "변경 사항을 저장하시겠습니까?";

                const yesBtn = document.getElementById('saveBtn');
                const noBtn = document.getElementById('nosaveBtn');
                const closeBtn = document.getElementById('closeBtn');
                // Yes → 변경사항 저장
                yesBtn.onclick = () => {
                    modal.style.display = 'none';
                    isEditing = false;
                    callback('save');
                };

                // No → 저장없이 페이지 이동
                noBtn.onclick = () => {
                   modal.style.display = 'none';
                   isEditing = false;
                   callback('nosave');
                };

                // Close → 현재 페이지에 머무르기
                closeBtn.onclick = () => {
                   modal.style.display = 'none';
                   isEditing = false;
                   callback('close');
                };

               }

      // 다른 페이지로 이동하는 태크 클릭 시 모달 띄우기 (현재는 a태그로 설정)
      document.querySelectorAll('a').forEach(link => {
          link.addEventListener('click', function(e) {
              if (isEditing) {
                  e.preventDefault();

                  // savetype 가져와서 temp인 경우는 임시저장, post인 경우 저장
                  const savetype = document.querySelector('#savetype')?.value;

                  if (savetype === 'temp') {
                   LeaveModal(function(result) {
                        if (result === 'yes') {
                            // 페이지 이동 안함
                        } else if (result === 'saveTemp') {
                            // 임시저장 AJAX 호출 후 이동
                            tempsave().then(() => window.location.href = link.href);
                        } else if (result === 'no') {
                            // 저장하지 않고 이동
                            window.location.href = link.href;
                        }
                    });

                  }
                  else if(savetype === 'post') {
                  LeaveSaveModal(function(result){
                         if (result === 'close') {
                              // 페이지 이동 안함
                          } else if (result === 'save') {
                              // 임시저장 AJAX 호출 후 이동
                              save().then(() => window.location.href = link.href);
                          } else if (result === 'nosave') {
                              // 저장하지 않고 이동
                              window.location.href = link.href;
                          }
                  });
                  }//if문 끝

              }//isEditing if문 끝
          });
      });

      // 저장 AJAX 함수
        function save() {
            //저장 후 페이지 이동
            return new Promise((resolve, reject) => {
                const saveRequestDTO = {
                   booknum: $('#booknum').val(),
                   savetype: $('#savetype').val(),
                   title: $('#title').val(),
                   subtitle: $('#subtitle').val(),
                   content: editorInstance.getData()
                };

                $.ajax({
                    url: '/save',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(saveRequestDTO),
                    success: function(response) {
                        alert('저장 완료!');
                        resolve(response);
                    },
                    error: function() {
                        alert('저장 실패!');
                        reject();
                    }
                });
            });
        }

      // 임시저장 AJAX 함수
      function tempsave() {
          //임시저장 후 페이지 이동
          return new Promise((resolve, reject) => {
              const saveRequestDTO = {
                 tempId: $('#tempId').val(),
                 savetype: $('#savetype').val(),
                 title: $('#title').val(),
                 subtitle: $('#subtitle').val(),
                 content: editorInstance.getData()
              };

              $.ajax({
                  url: '/tempsave',
                  type: 'POST',
                  contentType: 'application/json',
                  data: JSON.stringify(saveRequestDTO),
                  success: function(response) {
                      alert('임시저장 완료!');
                      resolve(response);
                  },
                  error: function() {
                      alert('임시저장 실패!');
                      reject();
                  }
              });
          });
      }

 // url 공유 모달창 js
   function clip(){
       var url = '';
       var textarea = document.createElement("textarea");
       document.body.appendChild(textarea);
       url = window.document.location.href;
       textarea.value = url;
       textarea.select();
       document.execCommand("copy");
       document.body.removeChild(textarea);

       // 툴팁 표시
        const btn = document.querySelector(".urlbtn");
       btn.classList.add("show");

      // 2초 후 툴팁 숨기기
      setTimeout(() => {
          btn.classList.remove("show");
      }, 2000);
     }



