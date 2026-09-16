import os
from pathlib import Path
from slugify import slugify
from gtts import gTTS


class VoiceoverGenerator:
    def __init__(self):
        self.output_dir = Path('audio_files')
        self.output_dir.mkdir(exist_ok=True)

    def generate_audio(self, script: str, title: str = 'voiceover') -> str:
        clean_title = slugify(title) or 'voiceover'
        output_path = self.output_dir / f'{clean_title}.mp3'

        if output_path.exists():
            output_path.unlink()

        tts = gTTS(text=script, lang='en', slow=False)
        tts.save(str(output_path))
        return str(output_path)
