document.addEventListener('DOMContentLoaded', async () => {
    const locationBadge = document.getElementById('locationBadge');
    const locationText = document.getElementById('locationText');
    const weatherBadge = document.getElementById('weatherBadge');
    if (!weatherBadge) return;

    async function updateWeatherDisplay() {
        try {
            const weather = await SDWeather.getWeather();
            if (locationText) {
                locationText.textContent = weather.locationName ?? '위치 확인 중';
            }
            weatherBadge.innerHTML = `
        <img src="https://openweathermap.org/img/wn/${weather.icon}@2x.png" alt="" class="sd-weather-icon-img">
        ${weather.description} · ${Math.round(weather.temp)}°C
      `;
        } catch (e) {
            if (locationText) locationText.textContent = '위치 정보 없음';
            weatherBadge.textContent = '날씨 정보 없음';
        }
    }

    await updateWeatherDisplay(); // 최초 1회

    // 10분마다 자동으로 다시 확인 (캐시가 만료됐으면 자동으로 새로 조회됨)
    setInterval(updateWeatherDisplay, 10 * 60 * 1000);
});