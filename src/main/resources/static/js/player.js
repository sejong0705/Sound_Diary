window.SDPlayer = (function() {
    let player = null;
    let deviceId = null;
    let accessToken = null;
    let isPaused = true;
    let currentTrackUri = null;
    let currentArtistName = null;
    let autoplayEnabled = true;

    let progressTimer = null;      // 진행바를 부드럽게 움직이기 위한 로컬 타이머
    let currentPositionMs = 0;
    let currentDurationMs = 0;
    let isSeeking = false;         // 사용자가 진행바를 드래그 중인지
    let currentArtistId = null;
	let isFetchingRecommendation = false;   // 추가: 중복 호출 방지 플래그
	
	
    const STATE_KEY = 'sd_player_state';

    const cover = document.getElementById('playerCover');
    const trackNameEl = document.getElementById('playerTrackName');
    const artistNameEl = document.getElementById('playerArtistName');
    const toggleBtn = document.getElementById('playerToggleBtn');
    const nextBtn = document.getElementById('playerNextBtn');
    const progressBar = document.getElementById('playerProgressBar');
    const currentTimeEl = document.getElementById('playerCurrentTime');
    const durationEl = document.getElementById('playerDuration');
    const toggleIcon = document.getElementById('playerToggleIcon');
	

	async function playNextRecommendation(endedTrackUri, artistId) {
	  if (!artistId) return;
	  if (isFetchingRecommendation) return;   // 이미 진행 중이면 무시

	  isFetchingRecommendation = true;

	  try {
	    const res = await fetch(`/api/spotify/recommendation?artistId=${artistId}&excludeUri=${encodeURIComponent(endedTrackUri)}`);
	    if (!res.ok) throw new Error('recommendation fetch failed');

	    const text = await res.text();
	    if (!text) {
	      console.warn('추천곡 없음');
	      return;
	    }

	    const nextTrack = JSON.parse(text);
	    if (!nextTrack || !nextTrack.uri) return;

	    renderBar(nextTrack.name, nextTrack.artistName, nextTrack.albumImageUrl);
	    currentTrackUri = nextTrack.uri;
	    currentArtistId = nextTrack.artistId;
	    currentArtistName = nextTrack.artistName;
	    await requestPlay(nextTrack.uri);

	    saveState({
	      uri: nextTrack.uri,
	      artistId: nextTrack.artistId,
	      trackName: nextTrack.name,
	      artistName: nextTrack.artistName,
	      albumImageUrl: nextTrack.albumImageUrl,
	      positionMs: 0,
	      isPlaying: true
	    });
	  } catch (e) {
	    console.warn('다음 곡 추천 실패:', e);
	  } finally {
	    isFetchingRecommendation = false;   // 끝나면 다시 호출 가능하게 해제
	  }
	}
    async function fetchToken() {
        const res = await fetch('/api/spotify/token');
        const data = await res.json();
        return data.accessToken;
    }

    function saveState(state) {
        sessionStorage.setItem(STATE_KEY, JSON.stringify(state));
    }

    function getState() {
        const raw = sessionStorage.getItem(STATE_KEY);
        return raw ? JSON.parse(raw) : null;
    }

    function renderBar(name, artist, imageUrl) {
        trackNameEl.textContent = name;
        artistNameEl.textContent = artist;
        if (imageUrl) {
            cover.src = imageUrl;
            cover.classList.remove('d-none');
        }
        toggleBtn.disabled = false;
        nextBtn.disabled = false;
    }

    // ms를 "1:23" 형태 문자열로 변환
    function formatTime(ms) {
        const totalSec = Math.floor(ms / 1000);
        const min = Math.floor(totalSec / 60);
        const sec = totalSec % 60;
        return `${min}:${String(sec).padStart(2, '0')}`;
    }

    // 진행바와 시간 표시를 갱신
    function updateProgressUI() {
        if (isSeeking || currentDurationMs === 0) return;
        const ratio = currentPositionMs / currentDurationMs;
        progressBar.value = Math.min(1000, Math.floor(ratio * 1000));
        currentTimeEl.textContent = formatTime(currentPositionMs);
        durationEl.textContent = formatTime(currentDurationMs);
    }

    // player_state_changed는 자주 발생하지 않아서, 그 사이는 로컬 타이머로 1초씩 흉내내서 부드럽게 움직임
    function startProgressTimer() {
        stopProgressTimer();
        progressTimer = setInterval(() => {
            if (!isPaused) {
                currentPositionMs += 1000;
                if (currentPositionMs > currentDurationMs) currentPositionMs = currentDurationMs;
                updateProgressUI();
            }
        }, 1000);
    }

    function stopProgressTimer() {
        if (progressTimer) {
            clearInterval(progressTimer);
            progressTimer = null;
        }
    }

    window.onSpotifyWebPlaybackSDKReady = async () => {
        accessToken = await fetchToken();
        if (!accessToken) return;

        player = new Spotify.Player({
            name: 'Sound Diary Web Player',
            getOAuthToken: async (cb) => {
                const freshToken = await fetchToken();
                accessToken = freshToken;
                cb(freshToken);
            },
            volume: 0.5
        });

        player.addListener('ready', async ({ device_id }) => {
            deviceId = device_id;

            const saved = getState();
            if (saved && saved.uri) {
                renderBar(saved.trackName, saved.artistName, saved.albumImageUrl);
                currentTrackUri = saved.uri;
                currentArtistName = saved.artistName;
                currentArtistId = saved.artistId;

                if (saved.isPlaying) {
                    await requestPlay(saved.uri);
                    if (saved.positionMs > 0) {
                        setTimeout(() => seekTo(saved.positionMs), 500);
                    }
                }
            }
        });

        player.addListener('player_state_changed', (state) => {
            if (!state) return;
            isPaused = state.paused;
            toggleIcon.src = isPaused ? '/images/play-circle.svg' : '/images/pause-circle.svg';

            // 서버(SDK)가 알려주는 실제 위치/길이로 로컬 값 보정
            currentPositionMs = state.position;
            currentDurationMs = state.duration;
            updateProgressUI();

            if (isPaused) {
                stopProgressTimer();
            } else {
                startProgressTimer();
            }

            const track = state.track_window.current_track;
            currentTrackUri = track.uri;
            currentArtistName = track.artists[0].name;
            currentArtistId = track.artists[0].uri.split(':').pop();//아티스트 id 가져옴

            saveState({
                uri: track.uri,
                artistId: currentArtistId,
                trackName: track.name,
                artistName: track.artists[0].name,
                albumImageUrl: track.album.images[0]?.url,
                positionMs: state.position,
                isPlaying: !state.paused
            });

            const isTrackEnded =
                state.paused &&
                state.track_window.previous_tracks.length > 0 &&
                state.track_window.previous_tracks[0].uri === track.uri;

            if (isTrackEnded && autoplayEnabled) {
                playNextRecommendation(track.uri, currentArtistId);
            }
        });

        player.connect();
    };

    async function requestPlay(uri) {
        await fetch(`https://api.spotify.com/v1/me/player/play?device_id=${deviceId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ uris: [uri] })
        });
    }

    async function seekTo(positionMs) {
        await fetch(`https://api.spotify.com/v1/me/player/seek?position_ms=${positionMs}&device_id=${deviceId}`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${accessToken}` }
        });
    }

    toggleBtn.addEventListener('click', () => {
        if (player) player.togglePlay();
    });

    // "다음 곡" 버튼: 지금 곡을 강제로 끝난 것처럼 취급해서 바로 추천곡으로 넘어감
    nextBtn.addEventListener('click', () => {
        if (currentTrackUri && currentArtistId) {
            playNextRecommendation(currentTrackUri, currentArtistId);
        }
    });

    // 진행바를 사용자가 드래그할 때: 드래그 중엔 자동 갱신 멈춤
    progressBar.addEventListener('input', () => {
        isSeeking = true;
        const ratio = progressBar.value / 1000;
        currentTimeEl.textContent = formatTime(ratio * currentDurationMs);
    });

    // 드래그 끝나면 실제 재생 위치를 그 지점으로 이동시킴
    progressBar.addEventListener('change', async () => {
        const ratio = progressBar.value / 1000;
        const targetMs = Math.floor(ratio * currentDurationMs);
        await seekTo(targetMs);
        currentPositionMs = targetMs;
        isSeeking = false;
    });

    async function playTrack(uri, name, artist, imageUrl, artistId) {
        if (!deviceId) {
            alert('재생 준비 중이에요. 잠시 후 다시 시도해주세요.');
            return;
        }

        renderBar(name, artist, imageUrl);
        currentTrackUri = uri;
        currentArtistName = artist;
        currentArtistId = artistId;
        await requestPlay(uri);

        saveState({ uri, artistId, trackName: name, artistName: artist, albumImageUrl: imageUrl, positionMs: 0, isPlaying: true });
    }

    return { playTrack };
})();