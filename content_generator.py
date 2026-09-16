import os
import re
from pathlib import Path

try:
    from openai import OpenAI
except Exception:  # pragma: no cover
    OpenAI = None


class ContentGenerator:
    def __init__(self):
        self.api_key = os.getenv("OPENAI_API_KEY")
        self.client = OpenAI(api_key=self.api_key) if self.api_key and OpenAI else None

    def generate_script(self, topic: str, duration_seconds: int) -> str:
        minutes = max(1, round(duration_seconds / 60))
        if not self.client:
            return self._fallback_script(topic, minutes)

        prompt = (
            f"Create a clear, engaging, professional long-form video script on '{topic}'. "
            f"Target length: about {minutes} minutes of spoken narration. "
            "Use a natural spoken tone, include an introduction, 4-6 sections, examples, and a strong conclusion. "
            "Structure it in readable paragraphs and keep it suitable for narration."
        )

        try:
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": "You write polished video narration scripts."},
                    {"role": "user", "content": prompt},
                ],
                temperature=0.8,
                max_tokens=1800,
            )
            script = response.choices[0].message.content.strip()
            return self._clean_script(script)
        except Exception:
            return self._fallback_script(topic, minutes)

    def _clean_script(self, script: str) -> str:
        script = script.replace("\r", "\n")
        script = re.sub(r"\n{3,}", "\n\n", script)
        return script.strip()

    def _fallback_script(self, topic: str, minutes: int) -> str:
        intro = (
            f"Welcome to this long-form guide on {topic}. In this session, we will walk through "
            "the key ideas, practical strategies, and real-world examples that make this topic important."
        )

        sections = [
            (
                "First, it is important to establish the foundation. Every strong concept begins with "
                "clarity, context, and a practical understanding of why the subject matters in the real world."
            ),
            (
                "Next, we look at the main principles behind the topic. These ideas help you understand "
                "how things work, where common mistakes happen, and how to apply better decision-making."
            ),
            (
                "Then we explore examples and use cases. When you connect theory to practical scenarios, "
                "the subject becomes easier to understand and much more useful in daily work or learning."
            ),
            (
                "After that, we focus on the actionable steps. These are the habits, methods, and routines "
                "that help turn knowledge into real progress and measurable results."
            ),
            (
                "Finally, we conclude with a summary of the biggest takeaways. By combining insight, structure, "
                "and repetition, you can build lasting understanding and apply the material with confidence."
            ),
        ]

        script = "\n\n".join([intro] + sections)
        return script + "\n\n" + (
            f"This was a high-level overview of {topic}. If you want to go deeper, the best next step is to "
            "break the topic into smaller parts, practice consistently, and build from fundamentals to advanced understanding."
        )
