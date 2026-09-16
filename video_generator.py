import openai
import pyttsx3
from moviepy.editor import *
import os
from dotenv import load_dotenv
from content_generator import ContentGenerator
from voiceover_generator import VoiceoverGenerator
from video_composer import VideoComposer

load_dotenv()

class VideoGenerator:
    def __init__(self):
        self.content_gen = ContentGenerator()
        self.voiceover_gen = VoiceoverGenerator()
        self.video_composer = VideoComposer()
        self.progress = {'status': 'idle', 'percent': 0}
    
    def generate(self, topic, duration=6000):
        """Generate a complete video of specified duration (in seconds)"""
        try:
            # Step 1: Generate content script
            self.progress = {'status': 'Generating script...', 'percent': 10}
            script = self.content_gen.generate_script(topic, duration)
            
            # Step 2: Generate voiceover
            self.progress = {'status': 'Generating voiceover...', 'percent': 40}
            audio_path = self.voiceover_gen.generate_audio(script)
            
            # Step 3: Generate visuals
            self.progress = {'status': 'Creating visuals...', 'percent': 70}
            visuals = self.video_composer.create_visuals(topic, script)
            
            # Step 4: Compose final video
            self.progress = {'status': 'Composing video...', 'percent': 85}
            video_path = self.video_composer.compose_video(
                audio_path, visuals, topic
            )
            
            self.progress = {'status': 'Complete', 'percent': 100}
            return video_path
            
        except Exception as e:
            self.progress = {'status': f'Error: {str(e)}', 'percent': 0}
            raise e
    
    def get_progress(self):
        return self.progress
