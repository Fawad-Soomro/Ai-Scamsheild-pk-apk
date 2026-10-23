package com.example.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.Recommendation
import com.example.data.model.RiskIndicator
import com.example.data.model.ScanRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateReport(
        context: Context,
        record: ScanRecord,
        indicators: List<RiskIndicator>,
        recommendations: List<Recommendation>,
        includeRawContent: Boolean = false
    ): Result<File> {
        val pdfDocument = PdfDocument()

        return try {
            val pageWidth = 595 // Standard A4 width in PostScript points
            val pageHeight = 842 // Standard A4 height in PostScript points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Header Banner (Cyber Navy)
            paint.color = Color.rgb(10, 25, 47) // Deep navy
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 75f, paint)

            // Header Accent Line (Emerald/Cyan)
            paint.color = Color.rgb(0, 230, 153) // Cyber Emerald
            paint.strokeWidth = 3f
            canvas.drawLine(0f, 75f, pageWidth.toFloat(), 75f, paint)

            // Title Text
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("AI ScamShield PK", 25f, 38f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(136, 146, 176) // Light slate
            canvas.drawText("Digital Threat Risk Assessment & Forensic Report", 25f, 56f, paint)

            // Header Right - Date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = dateFormat.format(Date(record.timestampMs))
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.WHITE
            paint.textSize = 9f
            canvas.drawText("Scan ID: #${record.id}", pageWidth - 25f, 36f, paint)
            paint.color = Color.rgb(136, 146, 176)
            canvas.drawText("Generated: $dateStr", pageWidth - 25f, 54f, paint)
            paint.textAlign = Paint.Align.LEFT

            var currentY = 100f

            // Risk Assessment Box
            val riskColor = when (record.riskLevel.uppercase()) {
                "CRITICAL" -> Color.rgb(239, 68, 68)
                "HIGH" -> Color.rgb(249, 115, 22)
                "MODERATE" -> Color.rgb(245, 158, 11)
                else -> Color.rgb(16, 185, 129)
            }

            val boxRect = RectF(25f, currentY, pageWidth - 25f, currentY + 95f)
            paint.color = Color.rgb(245, 247, 250)
            canvas.drawRoundRect(boxRect, 8f, 8f, paint)

            // Left colored stripe
            paint.color = riskColor
            canvas.drawRoundRect(RectF(25f, currentY, 32f, currentY + 95f), 4f, 4f, paint)

            // Score Dial / Number
            paint.color = riskColor
            paint.textSize = 28f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${record.riskScore}", 46f, currentY + 45f, paint)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText("/ 100 Risk Score", 46f, currentY + 62f, paint)

            // Risk Level & Category
            paint.color = riskColor
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${record.riskLevel.uppercase()} SEVERITY", 150f, currentY + 30f, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 12f
            canvas.drawText(record.category, 150f, currentY + 50f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(
                "Input Type: ${record.inputType}  •  Confidence: ${(record.confidence * 100).toInt()}%  •  Language: ${record.language}",
                150f,
                currentY + 70f,
                paint
            )

            currentY += 115f

            // Assessment Summary
            if (record.summary.isNotBlank()) {
                paint.color = Color.rgb(15, 23, 42)
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("EXECUTIVE SUMMARY", 25f, currentY, paint)
                currentY += 15f

                paint.color = Color.rgb(51, 65, 85)
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                currentY = drawWrappedText(canvas, paint, record.summary, 25f, currentY, pageWidth - 50f)
                currentY += 15f
            }

            // Submitted Content Section (Privacy Conscious)
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("SUBMITTED EVIDENCE CONTENT", 25f, currentY, paint)
            currentY += 15f

            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            val contentText = if (includeRawContent) {
                record.rawInput.take(300) + if (record.rawInput.length > 300) " [truncated...]" else ""
            } else {
                "[Content masked by user for privacy protection. Modality: ${record.inputType}, length: ${record.rawInput.length} characters]"
            }
            currentY = drawWrappedText(canvas, paint, contentText, 25f, currentY, pageWidth - 50f)
            currentY += 15f

            // Indicators Section
            if (indicators.isNotEmpty()) {
                paint.color = Color.rgb(15, 23, 42)
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("DETECTED THREAT INDICATORS (${indicators.size})", 25f, currentY, paint)
                currentY += 15f

                indicators.take(3).forEach { ind ->
                    paint.color = when (ind.severity.uppercase()) {
                        "CRITICAL" -> Color.rgb(239, 68, 68)
                        "HIGH" -> Color.rgb(249, 115, 22)
                        else -> Color.rgb(245, 158, 11)
                    }
                    paint.textSize = 9.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("• [${ind.severity.uppercase()}] ${ind.name}", 30f, currentY, paint)
                    currentY += 13f

                    paint.color = Color.rgb(51, 65, 85)
                    paint.textSize = 8.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    currentY = drawWrappedText(canvas, paint, "  Evidence: \"${ind.evidence}\" — ${ind.explanation}", 35f, currentY, pageWidth - 60f)
                    currentY += 6f
                }
                currentY += 8f
            }

            // Recommendations Section
            if (recommendations.isNotEmpty()) {
                paint.color = Color.rgb(15, 23, 42)
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("RECOMMENDED PROTECTIVE ACTIONS", 25f, currentY, paint)
                currentY += 15f

                recommendations.take(3).forEach { rec ->
                    paint.color = Color.rgb(16, 185, 129)
                    paint.textSize = 9.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("✔ ${rec.action}", 30f, currentY, paint)
                    currentY += 13f

                    if (rec.description.isNotBlank()) {
                        paint.color = Color.rgb(71, 85, 105)
                        paint.textSize = 8.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        currentY = drawWrappedText(canvas, paint, "  ${rec.description}", 35f, currentY, pageWidth - 60f)
                        currentY += 6f
                    }
                }
                currentY += 8f
            }

            // Emergency Contacts in Pakistan
            val helplineRect = RectF(25f, currentY, pageWidth - 25f, currentY + 45f)
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(helplineRect, 6f, 6f, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Official Pakistan Cyber Crime Helplines:", 35f, currentY + 18f, paint)

            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("• FIA National Response Centre for Cyber Crime (NR3C): Call 1991", 35f, currentY + 34f, paint)
            canvas.drawText("• PTA Telecom Consumer Protection: 0800-55055", 330f, currentY + 34f, paint)

            currentY += 60f

            // Footer
            val footerY = pageHeight - 35f
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 1f
            canvas.drawLine(25f, footerY - 12f, pageWidth - 25f, footerY - 12f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Disclaimer: Probabilistic AI security assessment. Not a legal or forensic guarantee.", 25f, footerY, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("Developed by Team Trident AI", pageWidth - 25f, footerY, paint)
            paint.textAlign = Paint.Align.LEFT

            pdfDocument.finishPage(page)

            // Save PDF to cache/reports/
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val reportFile = File(reportsDir, "scamshield_report_${record.id}.pdf")
            val outputStream = FileOutputStream(reportFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()

            Result.success(reportFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawWrappedText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float
    ): Float {
        var y = startY
        val words = text.split(" ")
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, y, paint)
                y += paint.textSize + 4f
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, y, paint)
            y += paint.textSize + 4f
        }
        return y
    }
}
