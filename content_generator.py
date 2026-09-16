import openai
from dotenv import load_dotenv
import os

load_dotenv()
openai.api_key = os.getenv('OPENAI_API_KEY')

class ContentGenerator:
    def __init__(self):
        self.model = "gpt-3.5-turbo"
    
    def generate_script(self, topic, duration):
        """
        Generate a comprehensive script for the video.
        Duration is in seconds, we estimate ~140 words per minute of speech.
        """
        minutes = duration / 60
        estimated_words = int(minutes * 140)
        
        prompt = f"""
        Create a comprehensive, engaging video script about '{topic}'.
        The script should be approximately {estimated_words} words long (for {minutes:.0f} minutes of speech).
        
        Structure the script with:
        1. An engaging introduction
        2. Key sections with detailed explanations
        3. Real-world examples
        4. Tips and takeaways
        5. A compelling conclusion
        
        Make it suitable for a professional video with narration.
        """
        
        response = openai.ChatCompletion.create(
            model=self.model,
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7
        )
        
        return response['choices'][0]['message']['content']
    
    def break_into_segments(self, script, segment_length=30):
        """
        Break script into segments for better visual composition.
        """
        sentences = script.split('.')
        segments = []
        current_segment = ""
        
        for sentence in sentences:
            if len(current_segment) + len(sentence) < segment_length * 10:
                current_segment += sentence + "."
            else:
                if current_segment:
                    segments.append(current_segment)
                current_segment = sentence + "."
        
        if current_segment:
            segments.append(current_segment)
        
        return segments
