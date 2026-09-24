document.addEventListener('DOMContentLoaded', () => {
    const countLabel = document.getElementById('diaryCount');
    const emptyMsg = document.getElementById('emptyMsg');
    const filterEmptyMsg = document.getElementById('filterEmptyMsg');
    const filterBtns = document.querySelectorAll('.sd-filter-btn');
    const diaryGrid = document.getElementById('diaryGrid');
    const sentinel = document.getElementById('scrollSentinel');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const noMoreIndicator = document.getElementById('noMoreIndicator');

    const PAGE_SIZE = 12;
    let offset = 0;
    let isLoading = false;
    let hasMore = true;

    // classifyWeather, weatherIconImg, formatDate, applyCurrentFilter는 공통 파일 사용

    function renderCard(diary) {
        const wcat = classifyWeather(diary.weatherIcon);
        const col = document.createElement('div');
        col.className = 'col-md-4 sd-diary-col';
        col.dataset.weather = wcat;
        col.dataset.diaryId = diary.diaryId;

        const trackBlock = diary.trackName
            ? `
        <div class="sd-track-mini">
          <img src="${diary.albumImageUrl ?? ''}" alt="" class="sd-track-thumb" onerror="this.style.display='none'">
          <div>
            <div class="sd-track-name">${diary.trackName}</div>
            <div class="sd-track-artist">${diary.artistName ?? ''}</div>
          </div>
        </div>`
            : '';

        const profileImg = diary.authorProfileImgUrl && diary.authorProfileImgUrl.trim() !== ''
            ? diary.authorProfileImgUrl
            : '/images/default-profile.png';

        col.innerHTML = `
      <div class="card sd-diary-card h-100">
        <div class="card-body d-flex flex-column">
          <div class="sd-author-row mb-2">
            <img src="${profileImg}" alt="" class="sd-author-avatar"
                 onerror="this.src='/images/default-profile.png'">
            <span class="sd-author-name">${diary.authorNickname ?? '익명'}</span>
          </div>
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h5 class="sd-card-title">${diary.title ?? '제목 없음'}</h5>
            <span class="badge sd-badge sd-weather-badge-img">${weatherIconImg(diary.weatherIcon)}</span>
          </div>
          <div class="sd-card-date">${formatDate(diary.createdAt)}</div>
          ${trackBlock}
          <p class="sd-card-content flex-grow-1">${(diary.content ?? '').slice(0, 80)}${(diary.content ?? '').length > 80 ? '…' : ''}</p>
          <div class="d-flex gap-2 mt-2">
            <a href="/community/detail/${diary.diaryId}" class="btn btn-sm sd-btn-outline">상세보기</a>
          </div>
        </div>
      </div>
    `;
        return col;
    }

    async function loadMore() {
        if (isLoading || !hasMore) return;
        isLoading = true;
        loadingIndicator.classList.remove('d-none');

        try {
            const res = await fetch(`/api/community?offset=${offset}&limit=${PAGE_SIZE}`);
            if (!res.ok) throw new Error('load failed');
            const list = await res.json();

            if (offset === 0 && list.length === 0) {
                emptyMsg.classList.remove('d-none');
                loadingIndicator.classList.add('d-none');
                hasMore = false;
                return;
            }

            list.forEach(diary => {
                const col = renderCard(diary);
                applyCurrentFilter(col);
                diaryGrid.appendChild(col);
            });

            offset += list.length;

            if (list.length < PAGE_SIZE) {
                hasMore = false;
                noMoreIndicator.classList.remove('d-none');
                observer.unobserve(sentinel);
            }
        } catch (e) {
            console.warn('로딩 실패:', e);
            loadingIndicator.textContent = '불러오지 못했어요.';
        } finally {
            isLoading = false;
            loadingIndicator.classList.add('d-none');
        }
        updateFilterEmptyMsg(filterEmptyMsg, hasMore);
        if (hasMore && isSentinelVisible()) loadMore();
    }
    function isSentinelVisible() {
        const rect = sentinel.getBoundingClientRect();
        return rect.top < window.innerHeight + 200;   // observer의 rootMargin과 동일
    }
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) loadMore();
        });
    }, { rootMargin: '200px' });

    observer.observe(sentinel);

    bindWeatherFilterButtons(filterBtns, filterEmptyMsg);
    // 필터 적용 후 보이는 카드가 부족하면 이어서 로드
    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            updateFilterEmptyMsg(filterEmptyMsg, hasMore);
            if (hasMore && isSentinelVisible()) loadMore();
        });
    });
    async function loadTotalCount() {
        try {
            const res = await fetch('/api/community/count');
            if (!res.ok) throw new Error('count fetch failed');
            const total = await res.json();
            countLabel.textContent = `총 ${total}개`;
        } catch (e) {
            countLabel.textContent = '';
        }
    }

    loadTotalCount();
    loadMore();
});

// ===== 좌측 랭킹 패널 (community-list 전용) =====
document.addEventListener('DOMContentLoaded', () => {
    const rankingList = document.getElementById('rankingList');

    async function loadRanking() {
        rankingList.innerHTML = '<p class="sd-status">불러오는 중...</p>';

        try {
            const res = await fetch('/api/community/ranking');
            if (!res.ok) throw new Error('ranking fetch failed');
            const list = await res.json();

            if (list.length === 0) {
                rankingList.innerHTML = '<p class="sd-status">아직 데이터가 충분하지 않아요.</p>';
                return;
            }

            rankingList.innerHTML = list.map((item, idx) => `
      <div class="sd-ranking-item sd-ranking-clickable"
           data-uri="${item.spotifyTrackUri ?? ''}"
           data-name="${item.trackName ?? ''}"
           data-artist="${item.artistName ?? ''}"
           data-image="${item.albumImageUrl ?? ''}">
        <span class="sd-ranking-rank">${idx + 1}</span>
        <img src="${item.albumImageUrl ?? ''}" alt="" class="sd-ranking-cover" onerror="this.style.visibility='hidden'">
        <div class="sd-ranking-info">
          <div class="sd-ranking-track">${item.trackName}</div>
          <div class="sd-ranking-artist">${item.artistName}</div>
        </div>
        <span class="sd-ranking-count">${item.cnt}회</span>
      </div>
    `).join('');

            document.querySelectorAll('.sd-ranking-clickable').forEach(el => {
                el.addEventListener('click', () => {
                    const uri = el.dataset.uri;
                    if (!uri) {
                        alert('이 곡은 재생 정보가 없어요.');
                        return;
                    }
                    if (parent.SDPlayer) {
                        parent.SDPlayer.playTrack(uri, el.dataset.name, el.dataset.artist, el.dataset.image, null);
                    }
                });
            });
        } catch (e) {
            rankingList.innerHTML = '<p class="sd-status">불러오지 못했어요.</p>';
        }
    }

    loadRanking();
});