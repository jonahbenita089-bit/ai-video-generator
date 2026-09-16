# AI Video Generator

An intelligent AI assistant that automatically generates high-quality videos up to 100 minutes long with automated content creation, voiceover generation, and video composition.

## Features

- **AI-Powered Script Generation**: Uses OpenAI GPT to create engaging, comprehensive scripts
- **Text-to-Speech Voiceover**: Converts scripts to professional audio narration
- **Automated Visuals**: Generates visual frames to accompany the narration
- **Video Composition**: Combines audio, visuals, and text into a complete video
- **Long-form Video Support**: Can generate videos up to 100+ minutes
- **Web Interface**: Flask-based UI for easy video generation
- **Progress Tracking**: Real-time progress updates during video generation

## Requirements

- Python 3.8+
- FFmpeg installed on your system
- OpenAI API key

## Installation

1. Clone this repository
```bash
git clone https://github.com/jonahbenita089-bit/ai-video-generator.git
cd ai-video-generator
```

2. Install dependencies
```bash
pip install -r requirements.txt
```

3. Set up environment variables
```bash
cp .env.example .env
# Edit .env and add your OpenAI API key
```

## Usage

### Via Web Interface

```bash
python app.py
```

Then open `http://localhost:5000` in your browser.

### Via Python

```python
from video_generator import VideoGenerator

vg = VideoGenerator()
video_path = vg.generate(
    topic="Introduction to Machine Learning",
    duration=6000  # 100 minutes in seconds
)
```

## Project Structure

- `app.py` - Flask application and routes
- `video_generator.py` - Main video generation orchestrator
- `content_generator.py` - AI script generation
- `voiceover_generator.py` - Text-to-speech audio generation
- `video_composer.py` - Video composition and frame generation
- `templates/` - HTML templates for the web interface
- `static/` - CSS and JavaScript files

## Configuration

### Video Duration
Default is 100 minutes (6000 seconds). You can adjust this:

```python
video_path = vg.generate(topic="Your Topic", duration=3600)  # 60 minutes
```

### Voice Settings
Adjust speech rate and voice in `voiceover_generator.py`:

```python
vg.voiceover_gen.set_speech_rate(150)  # Words per minute
vg.voiceover_gen.set_voice(0)  # 0=male, 1=female
```

## Output

Generated videos are saved in the `videos/` directory with the topic name as the filename.

## API Endpoints

- `POST /api/generate-video` - Generate a new video
  - Body: `{"topic": "string", "duration": int}`
  - Returns: `{"success": bool, "video_path": string}`

- `GET /api/progress` - Get current generation progress
  - Returns: `{"status": string, "percent": int}`

## License

MIT License

## Support

For issues and feature requests, please open a GitHub issue.
