const button = document.getElementById('jokeButton');
const category = document.getElementById('category');
const joke = document.getElementById('joke');
const status = document.getElementById('status');

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

button.addEventListener('click', getJoke);
getJoke();
