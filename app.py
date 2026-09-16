from flask import Flask, render_template, request, jsonify
from video_generator import VideoGenerator
import os
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)
video_gen = VideoGenerator()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/generate-video', methods=['POST'])
def generate_video():
    data = request.json
    topic = data.get('topic')
    duration = data.get('duration', 6000)  # 100 minutes in seconds
    
    try:
        video_path = video_gen.generate(topic, duration)
        return jsonify({'success': True, 'video_path': video_path})
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/progress', methods=['GET'])
def get_progress():
    progress = video_gen.get_progress()
    return jsonify(progress)

if __name__ == '__main__':
    app.run(debug=True, port=5000)
