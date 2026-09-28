package com.stitchilyas.vakitvedua.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

object CardGenerator {

    enum class CardStyle(
        val styleName: String,
        val bgColors: IntArray,
        val accentColor: Int,
        val textColor: Int,
        val subTextColor: Int
    ) {
        EMERALD_GOLD(
            "Zümrüt & Altın",
            intArrayOf(Color.parseColor("#0F3832"), Color.parseColor("#1B5E20")),
            Color.parseColor("#FFD700"),
            Color.WHITE,
            Color.parseColor("#B2DFDB")
        ),
        ROYAL_GOLD(
            "Saray Altını",
            intArrayOf(Color.parseColor("#3E2723"), Color.parseColor("#21120E")),
            Color.parseColor("#FFC107"),
            Color.parseColor("#FFF8E1"),
            Color.parseColor("#FFE082")
        ),
        MIDNIGHT_BLUE(
            "Gece Mavisi & Mehtap",
            intArrayOf(Color.parseColor("#0A192F"), Color.parseColor("#112240")),
            Color.parseColor("#64FFDA"),
            Color.WHITE,
            Color.parseColor("#8892B0")
        ),
        ROSE_CRIMSON(
            "Gül & Bordo",
            intArrayOf(Color.parseColor("#4A0E17"), Color.parseColor("#2A080C")),
            Color.parseColor("#FFD700"),
            Color.parseColor("#FFF5F5"),
            Color.parseColor("#FFCDD2")
        ),
        OTTOMAN_VELVET(
            "Osmanlı Kadife",
            intArrayOf(Color.parseColor("#1A237E"), Color.parseColor("#0D47A1")),
            Color.parseColor("#FFD700"),
            Color.WHITE,
            Color.parseColor("#90CAF9")
        )
    }

    /**
     * Generates a 1080x1350 (4:5 Aspect Ratio) high-definition greeting card Bitmap.
     */
    fun createCardBitmap(
        title: String,
        message: String,
        cardStyle: CardStyle
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Background Gradient
        val bgPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                cardStyle.bgColors, null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Outer & Inner Gold Filigree Borders
        val strokePaint = Paint().apply {
            isAntiAlias = true
            color = cardStyle.accentColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val padding = 45f
        canvas.drawRect(padding, padding, width - padding, height - padding, strokePaint)

        strokePaint.strokeWidth = 2f
        val innerPadding = 60f
        canvas.drawRect(innerPadding, innerPadding, width - innerPadding, height - innerPadding, strokePaint)

        // 3. Corner Ornamental Rosettes (8-Point Islamic Star Rosettes at corners)
        drawCornerStar(canvas, innerPadding, innerPadding, cardStyle.accentColor)
        drawCornerStar(canvas, width - innerPadding, innerPadding, cardStyle.accentColor)
        drawCornerStar(canvas, innerPadding, height - innerPadding, cardStyle.accentColor)
        drawCornerStar(canvas, width - innerPadding, height - innerPadding, cardStyle.accentColor)

        // 4. Header Crescent & Mosque Emblem
        val emblemPaint = Paint().apply {
            isAntiAlias = true
            color = cardStyle.accentColor
            textSize = 54f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🕌   🌙   🕋", width / 2f, 160f, emblemPaint)

        val bismillahPaint = TextPaint().apply {
            isAntiAlias = true
            color = cardStyle.accentColor
            textSize = 42f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.SERIF
        }
        canvas.drawText("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", width / 2f, 230f, bismillahPaint)

        // Decorative Line under Bismillah
        val linePaint = Paint().apply {
            isAntiAlias = true
            color = cardStyle.accentColor
            strokeWidth = 3f
        }
        canvas.drawLine(width / 2f - 180f, 260f, width / 2f + 180f, 260f, linePaint)
        canvas.drawCircle(width / 2f, 260f, 8f, Paint().apply { color = cardStyle.accentColor; isAntiAlias = true })

        // 5. Title Header
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = cardStyle.accentColor
            textSize = 58f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title, width / 2f, 350f, titlePaint)

        // 6. Message Body Text (Wrapped nicely in StaticLayout)
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = cardStyle.textColor
            textSize = 44f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        val textWidth = width - 200
        val staticLayout = StaticLayout.Builder
            .obtain(message, 0, message.length, bodyPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(12f, 1.2f)
            .build()

        val textHeight = staticLayout.height
        val startY = 440f + (550f - textHeight) / 2f

        canvas.save()
        canvas.translate(100f, startY)
        staticLayout.draw(canvas)
        canvas.restore()

        // 7. Footer Decorative Divider & Signature Watermark
        val footerY = height - 160f
        canvas.drawLine(width / 2f - 220f, footerY, width / 2f + 220f, footerY, linePaint)
        canvas.drawCircle(width / 2f, footerY, 8f, Paint().apply { color = cardStyle.accentColor; isAntiAlias = true })

        val footerPaint = TextPaint().apply {
            isAntiAlias = true
            color = cardStyle.subTextColor
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("VAKİT VE DUA · DUALARDA BULUŞMAK DİLEĞİYLE", width / 2f, footerY + 50f, footerPaint)

        return bitmap
    }

    private fun drawCornerStar(canvas: Canvas, cx: Float, cy: Float, colorInt: Int) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = colorInt
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val r = 24f
        val path = Path()
        for (i in 0 until 8) {
            val angle = i * 45f
            val rad = Math.toRadians(angle.toDouble())
            val x = cx + r * cos(rad).toFloat()
            val y = cy + r * sin(rad).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    /**
     * Compress bitmap to PNG file in cacheDir and open System Share Chooser with image/png Intent!
     */
    fun shareCardAsImage(context: Context, bitmap: Bitmap, caption: String = "") {
        try {
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(imagesDir, "vakit_ve_dua_kart_${System.currentTimeMillis()}.png")

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (caption.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Görsel Kartı Paylaş")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Görsel kart paylaşılamadı: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
