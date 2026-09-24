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

    // ===== 삭제 (diary-list 전용) =====
    diaryGrid.addEventListener('click', async (e) => {
        if (!e.target.classList.contains('sd-delete-btn')) return;
        if (!confirm('이 일기를 삭제할까요?')) return;

        const id = e.target.dataset.id;
        try {
            const res = await fetch(`/api/diaries/${id}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('delete failed');

            e.target.closest('.sd-diary-col').remove();
            const current = parseInt(countLabel.textContent.replace(/[^0-9]/g, ''), 10) || 0;
            countLabel.textContent = `총 ${current - 1}개`;
        } catch (err) {
            alert('삭제에 실패했어요.');
        }
    });

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

        col.innerHTML = `
      <div class="card sd-diary-card h-100">
        <div class="card-body d-flex flex-column">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h5 class="sd-card-title">${diary.title ?? '제목 없음'}</h5>
            <span class="badge sd-badge sd-weather-badge-img">${weatherIconImg(diary.weatherIcon)}</span>
          </div>
          <div class="sd-card-date">${formatDate(diary.createdAt)}</div>
          ${trackBlock}
          <p class="sd-card-content flex-grow-1">${(diary.content ?? '').slice(0, 80)}${(diary.content ?? '').length > 80 ? '…' : ''}</p>
          <div class="d-flex gap-2 mt-2">
            <a href="/diary/detail/${diary.diaryId}" class="btn btn-sm sd-btn-outline">상세보기</a>
            <button class="btn btn-sm sd-btn-danger sd-delete-btn" data-id="${diary.diaryId}">삭제</button>
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
            const res = await fetch(`/api/diaries?offset=${offset}&limit=${PAGE_SIZE}`);
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
        // 추가: 문구 재계산 + sentinel이 아직 보이면 계속 로드
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
            const res = await fetch('/api/diaries/count');
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

document.addEventListener('DOMContentLoaded', () => {
    // ===== 좌측 음악 검색 (diary-list 전용) =====
    const trackSearchInput = document.getElementById('trackSearchInput');
    const trackSearchBtn = document.getElementById('trackSearchBtn');
    const trackResultsBox = document.getElementById('trackResults');

    async function doTrackSearch() {
        const q = trackSearchInput.value.trim();
        if (!q) return;

        trackResultsBox.innerHTML = '<div class="sd-status">검색 중…</div>';

        try {
            const res = await fetch(`/api/spotify/search?q=${encodeURIComponent(q)}`);
            if (!res.ok) throw new Error('search failed');
            const tracks = await res.json();

            trackResultsBox.innerHTML = '';
            if (tracks.length === 0) {
                trackResultsBox.innerHTML = '<div class="sd-status">검색 결과가 없어요.</div>';
                return;
            }

            tracks.forEach(track => {
                const item = document.createElement('div');
                item.className = 'sd-track-result-item';
                item.innerHTML = `
        <img src="${track.albumImageUrl ?? ''}" alt="" class="sd-track-thumb"
             style="width:36px;height:36px;border-radius:4px;object-fit:cover;flex-shrink:0;">
        <div>
          <div class="sd-track-name">${track.name}</div>
          <div class="sd-track-artist">${track.artistName}</div>
        </div>
      `;
                item.addEventListener('click', () => {
                    if (parent.SDPlayer) {
                        parent.SDPlayer.playTrack(track.uri, track.name, track.artistName, track.albumImageUrl, track.artistId);
                    }
                });
                trackResultsBox.appendChild(item);
            });
        } catch (e) {
            trackResultsBox.innerHTML = '<div class="sd-status error">검색에 실패했어요.</div>';
        }
    }

    if (trackSearchBtn) {
        trackSearchBtn.addEventListener('click', doTrackSearch);
        trackSearchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') { e.preventDefault(); doTrackSearch(); }

        });
    }
});