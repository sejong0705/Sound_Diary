window.SDWeather = (function() {
    const STORAGE_KEY = 'sd_weather';
    const REFRESH_INTERVAL_MS = 10 * 60 * 1000; // 10분마다 갱신

	function getCached() {
	  const raw = sessionStorage.getItem(STORAGE_KEY);
	  if (!raw) return null;

	  const parsed = JSON.parse(raw);

	  if (!parsed.locationName) {
	    sessionStorage.removeItem(STORAGE_KEY);
	    return null;
	  }

	  // 이 부분이 있는지 확인
	  const age = Date.now() - (parsed.fetchedAt || 0);
	  if (age > REFRESH_INTERVAL_MS) {
	    return null;
	  }

	  return parsed;
	}
    function setCached(data) {
		data.fetchedAt = Date.now();
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    }

    async function getWeather() {
        const cached = getCached();
        if (cached) return cached;

        if (!navigator.geolocation) {
            throw new Error('geolocation unavailable');
        }

        const position = await new Promise((resolve, reject) => {
            navigator.geolocation.getCurrentPosition(resolve, reject);
        });

        const { latitude, longitude } = position.coords;

        const [weatherRes, locationRes] = await Promise.all([
            fetch(`/api/weather?lat=${latitude}&lon=${longitude}`),
            fetch(`/api/location?lat=${latitude}&lon=${longitude}`)
        ]);

        if (!weatherRes.ok) throw new Error('weather fetch failed');
        const weatherData = await weatherRes.json();
        const locationData = locationRes.ok ? await locationRes.json() : { locationName: null };

        const result = {
            latitude,
            longitude,
            icon: weatherData.icon,
            description: weatherData.description,
            temp: weatherData.temp,
            locationName: locationData.locationName
        };

        setCached(result);
        return result;
    }

    function clear() {
        sessionStorage.removeItem(STORAGE_KEY);
    }

    return { getWeather, clear };
})();