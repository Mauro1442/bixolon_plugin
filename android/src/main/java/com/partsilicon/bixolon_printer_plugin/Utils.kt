package com.partsilicon.bixolon_printer_plugin

import android.graphics.*

fun textToBitmap(text:String): Bitmap? {
    val txts =  text.split('\n')
    var lineHeight = 36f
    val bmpW = 420

    val pnt = Paint();
    pnt.setColor(Color.WHITE)
    pnt.textSize = 23.0f
    pnt.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
    pnt.setAntiAlias(true)
    //pnt.textAlign = Paint.Align.CENTER

    var lineNo = 0
    val mapLines = ArrayList< Pair<String , Float>>()

    txts.forEachIndexed{ i , str ->
        run {
            if(str.isNotEmpty()) {

                val widthPixel = pnt.measureText(str)
                if (widthPixel <= bmpW - 30) {
                    val offset = (bmpW - widthPixel).div(2)
                    //canvas.drawText(str, offset, (lineNo) * lineHeight, pnt)
                    mapLines.add(Pair<String, Float>(str, offset))
                    lineNo++
                } else {
                    //val lineNeeded = (widthPixel / bmpW).toInt()
                    val words = str.split(" ")
                    var lineStr: String = ""
                    words.forEachIndexed { wI, it ->
                        if (it == null)
                            return@forEachIndexed
                        lineStr = "$lineStr$it "
                        if (pnt.measureText(lineStr) >= bmpW - 30 || wI == words.size - 1) {
                            val offset = (bmpW - pnt.measureText(lineStr)).div(2)
                            //canvas.drawText(lineStr, offset, (lineNo) * lineHeight, pnt)
                            mapLines.add(Pair<String, Float>(lineStr, offset))
                            lineNo++
                            lineStr = ""
                        }
                    }
                }
            }
        }
    }


    val bmpH = (lineNo * lineHeight.toInt())
    val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap);
    canvas.drawRect(0.0f,0.0f, bmpW.toFloat() , bmpH.toFloat() , pnt)

    pnt.setColor(Color.BLACK)

    mapLines.forEachIndexed { index, pair ->
        var lastLineY = (index.toFloat()+0.5f) * lineHeight
        canvas.drawText(pair.first, pair.second, lastLineY, pnt)
    }

    canvas.save()


    //imageView.setImageBitmap(bitmap)
    return bitmap
}