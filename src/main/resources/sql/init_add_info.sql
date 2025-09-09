-- 취미 정보 (info_high_num = 1)
INSERT INTO add_info (info_high_num, info_low_num, content, info_name) VALUES
(1, 1, '독서', 'hobby'),
(1, 2, '영화 감상', 'hobby'),
(1, 3, '음악 듣기', 'hobby'),
(1, 4, '산책', 'hobby'),
(1, 5, '요리', 'hobby'),
(1, 6, '베이킹', 'hobby'),
(1, 7, '등산', 'hobby'),
(1, 8, '자전거 타기', 'hobby'),
(1, 9, '수영', 'hobby'),
(1, 10, '사진 촬영', 'hobby'),
(1, 11, '그림 그리기', 'hobby'),
(1, 12, '캘리그라피', 'hobby'),
(1, 13, '뜨개질', 'hobby'),
(1, 14, '원예', 'hobby'),
(1, 15, '악기 연주', 'hobby'),
(1, 16, '댄스', 'hobby'),
(1, 17, '명상', 'hobby'),
(1, 18, '피규어 수집', 'hobby'),
(1, 19, '플라모델 조립', 'hobby'),
(1, 20, '보드 게임', 'hobby'),
(1, 21, '게임', 'hobby'),
(1, 22, 'DIY 공예', 'hobby'),
(1, 23, '글쓰기', 'hobby'),
(1, 24, '드라마 시청', 'hobby'),
(1, 25, '웹툰 보기', 'hobby'),
(1, 26, '카페 탐방', 'hobby'),
(1, 27, '맛집 탐방', 'hobby'),
(1, 28, '퍼스널 컬러 진단', 'hobby'),
(1, 29, '외국어 공부', 'hobby'),
(1, 30, '코딩', 'hobby'),
(1, 31, '쇼핑', 'hobby'),
(1, 32, '예술 관람', 'hobby'),
(1, 33, '스키/스노보드', 'hobby'),
(1, 34, '낚시', 'hobby'),
(1, 35, '해변가 산책', 'hobby'),
(1, 36, '숲 속 산책', 'hobby'),
(1, 37, '피크닉', 'hobby')
AS new_data
ON DUPLICATE KEY UPDATE 
    content = new_data.content,
    info_name = new_data.info_name;

-- MBTI 유형 (info_high_num = 2)
INSERT INTO add_info (info_high_num, info_low_num, content, info_name) VALUES
(2, 1, 'ISTJ', 'mbti'),
(2, 2, 'ISFJ', 'mbti'),
(2, 3, 'INFJ', 'mbti'),
(2, 4, 'INTJ', 'mbti'),
(2, 5, 'ISTP', 'mbti'),
(2, 6, 'ISFP', 'mbti'),
(2, 7, 'INFP', 'mbti'),
(2, 8, 'INTP', 'mbti'),
(2, 9, 'ESTP', 'mbti'),
(2, 10, 'ESFP', 'mbti'),
(2, 11, 'ENFP', 'mbti'),
(2, 12, 'ENTP', 'mbti'),
(2, 13, 'ESTJ', 'mbti'),
(2, 14, 'ESFJ', 'mbti'),
(2, 15, 'ENFJ', 'mbti'),
(2, 16, 'ENTJ', 'mbti')
AS new_data
ON DUPLICATE KEY UPDATE 
    content = new_data.content,
    info_name = new_data.info_name;

-- 여행 스타일 (info_high_num = 3)
INSERT INTO add_info (info_high_num, info_low_num, content, info_name) VALUES
(3, 1, '휴양', 'travel_style'),
(3, 2, '액티비티', 'travel_style'),
(3, 3, '문화유적 탐방', 'travel_style'),
(3, 4, '미식', 'travel_style'),
(3, 5, '자연 친화', 'travel_style'),
(3, 6, '도시 탐험', 'travel_style'),
(3, 7, '테마파크 투어', 'travel_style'),
(3, 8, '자유', 'travel_style'),
(3, 9, '배낭', 'travel_style'),
(3, 10, '호캉스', 'travel_style'),
(3, 11, '캠핑', 'travel_style'),
(3, 12, '크루즈', 'travel_style'),
(3, 13, '기차', 'travel_style'),
(3, 14, '로드 트립', 'travel_style'),
(3, 15, '겨울 스포츠', 'travel_style'),
(3, 16, '순례길', 'travel_style'),
(3, 17, '역사 탐방', 'travel_style'),
(3, 18, '시장 투어', 'travel_style'),
(3, 19, '온천', 'travel_style'),
(3, 20, '섬', 'travel_style'),
(3, 21, '럭셔리', 'travel_style'),
(3, 22, '서핑', 'travel_style'),
(3, 23, '계곡', 'travel_style'),
(3, 24, '미술관/박물관', 'travel_style'),
(3, 25, '공연/축제', 'travel_style'),
(3, 26, '야시장', 'travel_style'),
(3, 27, '템플 스테이', 'travel_style'),
(3, 28, '골프', 'travel_style'),
(3, 29, '승마', 'travel_style'),
(3, 30, '요트', 'travel_style')
AS new_data
ON DUPLICATE KEY UPDATE 
    content = new_data.content,
    info_name = new_data.info_name;

-- 동반자 유형 (info_high_num = 4)
INSERT INTO add_info (info_high_num, info_low_num, content, info_name) VALUES
(4, 1, '혼자', 'companion'),
(4, 2, '친구와', 'companion'),
(4, 3, '연인과', 'companion'),
(4, 4, '배우자와', 'companion'),
(4, 5, '부모님과', 'companion'),
(4, 6, '자녀와', 'companion'),
(4, 7, '가족 단체', 'companion'),
(4, 8, '반려동물과', 'companion'),
(4, 9, '동호회/모임', 'companion'),
(4, 10, '회사 동료와', 'companion')
AS new_data
ON DUPLICATE KEY UPDATE 
    content = new_data.content,
    info_name = new_data.info_name;

-- 음식 취향 (info_high_num = 5)
INSERT INTO add_info (info_high_num, info_low_num, content, info_name) VALUES
(5, 1, '한식', 'food_preference'),
(5, 2, '중식', 'food_preference'),
(5, 3, '일식', 'food_preference'),
(5, 4, '양식', 'food_preference'),
(5, 5, '분식', 'food_preference'),
(5, 6, '아시아 음식', 'food_preference'),
(5, 7, '채식', 'food_preference'),
(5, 8, '해산물', 'food_preference'),
(5, 9, '육류', 'food_preference'),
(5, 10, '면류', 'food_preference'),
(5, 11, '베이커리', 'food_preference'),
(5, 12, '디저트', 'food_preference'),
(5, 13, '커피/차', 'food_preference'),
(5, 14, '길거리 음식', 'food_preference'),
(5, 15, '매운 음식', 'food_preference'),
(5, 16, '짠 음식', 'food_preference'),
(5, 17, '단 음식', 'food_preference'),
(5, 18, '신 음식', 'food_preference'),
(5, 19, '튀김', 'food_preference'),
(5, 20, '볶음', 'food_preference'),
(5, 21, '찜', 'food_preference'),
(5, 22, '구이', 'food_preference'),
(5, 23, '국물 요리', 'food_preference'),
(5, 24, '면 요리', 'food_preference'),
(5, 25, '밥 요리', 'food_preference'),
(5, 26, '전골', 'food_preference'),
(5, 27, '찌개', 'food_preference'),
(5, 28, '파스타', 'food_preference'),
(5, 29, '피자', 'food_preference'),
(5, 30, '햄버거', 'food_preference'),
(5, 31, '스테이크', 'food_preference'),
(5, 32, '샐러드', 'food_preference'),
(5, 33, '돈까스', 'food_preference'),
(5, 34, '초밥', 'food_preference'),
(5, 35, '회', 'food_preference'),
(5, 36, '소고기', 'food_preference'),
(5, 37, '돼지고기', 'food_preference'),
(5, 38, '닭고기', 'food_preference'),
(5, 39, '향신료', 'food_preference'),
(5, 40, '음료', 'food_preference'),
(5, 41, '비건', 'food_preference'),
(5, 42, '락토-오보 베지테리언', 'food_preference'),
(5, 43, '페스코 베지테리언', 'food_preference'),
(5, 44, '글루텐프리', 'food_preference'),
(5, 45, '유기농', 'food_preference')
AS new_data
ON DUPLICATE KEY UPDATE 
    content = new_data.content,
    info_name = new_data.info_name;
