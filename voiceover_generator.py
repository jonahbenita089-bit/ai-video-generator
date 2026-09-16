import pyttsx3
import os
from pathlib import Path

class VoiceoverGenerator:
    def __init__(self):
        self.engine = pyttsx3.init()
        self.engine.setProperty('rate', 150)  # Speed of speech
        self.engine.setProperty('volume', 0.9)
        self.output_dir = 'audio_files'
        Path(self.output_dir).mkdir(exist_ok=True)
    
    def generate_audio(self, script):
        """
        Convert script text to audio file.
        """
        output_path = os.path.join(self.output_dir, 'voiceover.mp3')
        
        try:
            self.engine.save_to_file(script, output_path)
            self.engine.runAndWait()
            return output_path
        except Exception as e:
            print(f"Error generating audio: {e}")
            raise e
    
    def set_voice(self, voice_id=0):
        """
        Set the voice for text-to-speech.
        voice_id: 0 for male, 1 for female
        """
        voices = self.engine.getProperty('voices')
        if voice_id < len(voices):
            self.engine.setProperty('voice', voices[voice_id].id)
    
    def set_speech_rate(self, rate=150):
        """
        Set the rate of speech (words per minute).
        """
        self.engine.setProperty('rate', rate)
