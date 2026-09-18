from flask import Flask, render_template, request, jsonify
from video_generator import VideoGenerator
import requests

app = Flask(__name__)
video_generator = VideoGenerator()
JOKE_API_URL = "https://v2.jokeapi.dev/joke/Any"

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/joke', methods=['GET'])
def random_joke():
    category = request.args.get('category', 'Any')
    allowed_categories = {'Any', 'Programming', 'Misc', 'Dark', 'Pun', 'Spooky', 'Christmas'}
    if category not in allowed_categories:
        return jsonify({'success': False, 'error': 'Unsupported joke category.'}), 400

    try:
        response = requests.get(
            JOKE_API_URL,
            params={'format': 'json', 'type': 'single', 'category': category, 'safe-mode': ''},
            timeout=8,
        )
        response.raise_for_status()
        data = response.json()
        if data.get('error'):
            return jsonify({'success': False, 'error': 'The joke service returned an error.'}), 502
        joke = data.get('joke')
        if not joke:
            return jsonify({'success': False, 'error': 'No joke was returned.'}), 502
        return jsonify({'success': True, 'joke': joke, 'category': data.get('category', category)})
    except requests.RequestException:
        return jsonify({'success': False, 'error': 'The joke service is temporarily unavailable.'}), 502

@app.route('/api/generate-video', methods=['POST'])
def generate_video():
    payload = request.get_json(silent=True) or {}
    topic = (payload.get('topic') or '').strip()
    duration = int(payload.get('duration') or 6000)

    if not topic:
        return jsonify({'success': False, 'error': 'Please provide a topic.'}), 400

    try:
        result = video_generator.generate(topic=topic, duration_seconds=duration)
        return jsonify({'success': True, **result})
    except Exception as exc:
        return jsonify({'success': False, 'error': str(exc)}), 500

@app.route('/api/progress', methods=['GET'])
def api_progress():
    return jsonify(video_generator.get_progress())

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
