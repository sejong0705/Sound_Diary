document.addEventListener('DOMContentLoaded', () => {
  const countLabel = document.getElementById('diaryCount');
  const emptyMsg = document.getElementById('emptyMsg');
  const filterEmptyMsg = document.getElementById('filterEmptyMsg');
  const filterBtns = document.querySelectorAll('.sd-filter-btn');
  const diaryGrid = document.getElementById('diaryGrid');
  const sentinel = document.getElementById('scrollSentinel');
  const loadingIndicator = document.getElementById('loadingIndicator');
  const noMoreIndicator = document.getElementById('noMoreIndicator');

  const PAGE_SIZE = 10;
  let offset = 0;
  let totalCount = 0;
  let isLoading = false;
  let hasMore = true;

  function classifyWeather(icon) {
    if (!icon) return 'unknown';
    if (icon.startsWith('01')) return 'sunny';
    if (icon.startsWith('02') || icon.startsWith('03') || icon.startsWith('04') || icon.startsWith('50')) return 'cloudy';
    if (icon.startsWith('09') || icon.startsWith('10') || icon.startsWith('11')) return 'rainy';
    if (icon.startsWith('13')) return 'snowy';
    return 'cloudy';
  }

  function weatherBadgeText(wcat) {
    if (wcat === 'sunny') return '☀️ 맑음';
    if (wcat === 'rainy') return '🌧️ 비';
    if (wcat === 'snowy') return '❄️ 눈';
    if (wcat === 'cloudy') return '☁️ 흐림';
    return '날씨 없음';
  }

  function formatDate(value) {
    if (!value) return '';
    const d = new Date(value);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}.${m}.${day}`;
  }

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
          <div class="sd-author-row mb-2">
            <img src="${diary.authorProfileImgUrl ?? ''}" alt="" class="sd-author-avatar" onerror="this.style.display='none'">
            <span class="sd-author-name">${diary.authorNickname ?? '익명'}</span>
          </div>
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h5 class="sd-card-title">${diary.title ?? '제목 없음'}</h5>
            <span class="badge sd-badge">${weatherBadgeText(wcat)}</span>
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

  function applyCurrentFilter(col) {
    const activeBtn = document.querySelector('.sd-filter-btn.active');
    const filter = activeBtn ? activeBtn.dataset.filter : 'all';
    if (filter !== 'all' && col.dataset.weather !== filter) {
      col.classList.add('d-none');
    }
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
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) loadMore();
    });
  }, { rootMargin: '200px' });

  observer.observe(sentinel);

  filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      filterBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      const filter = btn.dataset.filter;
      const diaryCols = document.querySelectorAll('.sd-diary-col');
      let visibleCount = 0;

      diaryCols.forEach(col => {
        const matches = filter === 'all' || col.dataset.weather === filter;
        col.classList.toggle('d-none', !matches);
        if (matches) visibleCount++;
      });

      filterEmptyMsg.classList.toggle('d-none', visibleCount !== 0);
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

// ===== 좌측 랭킹 패널 =====
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
           data-uri="${item.SPOTIFYTRACKURI ?? ''}"
           data-name="${item.TRACKNAME ?? ''}"
           data-artist="${item.ARTISTNAME ?? ''}"
           data-image="${item.ALBUMIMAGEURL ?? ''}">
        <span class="sd-ranking-rank">${idx + 1}</span>
        <img src="${item.ALBUMIMAGEURL ?? ''}" alt="" class="sd-ranking-cover" onerror="this.style.visibility='hidden'">
        <div class="sd-ranking-info">
          <div class="sd-ranking-track">${item.TRACKNAME}</div>
          <div class="sd-ranking-artist">${item.ARTISTNAME}</div>
        </div>
        <span class="sd-ranking-count">${item.CNT}회</span>
      </div>
    `).join('');

    // 클릭 시 하단 재생바에서 재생
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