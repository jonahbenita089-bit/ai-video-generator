const button = document.getElementById('jokeButton');
const category = document.getElementById('category');
const joke = document.getElementById('joke');
const status = document.getElementById('status');
const copyButton = document.getElementById('copyButton');
const themeToggle = document.getElementById('themeToggle');
const favoriteList = document.getElementById('favoriteList');

const favorites = new Set(JSON.parse(localStorage.getItem('favorites') || '[]'));

function renderFavorites() {
  if (!favorites.size) {
    favoriteList.innerHTML = '<p class="favorites-empty">No favorites yet. Save some jokes you love.</p>';
    return;
  }

  favoriteList.innerHTML = Array.from(favorites)
    .map((item) => `<div class="favorite-item">${item}</div>`)
    .join('');
}

function saveFavorite(currentJoke) {
  if (!currentJoke || !currentJoke.trim()) return;
  favorites.add(currentJoke.trim());
  localStorage.setItem('favorites', JSON.stringify(Array.from(favorites)));
  renderFavorites();
}

async function getJoke() {
  button.disabled = true;
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
    button.disabled = false;
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

button.addEventListener('click', getJoke);
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
