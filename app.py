import os
import threading
import uuid
from pathlib import Path

from flask import Flask, jsonify, render_template, request, send_from_directory, url_for
from dotenv import load_dotenv

from video_generator import VideoGenerator

load_dotenv()

app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 16 * 1024
video_generator = VideoGenerator()
VIDEO_DIR = Path(video_generator.composer.output_dir).resolve()
VIDEO_DIR.mkdir(parents=True, exist_ok=True)
jobs = {}
jobs_lock = threading.Lock()


def run_job(job_id, topic, duration):
    try:
        result = video_generator.generate(topic=topic, duration_seconds=duration)
        video_path = Path(result["video_path"]).resolve()
        audio_path = Path(result["audio_path"]).resolve()
        with jobs_lock:
            jobs[job_id] = {
                "id": job_id,
                "status": "completed",
                "percent": 100,
                "message": "Video ready",
                "video_url": url_for("download_video", filename=video_path.name),
                "audio_url": url_for("download_audio", filename=audio_path.name) if audio_path.exists() else None,
                "script_preview": result.get("script_preview", ""),
                "duration_seconds": duration,
            }
    except Exception as exc:
        with jobs_lock:
            jobs[job_id] = {"id": job_id, "status": "failed", "percent": 0, "message": str(exc)}


@app.get("/")
def index():
    return render_template("index.html")


@app.post("/api/jobs")
def create_job():
    payload = request.get_json(silent=True) or {}
    topic = str(payload.get("topic", "")).strip()
    try:
        duration = int(payload.get("duration", 600))
    except (TypeError, ValueError):
        duration = 600
    if not topic:
        return jsonify({"error": "Please describe the video you want to create."}), 400
    if len(topic) > 1000:
        return jsonify({"error": "The video description is too long."}), 400
    if duration < 10 or duration > 6000:
        return jsonify({"error": "Duration must be between 10 seconds and 100 minutes."}), 400

    job_id = uuid.uuid4().hex
    with jobs_lock:
        jobs[job_id] = {"id": job_id, "status": "queued", "percent": 0, "message": "Queued"}
    threading.Thread(target=run_job, args=(job_id, topic, duration), daemon=True).start()
    return jsonify({"id": job_id, "status_url": url_for("job_status", job_id=job_id)}), 202


@app.get("/api/jobs/<job_id>")
def job_status(job_id):
    with jobs_lock:
        job = jobs.get(job_id)
    if not job:
        return jsonify({"error": "Job not found."}), 404
    # The generator currently exposes one shared progress object; use it while this job runs.
    if job["status"] in {"queued", "running"}:
        progress = video_generator.get_progress()
        job = {**job, "status": "running", "percent": progress.get("percent", 0), "message": progress.get("status", "Working")}
        with jobs_lock:
            jobs[job_id] = job
    return jsonify(job)


@app.get("/videos/<path:filename>")
def download_video(filename):
    return send_from_directory(VIDEO_DIR, filename, as_attachment=False, mimetype="video/mp4")


@app.get("/audio/<path:filename>")
def download_audio(filename):
    audio_dir = Path(video_generator.voiceover_engine.output_dir).resolve()
    return send_from_directory(audio_dir, filename, as_attachment=True)


@app.get("/healthz")
def healthz():
    return jsonify({"ok": True})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("PORT", "5000")), debug=os.getenv("FLASK_DEBUG") == "1")
