package com.childrenofcorn.eldorado

import android.R.attr.bitmap
import android.os.Bundle
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import com.childrenofcorn.eldorado.databinding.ActivityQractivityBinding
import com.google.zxing.WriterException


class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQractivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var dimension = 1000

        val URL = intent.getStringExtra("URL")
        val qrCodeGenerator = QRGEncoder(URL, null, QRGContents.Type.TEXT, dimension)

        try {
            val bitmap = qrCodeGenerator.bitmap
            binding.imageViewCode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.d("QRActivity", e.toString())
        }
    }
}