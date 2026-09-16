import os
from pathlib import Path
from dotenv import load_dotenv

from content_generator import ContentGenerator
from voiceover_generator import VoiceoverGenerator
from video_composer import VideoComposer

load_dotenv()


class VideoGenerator:
    def __init__(self):
        self.content_engine = ContentGenerator()
        self.voiceover_engine = VoiceoverGenerator()
        self.composer = VideoComposer()
        self.progress = {"status": "idle", "percent": 0}

    def generate(self, topic: str, duration_seconds: int = 6000):
        """Generate a structured long-form video for the given topic."""
        self.progress = {"status": "Preparing generation", "percent": 5}

        if duration_seconds <= 0:
            raise ValueError("Duration must be greater than zero.")

        self.progress = {"status": "Generating script", "percent": 20}
        script = self.content_engine.generate_script(topic, duration_seconds)

        self.progress = {"status": "Generating narration", "percent": 55}
        audio_path = self.voiceover_engine.generate_audio(script, title=topic)

        self.progress = {"status": "Building visuals", "percent": 75}
        slide_paths = self.composer.create_visuals(topic, script)

        self.progress = {"status": "Composing final video", "percent": 90}
        output_path = self.composer.compose_video(audio_path=audio_path, slides=slide_paths, title=topic)

        self.progress = {"status": "Completed", "percent": 100}
        return {
            "video_path": output_path,
            "audio_path": audio_path,
            "script_preview": script[:500],
            "duration_seconds": duration_seconds,
        }

    def get_progress(self):
        return self.progress
