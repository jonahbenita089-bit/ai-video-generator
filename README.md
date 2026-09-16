const form = document.getElementById('videoForm');
const statusText = document.getElementById('statusText');
const progressFill = document.getElementById('progressFill');
const resultBox = document.getElementById('resultBox');
const videoPlayer = document.getElementById('videoPlayer');
const resultMessage = document.getElementById('resultMessage');

let progressTimer = null;

form.addEventListener('submit', async function (event) {
  event.preventDefault();

  const topic = document.getElementById('topic').value.trim();
  const duration = Number(document.getElementById('duration').value || 6000);

  if (!topic) {
    statusText.textContent = 'Please provide a topic.';
    return;
  }

  statusText.textContent = 'Generating...';
  progressFill.style.width = '5%';
  resultBox.classList.add('hidden');

  try {
    const response = await fetch('/api/generate-video', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ topic, duration })
    });

    const data = await response.json();
    if (!response.ok || !data.success) {
      throw new Error(data.error || 'Unknown error');
    }

    const videoPath = data.video_path;
    videoPlayer.src = '/' + videoPath.replace(/\\/g, '/');
    resultMessage.textContent = `Generated successfully: ${videoPath}`;
    resultBox.classList.remove('hidden');

    pollProgress();
  } catch (error) {
    statusText.textContent = `Error: ${error.message}`;
    progressFill.style.width = '0%';
  }
});

function pollProgress() {
  if (progressTimer) {
    clearInterval(progressTimer);
  }

  progressTimer = setInterval(async () => {
    try {
      const response = await fetch('/api/progress');
      const data = await response.json();
      statusText.textContent = data.status || 'Processing';
      progressFill.style.width = `${Math.max(0, Math.min(100, data.percent || 0))}%`;

      if ((data.percent || 0) >= 100) {
        clearInterval(progressTimer);
      }
    } catch (_) {
      // ignore transient errors
    }
  }, 800);
}
