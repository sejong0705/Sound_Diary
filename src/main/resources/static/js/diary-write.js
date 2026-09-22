document.addEventListener('DOMContentLoaded', async () => {
  const weatherBtns = document.querySelectorAll('.sd-weather-btn');
  const weatherStatus = document.getElementById('weatherStatus');

  const latitudeField = document.getElementById('latitudeField');
  const longitudeField = document.getElementById('longitudeField');
  const weatherIconField = document.getElementById('weatherIconField');
  const weatherDescField = document.getElementById('weatherDescField');
  const weatherTempField = document.getElementById('weatherTempField');

  try {
    // 헤더에서 이미 받아온 값이 캐시에 있으면 GPS 재요청 없이 바로 재사용
    const weather = await parent.SDWeather.getWeather();

    latitudeField.value = weather.latitude;
    longitudeField.value = weather.longitude;
	locationNameField.value = weather.locationName ?? '';
    weatherIconField.value = weather.icon;
    weatherDescField.value = weather.description;
    weatherTempField.value = weather.temp;

    weatherStatus.textContent = `${weather.description} · ${Math.round(weather.temp)}°C`;
  } catch (e) {
    weatherStatus.textContent = '날씨 정보를 가져오지 못했어요';
  }

  weatherBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      weatherBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      weatherIconField.value = btn.dataset.icon;
      weatherDescField.value = btn.dataset.desc;
    });
  });
  // ===== 음악 검색 =====
  const searchInput = document.getElementById('trackSearchInput');
  const searchBtn = document.getElementById('trackSearchBtn');
  const resultsBox = document.getElementById('trackResults');

  const trackNameField = document.getElementById('trackNameField');
  const artistNameField = document.getElementById('artistNameField');
  const albumImageUrlField = document.getElementById('albumImageUrlField');
  const spotifyTrackUriField = document.getElementById('spotifyTrackUriField');

  const preview = document.getElementById('selectedTrackPreview');
  const previewImg = document.getElementById('selectedTrackImg');
  const previewName = document.getElementById('selectedTrackName');
  const previewArtist = document.getElementById('selectedTrackArtist');

  async function doSearch() {
    const q = searchInput.value.trim();
    if (!q) return;

    resultsBox.innerHTML = '<div class="sd-status">검색 중…</div>';

    try {
      const res = await fetch(`/api/spotify/search?q=${encodeURIComponent(q)}`);
      if (!res.ok) throw new Error('search failed');
      const tracks = await res.json();

      resultsBox.innerHTML = '';
      if (tracks.length === 0) {
        resultsBox.innerHTML = '<div class="sd-status">검색 결과가 없어요.</div>';
        return;
      }

      tracks.forEach(track => {
        const item = document.createElement('div');
        item.className = 'sd-track-result-item';
        item.innerHTML = `
          <img src="${track.albumImageUrl ?? ''}" alt="" class="sd-track-thumb">
          <div>
            <div class="sd-track-name">${track.name}</div>
            <div class="sd-track-artist">${track.artistName}</div>
          </div>
        `;
        item.addEventListener('click', () => selectTrack(track));
        resultsBox.appendChild(item);
      });
    } catch (e) {
      resultsBox.innerHTML = '<div class="sd-status error">검색에 실패했어요.</div>';
    }
  }

  function selectTrack(track) {
    trackNameField.value = track.name;
    artistNameField.value = track.artistName;
    albumImageUrlField.value = track.albumImageUrl ?? '';
    spotifyTrackUriField.value = track.uri;

    previewImg.src = track.albumImageUrl ?? '';
    previewName.textContent = track.name;
    previewArtist.textContent = track.artistName;
    preview.classList.remove('d-none');

    if (parent.SDPlayer) {
      parent.SDPlayer.playTrack(track.uri, track.name, track.artistName, track.albumImageUrl);
    }
  }

  searchBtn.addEventListener('click', doSearch);
  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') { e.preventDefault(); doSearch(); }
  });
});
const publicToggleUI = document.getElementById('publicToggleUI');
const isPublicField = document.getElementById('isPublicField');

publicToggleUI.addEventListener('change', () => {
  isPublicField.value = publicToggleUI.checked ? 'Y' : 'N';
});