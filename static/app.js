:root {
  --bg: #0f172a;
  --panel: #111827;
  --panel-2: #1f2937;
  --accent: #38bdf8;
  --accent-2: #a78bfa;
  --text: #e5e7eb;
  --muted: #94a3b8;
  --success: #22c55e;
}

* { box-sizing: border-box; }

body {
  margin: 0;
  background: linear-gradient(135deg, var(--bg), #111827 40%, #020617 100%);
  color: var(--text);
  font-family: Arial, sans-serif;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

.hero {
  text-align: center;
  margin-bottom: 24px;
}

.hero h1 {
  font-size: clamp(2rem, 5vw, 3rem);
  margin-bottom: 8px;
}

.hero p {
  color: var(--muted);
  font-size: 1.05rem;
}

.card {
  background: rgba(17, 24, 39, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.25);
  margin-bottom: 20px;
}

form {
  display: grid;
  gap: 12px;
}

label {
  font-weight: bold;
}

input, button {
  width: 100%;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  padding: 12px 14px;
  font-size: 1rem;
}

input {
  background: var(--panel-2);
  color: var(--text);
}

button {
  background: linear-gradient(90deg, var(--accent), var(--accent-2));
  color: #0b1120;
  font-weight: bold;
  border: none;
  cursor: pointer;
  transition: transform 0.2s ease;
}

button:hover {
  transform: translateY(-1px);
}

.progress-bar {
  width: 100%;
  height: 16px;
  background: rgba(148, 163, 184, 0.2);
  border-radius: 999px;
  overflow: hidden;
  margin-top: 8px;
}

#progressFill {
  width: 0%;
  height: 100%;
  background: linear-gradient(90deg, var(--success), var(--accent));
  border-radius: inherit;
  transition: width 0.2s ease;
}

.hidden {
  display: none;
}

#videoPlayer {
  width: 100%;
  border-radius: 12px;
  background: #000;
  min-height: 300px;
}

#resultMessage {
  color: var(--muted);
  margin-top: 12px;
}
