from flask import Flask, render_template, request, jsonify
from video_generator import VideoGenerator

app = Flask(__name__)
video_generator = VideoGenerator()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/generate-video', methods=['POST'])
def generate_video():
    payload = request.get_json(silent=True) or {}
    topic = (payload.get('topic') or '').strip()
    duration = int(payload.get('duration') or 6000)

    if not topic:
        return jsonify({"success": False, "error": "Please provide a topic."}), 400

    try:
        result = video_generator.generate(topic=topic, duration_seconds=duration)
        return jsonify({"success": True, **result})
    except Exception as exc:
        return jsonify({"success": False, "error": str(exc)}), 500

@app.route('/api/progress', methods=['GET'])
def api_progress():
    return jsonify(video_generator.get_progress())

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
