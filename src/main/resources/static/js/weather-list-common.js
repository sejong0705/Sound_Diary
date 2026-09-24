// diary-list.js, community-list.js 공통 유틸

function classifyWeather(icon) {
  if (!icon) return 'unknown';
  if (icon.startsWith('01')) return 'sunny';
  if (icon.startsWith('02') || icon.startsWith('03') || icon.startsWith('04') || icon.startsWith('50')) return 'cloudy';
  if (icon.startsWith('09') || icon.startsWith('10') || icon.startsWith('11')) return 'rainy';
  if (icon.startsWith('13')) return 'snowy';
  return 'cloudy';
}

function weatherIconImg(icon) {
  if (!icon) return '';
  return `<img src="https://openweathermap.org/img/wn/${icon}@2x.png" alt="" class="sd-weather-icon-img">`;
}

function formatDate(value) {
  if (!value) return '';
  const d = new Date(value);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}.${m}.${day}`;
}

function applyCurrentFilter(col) {
  const activeBtn = document.querySelector('.sd-filter-btn.active');
  const filter = activeBtn ? activeBtn.dataset.filter : 'all';
  if (filter !== 'all' && col.dataset.weather !== filter) {
    col.classList.add('d-none');
  }
}

function bindWeatherFilterButtons(filterBtns, filterEmptyMsg) {
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
}