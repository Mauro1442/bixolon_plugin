package com.partsilicon.eliteutils

import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import android.graphics.Bitmap as Bitmap1

var QR_width = 280

fun generateQRCode(text: String): Bitmap1 {

    val bitmap = Bitmap1.createBitmap(QR_width, QR_width, Bitmap1.Config.ARGB_8888)
    val codeWriter = MultiFormatWriter()
    try {
        val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, QR_width, QR_width)
        for (x in 0 until QR_width) {
            for (y in 0 until QR_width) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
    } catch (e: WriterException) {
        Log.d("QrCodeUril", "generateQRCode: ${e.message}")
    }
    return bitmap
}