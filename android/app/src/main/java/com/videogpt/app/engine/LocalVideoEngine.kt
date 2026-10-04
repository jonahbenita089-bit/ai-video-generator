package com.videogpt.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.min

class LocalVideoEngine(private val context: Context) {
    fun createLocalMp4(prompt: String, output: File): File {
        val width = 1920
        val height = 1080
        val frames = 24
        val fps = 24
        val mime = MediaFormat.MIMETYPE_VIDEO_AVC
        val format = MediaFormat.createVideoFormat(mime, width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaFormat.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 8_000_000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)

        val codec = MediaCodec.createEncoderByType(mime)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        val muxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var videoTimeUs = 0L
        var isMuxerStarted = false

        try {
            val bufferInfo = MediaCodec.BufferInfo()
            for (frameIndex in 0 until frames) {
                val inputIndex = codec.dequeueInputBuffer(-1)
                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)
                    if (inputBuffer != null) {
                        val bitmap = createFrameBitmap(prompt, frameIndex, width, height)
                        val yuv = convertBitmapToYuv(bitmap)
                        inputBuffer.clear()
                        inputBuffer.put(yuv)
                        codec.queueInputBuffer(inputIndex, 0, yuv.size, videoTimeUs, 0)
                    }
                }

                var outputIndex: Int
                do {
                    outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                    if (outputIndex >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outputIndex)
                        if (!isMuxerStarted && trackIndex == -1) {
                            val outFormat = codec.outputFormat
                            trackIndex = muxer.addTrack(outFormat)
                            muxer.start()
                            isMuxerStarted = true
                        }
                        if (outputBuffer != null) {
                            val data = ByteArray(bufferInfo.size)
                            outputBuffer.get(data)
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                                muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                            }
                        }
                        codec.releaseOutputBuffer(outputIndex, false)
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        val newFormat = codec.outputFormat
                        if (trackIndex == -1) {
                            trackIndex = muxer.addTrack(newFormat)
                            muxer.start()
                            isMuxerStarted = true
                        }
                    }
                } while (outputIndex >= 0)
                videoTimeUs += 1000000L / fps
            }
        } finally {
            codec.stop()
            codec.release()
            if (isMuxerStarted) {
                muxer.stop()
            }
            muxer.release()
        }

        return output
    }

    private fun createFrameBitmap(prompt: String, frameIndex: Int, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bg = if (frameIndex % 2 == 0) Color.rgb(32, 48, 96) else Color.rgb(18, 80, 70)
        canvas.drawColor(bg)
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 100f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(prompt.take(46), width / 2f, height / 2f, paint)
        return bitmap
    }

    private fun convertBitmapToYuv(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val yuv = ByteArray(width * height * 3 / 2)
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        var index = 0
        var uvIndex = width * height
        var yIndex = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val pixel = argb[yIndex++]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                yuv[index++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
            }
        }
        for (j in 0 until height step 2) {
            for (i in 0 until width step 2) {
                val idx1 = j * width + i
                val idx2 = idx1 + 1
                val idx3 = idx1 + width
                val idx4 = idx3 + 1
                val p1 = argb[idx1]
                val p2 = argb[idx2]
                val p3 = argb[idx3]
                val p4 = argb[idx4]
                val r = ((p1 shr 16) + (p2 shr 16) + (p3 shr 16) + (p4 shr 16)) / 4
                val g = ((p1 shr 8) + (p2 shr 8) + (p3 shr 8) + (p4 shr 8)) / 4
                val b1 = ((p1 and 0xFF) + (p2 and 0xFF) + (p3 and 0xFF) + (p4 and 0xFF)) / 4
                val u = ((-38 * r - 74 * g + 112 * b1 + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b1 + 128) shr 8) + 128
                yuv[uvIndex++] = u.toByte()
                yuv[uvIndex++] = v.toByte()
            }
        }
        return yuv
    }
}
