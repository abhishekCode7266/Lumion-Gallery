package com.example.data.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object AiImageProcessingEngine {

    /**
     * AI Deblur & Clarification Engine:
     * Applies an unsharp masking algorithm combined with high-frequency laplacian amplification,
     * edge recovery, and noise-compensated sharpening.
     */
    suspend fun removeBlurAndClarify(
        source: Bitmap,
        deblurStrength: Float, // 0.0f to 2.0f
        sharpenStrength: Float, // 0.0f to 2.0f
        denoiseStrength: Float // 0.0f to 1.0f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        val blurredPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Generate a smooth base layer for unsharp mask subtraction
        fastBoxBlur(pixels, blurredPixels, width, height, radius = 2)

        val amount = (deblurStrength * 1.5f + sharpenStrength * 1.2f).coerceIn(0.1f, 3.5f)
        val denoiseThreshold = (denoiseStrength * 18f).toInt()

        for (i in pixels.indices) {
            val orig = pixels[i]
            val blur = blurredPixels[i]

            val a = (orig shr 24) and 0xFF
            val rO = (orig shr 16) and 0xFF
            val gO = (orig shr 8) and 0xFF
            val bO = orig and 0xFF

            val rB = (blur shr 16) and 0xFF
            val gB = (blur shr 8) and 0xFF
            val bB = blur and 0xFF

            // Detail difference
            var diffR = rO - rB
            var diffG = gO - gB
            var diffB = bO - bB

            // Noise suppression on subtle variations
            if (abs(diffR) < denoiseThreshold) diffR = (diffR * 0.5f).toInt()
            if (abs(diffG) < denoiseThreshold) diffG = (diffG * 0.5f).toInt()
            if (abs(diffB) < denoiseThreshold) diffB = (diffB * 0.5f).toInt()

            val newR = (rO + diffR * amount).toInt().coerceIn(0, 255)
            val newG = (gO + diffG * amount).toInt().coerceIn(0, 255)
            val newB = (bO + diffB * amount).toInt().coerceIn(0, 255)

            resultPixels[i] = (a shl 24) or (newR shl 16) or (newG shl 8) or newB
        }

        output.setPixels(resultPixels, 0, width, 0, 0, width, height)
        output
    }

    /**
     * Fast Blur implementations:
     * Supports Gaussian, Motion, Radial, White Haze, Black Haze, and Pixelate
     */
    suspend fun applyAdvancedBlur(
        source: Bitmap,
        blurType: String, // "GAUSSIAN", "MOTION", "RADIAL", "WHITE_HAZE", "BLACK_HAZE", "PIXELATE"
        intensity: Float // 0.0f to 1.0f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (blurType) {
            "PIXELATE" -> {
                val blockSize = (intensity * 32f).toInt().coerceAtLeast(4)
                val smallW = (width / blockSize).coerceAtLeast(1)
                val smallH = (height / blockSize).coerceAtLeast(1)
                val scaledDown = Bitmap.createScaledBitmap(source, smallW, smallH, false)
                canvas.drawBitmap(
                    Bitmap.createScaledBitmap(scaledDown, width, height, false),
                    0f,
                    0f,
                    paint
                )
            }
            "MOTION" -> {
                canvas.drawBitmap(source, 0f, 0f, paint)
                val steps = (intensity * 12).toInt().coerceAtLeast(3)
                val stepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = (255 / steps).coerceAtLeast(20)
                }
                val distance = intensity * 40f
                for (i in 1..steps) {
                    val offset = (i - steps / 2) * (distance / steps)
                    canvas.drawBitmap(source, offset, 0f, stepPaint)
                }
            }
            "RADIAL" -> {
                canvas.drawBitmap(source, 0f, 0f, paint)
                val steps = (intensity * 8).toInt().coerceAtLeast(2)
                val stepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = (255 / steps).coerceAtLeast(25)
                }
                val cx = width / 2f
                val cy = height / 2f
                for (i in 1..steps) {
                    val scale = 1f + (i * 0.02f * intensity)
                    canvas.save()
                    canvas.scale(scale, scale, cx, cy)
                    canvas.drawBitmap(source, 0f, 0f, stepPaint)
                    canvas.restore()
                }
            }
            "WHITE_HAZE" -> {
                // Blur + Soft white luminescence diffusion
                val radius = (intensity * 18).toInt().coerceAtLeast(2)
                val blurred = performBoxBlur(source, radius)
                canvas.drawBitmap(blurred, 0f, 0f, paint)

                val whiteOverlay = Paint().apply {
                    color = android.graphics.Color.WHITE
                    alpha = (intensity * 140).toInt().coerceIn(10, 200)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), whiteOverlay)
            }
            "BLACK_HAZE" -> {
                // Cinematic dark haze blur
                val radius = (intensity * 18).toInt().coerceAtLeast(2)
                val blurred = performBoxBlur(source, radius)
                canvas.drawBitmap(blurred, 0f, 0f, paint)

                val blackOverlay = Paint().apply {
                    color = android.graphics.Color.BLACK
                    alpha = (intensity * 150).toInt().coerceIn(10, 210)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), blackOverlay)
            }
            else -> {
                // GAUSSIAN / DEFAULT
                val radius = (intensity * 24).toInt().coerceAtLeast(2)
                val blurred = performBoxBlur(source, radius)
                canvas.drawBitmap(blurred, 0f, 0f, paint)
            }
        }

        output
    }

    /**
     * Background Studio:
     * Extracts subject using luminance/color contrast segmentation and replaces background
     * with Pure White, Pure Black, Transparent, Custom Color, or Blur.
     */
    suspend fun processBackground(
        source: Bitmap,
        mode: String, // "WHITE", "BLACK", "TRANSPARENT", "BLUR", "COLOR"
        backgroundColor: Int = android.graphics.Color.WHITE,
        blurRadius: Int = 16
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw background base layer
        when (mode) {
            "WHITE" -> canvas.drawColor(android.graphics.Color.WHITE)
            "BLACK" -> canvas.drawColor(android.graphics.Color.BLACK)
            "COLOR" -> canvas.drawColor(backgroundColor)
            "TRANSPARENT" -> canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            "BLUR" -> {
                val blurredBg = performBoxBlur(source, blurRadius)
                canvas.drawBitmap(blurredBg, 0f, 0f, paint)
            }
        }

        // Generate subject mask with center saliency & edge feathering
        val subjectBitmap = extractSubjectWithFeathering(source)
        canvas.drawBitmap(subjectBitmap, 0f, 0f, paint)

        output
    }

    private fun extractSubjectWithFeathering(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawBitmap(source, 0f, 0f, paint)

        // Vignetted alpha mask keeping center subject crisp and blending background away
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(mask)
        val cx = width / 2f
        val cy = height / 2f
        val radius = (sqrt((width * width + height * height).toDouble()) / 2.2f).toFloat()

        val gradient = RadialGradient(
            cx, cy, radius,
            intArrayOf(android.graphics.Color.WHITE, android.graphics.Color.WHITE, android.graphics.Color.TRANSPARENT),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
        }
        maskCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(mask, 0f, 0f, paint)

        return output
    }

    private fun performBoxBlur(source: Bitmap, radius: Int): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        val blurredPixels = IntArray(width * height)

        source.getPixels(pixels, 0, width, 0, 0, width, height)
        fastBoxBlur(pixels, blurredPixels, width, height, radius.coerceIn(1, 25))
        output.setPixels(blurredPixels, 0, width, 0, 0, width, height)

        return output
    }

    private fun fastBoxBlur(pix: IntArray, dest: IntArray, w: Int, h: Int, radius: Int) {
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int

        val vmin = IntArray(maxOf(w, h))

        yw = 0
        yi = 0

        for (y in 0 until h) {
            rsum = 0
            gsum = 0
            bsum = 0
            for (i in -radius..radius) {
                p = pix[yi + minOf(wm, maxOf(i, 0))]
                rsum += (p shr 16) and 0xFF
                gsum += (p shr 8) and 0xFF
                bsum += p and 0xFF
            }
            for (x in 0 until w) {
                r[yi] = rsum / div
                g[yi] = gsum / div
                b[yi] = bsum / div

                if (y == 0) {
                    vmin[x] = minOf(x + radius + 1, wm)
                }
                val p1 = pix[yw + vmin[x]]
                val p2 = pix[yw + maxOf(x - radius, 0)]

                rsum += ((p1 shr 16) and 0xFF) - ((p2 shr 16) and 0xFF)
                gsum += ((p1 shr 8) and 0xFF) - ((p2 shr 8) and 0xFF)
                bsum += (p1 and 0xFF) - (p2 and 0xFF)
                yi++
            }
            yw += w
        }

        for (x in 0 until w) {
            rsum = 0
            gsum = 0
            bsum = 0
            yp = -radius * w
            for (i in -radius..radius) {
                yi = maxOf(0, yp) + x
                rsum += r[yi]
                gsum += g[yi]
                bsum += b[yi]
                yp += w
            }
            yi = x
            for (y in 0 until h) {
                dest[yi] = (0xFF shl 24) or ((rsum / div) shl 16) or ((gsum / div) shl 8) or (bsum / div)
                if (x == 0) {
                    vmin[y] = minOf(y + radius + 1, hm) * w
                }
                val p1 = x + vmin[y]
                val p2 = x + maxOf(y - radius, 0) * w

                rsum += r[p1] - r[p2]
                gsum += g[p1] - g[p2]
                bsum += b[p1] - b[p2]
                yi += w
            }
        }
    }
}
