// Hero Slider
const slides = document.querySelectorAll('.slide');
let currentSlide = 0;
const slideInterval = 5000; // 5 seconds

function nextSlide() {
    slides[currentSlide].classList.remove('active');
    currentSlide = (currentSlide + 1) % slides.length;
    slides[currentSlide].classList.add('active');
}

// Start the slider
let slideTimer = setInterval(nextSlide, slideInterval);

// Pause on hover
const slider = document.querySelector('.hero-slider');
if (slider) {
    slider.addEventListener('mouseenter', () => {
        clearInterval(slideTimer);
    });

    slider.addEventListener('mouseleave', () => {
        slideTimer = setInterval(nextSlide, slideInterval);
    });
}

// Mobile menu toggle
const mobileMenuButton = document.querySelector('.navbar-toggler');
const mobileMenu = document.querySelector('.navbar-collapse');

if (mobileMenuButton && mobileMenu) {
    mobileMenuButton.addEventListener('click', () => {
        mobileMenu.classList.toggle('show');
    });
}

// Search functionality
const searchButton = document.querySelector('.search-button');
const searchBox = document.querySelector('.search-box');

if (searchButton && searchBox) {
    searchButton.addEventListener('click', (e) => {
        e.preventDefault();
        searchBox.classList.toggle('d-none');
        searchBox.querySelector('input').focus();
    });
}

// Close search when clicking outside
document.addEventListener('click', (e) => {
    if (searchBox && !searchBox.contains(e.target) && e.target !== searchButton) {
        searchBox.classList.add('d-none');
    }
});

// Initialize tooltips
const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl);
});

// Sample data for destinations (replace with actual data from backend)
const destinations = [
    {
        id: 1,
        title: '서울',
        image: 'https://source.unsplash.com/random/400x300/?seoul,korea',
        description: '대한민국의 수도이자 최대 도시인 서울의 매력을 느껴보세요.'
    },
    {
        id: 2,
        title: '부산',
        image: 'https://source.unsplash.com/random/400x300/?busan,korea',
        description: '바다와 어우러진 부산의 아름다운 경관을 만나보세요.'
    },
    {
        id: 3,
        title: '제주도',
        image: 'https://source.unsplash.com/random/400x300/?jeju,korea',
        description: '한국의 하와이, 제주도의 아름다운 자연을 경험하세요.'
    },
    {
        id: 4,
        title: '경주',
        image: 'https://source.unsplash.com/random/400x300/?gyeongju,korea',
        description: '천년의 역사가 살아숨쉬는 신라의 고도, 경주를 만나보세요.'
    }
];

// Sample data for stories (replace with actual data from backend)
const stories = [
    {
        id: 1,
        title: '서울에서의 특별한 주말 여행기',
        image: 'https://source.unsplash.com/random/600x400/?travel,korea',
        excerpt: '서울에서의 짧지만 알찬 주말 여행 코스를 소개합니다. 명동, 경복궁, 남산타워까지!',
        date: '2023.05.15',
        author: '여행작가 김서울'
    },
    {
        id: 2,
        title: '가족과 함께하는 부산 여행 추천',
        image: 'https://source.unsplash.com/random/600x400/?family,travel',
        excerpt: '아이들과 함께 즐거운 시간을 보낼 수 있는 부산의 가족 여행지들을 소개합니다.',
        date: '2023.06.22',
        author: '가족여행연구소'
    },
    {
        id: 3,
        title: '제주도에서의 로맨틱한 하루',
        image: 'https://source.unsplash.com/random/600x400/?couple,jeju',
        excerpt: '커플을 위한 제주도 데이트 코스와 특별한 추억을 만들 수 있는 장소들을 소개합니다.',
        date: '2023.07.10',
        author: '제주로미로'
    }
];

// Function to render destination cards
function renderDestinations() {
    const container = document.getElementById('destinations-container');
    if (!container) return;

    let html = '';
    destinations.forEach(destination => {
        html += `
            <div class="col-md-6 col-lg-3 mb-4">
                <div class="card h-100">
                    <img src="${destination.image}" class="card-img-top" alt="${destination.title}">
                    <div class="card-body">
                        <h5 class="card-title">${destination.title}</h5>
                        <p class="card-text">${destination.description}</p>
                        <a href="/destination/${destination.id}" class="btn btn-outline-primary btn-sm">자세히 보기</a>
                    </div>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

// Function to render story cards
function renderStories() {
    const container = document.getElementById('stories-container');
    if (!container) return;

    let html = '';
    stories.forEach(story => {
        html += `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card story-card h-100">
                    <img src="${story.image}" class="card-img-top" alt="${story.title}">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <small class="text-muted">${story.date}</small>
                            <small class="text-muted">${story.author}</small>
                        </div>
                        <h5 class="card-title">${story.title}</h5>
                        <p class="card-text">${story.excerpt}</p>
                        <a href="/story/${story.id}" class="btn btn-link p-0">더보기</a>
                    </div>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    renderDestinations();
    renderStories();
    
    // Activate first slide
    if (slides.length > 0) {
        slides[0].classList.add('active');
    }
});
