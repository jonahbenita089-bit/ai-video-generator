from moviepy.editor import *
import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import random

class VideoComposer:
    def __init__(self):
        self.output_dir = 'videos'
        self.temp_dir = 'temp_frames'
        Path(self.output_dir).mkdir(exist_ok=True)
        Path(self.temp_dir).mkdir(exist_ok=True)
    
    def create_visuals(self, topic, script):
        """
        Create visual frames for the video.
        """
        frames = []
        
        # Create title frame
        title_frame = self._create_text_frame(topic, "Title")
        frames.append(title_frame)
        
        # Create content frames
        sentences = script.split('.')
        for i, sentence in enumerate(sentences[:50]):  # Limit to avoid excessive processing
            if sentence.strip():
                frame = self._create_text_frame(sentence.strip(), f"Slide {i+1}")
                frames.append(frame)
        
        return frames
    
    def _create_text_frame(self, text, title):
        """
        Create a single text frame.
        """
        # Create a simple image with text
        width, height = 1920, 1080
        image = Image.new('RGB', (width, height), color=self._random_color())
        
        # Add semi-transparent overlay
        overlay = Image.new('RGBA', (width, height), (0, 0, 0, 128))
        image.paste(overlay, (0, 0), overlay)
        
        draw = ImageDraw.Draw(image)
        
        # Add text
        text_color = (255, 255, 255)
        
        # Title
        draw.text((100, 100), title, fill=text_color)
        
        # Content (wrap text)
        self._draw_wrapped_text(draw, text, (100, 300), text_color)
        
        frame_path = os.path.join(self.temp_dir, f'frame_{random.randint(0, 1000000)}.png')
        image.save(frame_path)
        
        return frame_path
    
    def _draw_wrapped_text(self, draw, text, position, color, max_width=1800, line_height=40):
        """
        Draw text with wrapping.
        """
        words = text.split()
        lines = []
        current_line = []
        
        for word in words:
            current_line.append(word)
            line = ' '.join(current_line)
            if len(line) > 80:  # Simple word wrapping
                lines.append(' '.join(current_line[:-1]))
                current_line = [word]
        
        if current_line:
            lines.append(' '.join(current_line))
        
        y = position[1]
        for line in lines:
            draw.text((position[0], y), line, fill=color)
            y += line_height
    
    def _random_color(self):
        """
        Generate a random color.
        """
        return (random.randint(20, 100), random.randint(20, 100), random.randint(20, 100))
    
    def compose_video(self, audio_path, frames, title):
        """
        Compose the final video from audio and visual frames.
        """
        try:
            # Load audio
            audio = AudioFileClip(audio_path)
            duration = audio.duration
            
            # Create video from frames
            # Each frame duration is calculated to fit the audio duration
            frame_duration = duration / len(frames) if frames else 1
            
            clips = []
            for frame_path in frames:
                img_clip = ImageClip(frame_path).set_duration(frame_duration)
                clips.append(img_clip)
            
            # Concatenate clips
            video = concatenate_videoclips(clips)
            
            # Add audio
            final_video = video.set_audio(audio)
            
            # Write video file
            output_path = os.path.join(self.output_dir, f'{title.replace(" ", "_")}_video.mp4')
            final_video.write_videofile(output_path, fps=24, verbose=False, logger=None)
            
            return output_path
            
        except Exception as e:
            print(f"Error composing video: {e}")
            raise e
