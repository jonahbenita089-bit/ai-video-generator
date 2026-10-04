package com.videogpt.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject

class VideoCompositionEngine @Inject constructor(private val context: Context) {
    fun createVideoFromScenes(
        scenes: List<String>,
        outputFile: File,
        durationSeconds: Int = 30,
        onProgress: (Int) -> Unit = {}
    ): Boolean = try {
        val width = 1920
        val height = 1080
        val fps = 24
        val totalFrames = (durationSeconds * fps)
        val framesPerScene = if (scenes.isNotEmpty()) totalFrames / scenes.size else totalFrames

        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val format = MediaFormat.createVideoFormat(mimeType, width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 5_000_000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        val codec = MediaCodec.createEncoderByType(mimeType)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var videoTrackIndex = -1
        var frameCount = 0
        var timestampUs = 0L
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            var sceneIndex = 0
            var frameInScene = 0

            for (i in 0 until totalFrames) {
                if (sceneIndex >= scenes.size) sceneIndex = scenes.size - 1

                val inputIndex = codec.dequeueInputBuffer(10000)
                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)
                    if (inputBuffer != null) {
                        val bitmap = renderSceneBitmap(scenes[sceneIndex], width, height, frameInScene, framesPerScene)
                        val yuvData = bitmapToYuv420(bitmap)
                        inputBuffer.clear()
                        inputBuffer.put(yuvData)
                        inputBuffer.rewind()
                        codec.queueInputBuffer(inputIndex, 0, yuvData.size, timestampUs, 0)
                    }
                }

                var outputIndex: Int
                do {
                    outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                    when (outputIndex) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            if (videoTrackIndex == -1) {
                                videoTrackIndex = muxer.addTrack(codec.outputFormat)
                                muxer.start()
                            }
                        }
                        MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                        else -> {
                            if (outputIndex >= 0) {
                                val outputBuffer = codec.getOutputBuffer(outputIndex) ?: continue
                                if (videoTrackIndex != -1 && bufferInfo.size > 0) {
                                    muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                                }
                                codec.releaseOutputBuffer(outputIndex, false)
                                frameCount++
                            }
                        }
                    }
                } while (outputIndex >= 0)

                frameInScene++
                if (frameInScene >= framesPerScene) {
                    sceneIndex++
                    frameInScene = 0
                }
                timestampUs = (i * 1_000_000L) / fps
                onProgress((i * 100) / totalFrames)
            }

            codec.signalEndOfInputStream()
            var outputIndex: Int
            do {
                outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex) ?: continue
                    if (videoTrackIndex != -1 && bufferInfo.size > 0) {
                        muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                }
            } while (outputIndex >= 0 || (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) == 0)
        } finally {
            codec.stop()
            codec.release()
            if (videoTrackIndex != -1) {
                muxer.stop()
            }
            muxer.release()
        }

        Log.d("VideoEngine", "Video created successfully: ${outputFile.absolutePath}")
        true
    } catch (e: Exception) {
        Log.e("VideoEngine", "Error creating video", e)
        false
    }

    private fun renderSceneBitmap(
        sceneText: String,
        width: Int,
        height: Int,
        frameInScene: Int,
        totalFramesInScene: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = if (frameInScene % 2 == 0) Color.rgb(20, 30, 60) else Color.rgb(15, 70, 80)
        canvas.drawColor(bgColor)

        val progress = if (totalFramesInScene > 0) frameInScene.toFloat() / totalFramesInScene else 0f
        val alpha = (progress * 255).toInt()

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 140f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            alpha = minOf(255, alpha + 100)
        }

        val lines = sceneText.split(" - ")
        var y = 400f
        for (line in lines) {
            canvas.drawText(line.take(40), width / 2f, y, paint)
            y += 200f
        }

        return bitmap
    }

    private fun bitmapToYuv420(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixelCount = width * height
        val uvPixelCount = pixelCount / 4
        val nv21 = ByteArray(pixelCount + uvPixelCount * 2)

        val argb = IntArray(pixelCount)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        var yIndex = 0
        var uvIndex = pixelCount

        for (j in 0 until height) {
            for (i in 0 until width) {
                val pixel = argb[yIndex]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                nv21[yIndex++] = y.coerceIn(0, 255).toByte()
            }
        }

        for (j in 0 until height step 2) {
            for (i in 0 until width step 2) {
                val idx = j * width + i
                val r = ((argb[idx] shr 16) and 0xFF) +
                        ((argb[idx + 1] shr 16) and 0xFF) +
                        ((argb[idx + width] shr 16) and 0xFF) +
                        ((argb[idx + width + 1] shr 16) and 0xFF)
                val g = ((argb[idx] shr 8) and 0xFF) +
                        ((argb[idx + 1] shr 8) and 0xFF) +
                        ((argb[idx + width] shr 8) and 0xFF) +
                        ((argb[idx + width + 1] shr 8) and 0xFF)
                val b = (argb[idx] and 0xFF) +
                        (argb[idx + 1] and 0xFF) +
                        (argb[idx + width] and 0xFF) +
                        (argb[idx + width + 1] and 0xFF)

                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 10) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 10) + 128

                nv21[uvIndex++] = v.coerceIn(0, 255).toByte()
                nv21[uvIndex++] = u.coerceIn(0, 255).toByte()
            }
        }

        return nv21
    }
}
