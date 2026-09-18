const button = document.getElementById('jokeButton');
const category = document.getElementById('category');
const joke = document.getElementById('joke');
const status = document.getElementById('status');
const copyButton = document.getElementById('copyButton');
const shareButton = document.getElementById('shareButton');
const speakButton = document.getElementById('speakButton');
const favoriteButton = document.getElementById('favoriteButton');
const favoriteRandomButton = document.getElementById('favoriteRandomButton');
const themeToggle = document.getElementById('themeToggle');
const favoriteList = document.getElementById('favoriteList');
const loader = document.getElementById('loader');

const favorites = new Set(JSON.parse(localStorage.getItem('favorites') || '[]'));

function setLoading(isLoading) {
  loader.classList.toggle('hidden', !isLoading);
  button.disabled = isLoading;
}

function renderFavorites() {
  if (!favorites.size) {
    favoriteList.innerHTML = '<p class="favorites-empty">No favorites yet. Double click a joke or save one.</p>';
    return;
  }

  favoriteList.innerHTML = Array.from(favorites)
    .map((item) => `<div class="favorite-item">${item}</div>`)
    .join('');
}

function saveFavorite(currentJoke) {
  if (!currentJoke || !currentJoke.trim()) return;
  const value = currentJoke.trim();
  favorites.add(value);
  localStorage.setItem('favorites', JSON.stringify(Array.from(favorites)));
  renderFavorites();
}

async function getJoke() {
  setLoading(true);
  status.textContent = 'Finding a joke...';
  joke.textContent = '';

  try {
    const response = await fetch(`/api/joke?category=${encodeURIComponent(category.value)}`);
    const data = await response.json();
    if (!response.ok || !data.success) {
      throw new Error(data.error || 'Unable to get a joke.');
    }
    joke.textContent = data.joke;
    status.textContent = `${data.category} joke`;
  } catch (error) {
    joke.textContent = 'Sorry, no joke this time. Please try again.';
    status.textContent = error.message;
  } finally {
    setLoading(false);
  }
}

copyButton.addEventListener('click', async () => {
  if (!joke.textContent || joke.textContent.includes('Your joke will appear here')) {
    status.textContent = 'No joke to copy yet.';
    return;
  }

  try {
    await navigator.clipboard.writeText(joke.textContent);
    status.textContent = 'Joke copied!';
  } catch {
    status.textContent = 'Copy failed. Please try again.';
  }
});

shareButton.addEventListener('click', async () => {
  if (!joke.textContent || joke.textContent.includes('Your joke will appear here')) {
    status.textContent = 'No joke to share yet.';
    return;
  }

  const shareData = {
    title: 'Random Joke Generator',
    text: joke.textContent,
    url: window.location.href
  };

  try {
    if (navigator.share) {
      await navigator.share(shareData);
      status.textContent = 'Joke shared.';
    } else {
      await navigator.clipboard.writeText(joke.textContent);
      status.textContent = 'Share not supported; joke copied instead.';
    }
  } catch {
    status.textContent = 'Share canceled.';
  }
});

speakButton.addEventListener('click', () => {
  if (!joke.textContent || joke.textContent.includes('Your joke will appear here')) {
    status.textContent = 'No joke to read aloud yet.';
    return;
  }

  const speech = new SpeechSynthesisUtterance(joke.textContent);
  speech.lang = 'en-US';
  window.speechSynthesis.cancel();
  window.speechSynthesis.speak(speech);
  status.textContent = 'Speaking joke...';
});

favoriteButton.addEventListener('click', () => {
  saveFavorite(joke.textContent);
  status.textContent = 'Joke saved to favorites!';
});

favoriteRandomButton.addEventListener('click', () => {
  if (!favorites.size) {
    status.textContent = 'No favorites yet.';
    return;
  }

  const items = Array.from(favorites);
  const random = items[Math.floor(Math.random() * items.length)];
  joke.textContent = random;
  status.textContent = 'Picked a favorite joke.';
});

themeToggle.addEventListener('click', () => {
  document.body.classList.toggle('light');
  const isLight = document.body.classList.contains('light');
  themeToggle.textContent = isLight ? '☀️' : '🌙';
  localStorage.setItem('theme', isLight ? 'light' : 'dark');
});

const savedTheme = localStorage.getItem('theme');
if (savedTheme === 'light') {
  document.body.classList.add('light');
  themeToggle.textContent = '☀️';
}

joke.addEventListener('dblclick', () => {
  saveFavorite(joke.textContent);
  status.textContent = 'Joke saved to favorites!';
});

renderFavorites();
getJoke();
