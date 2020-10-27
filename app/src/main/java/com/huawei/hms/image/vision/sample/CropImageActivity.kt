/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.hms.image.vision.sample

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.image.vision.crop.CropLayoutView
import com.huawei.secure.android.common.intent.SafeIntent

/**
 * The type Crop activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
class CropImageActivity : AppCompatActivity(), View.OnClickListener {
    private val picPath: String? = null
    private var inputBm: Bitmap? = null
    private var cropImage: Button? = null
    private var flipH: Button? = null
    private var flipV: Button? = null
    private var rotate: Button? = null
    private val options: BitmapFactory.Options? = null
    private var cropLayoutView: CropLayoutView? = null
    private var rgCrop: RadioGroup? = null
    private var rbCircular: RadioButton? = null
    private var rbRectangle: RadioButton? = null
    private var spinner: Spinner? = null
    private val context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        cropLayoutView = findViewById(R.id.cropImageView)
        cropImage = findViewById(R.id.btn_crop_image)
        rotate = findViewById(R.id.btn_rotate)
        flipH = findViewById(R.id.btn_flip_horizontally)
        flipV = findViewById(R.id.btn_flip_vertically)
        cropLayoutView?.setAutoZoomEnabled(true)
        cropLayoutView?.setCropShape(CropLayoutView.CropShape.RECTANGLE)
        cropImage?.setOnClickListener(this)
        rotate?.setOnClickListener(this)
        flipH?.setOnClickListener(this)
        flipV?.setOnClickListener(this)
        rbCircular = findViewById(R.id.rb_circular)
        rgCrop = findViewById(R.id.rb_crop)
        rgCrop?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            val radioButton =
                radioGroup.findViewById<RadioButton>(i)
            if (radioButton == rbCircular) {
                cropLayoutView?.setCropShape(CropLayoutView.CropShape.OVAL)
            } else {
                cropLayoutView?.setCropShape(CropLayoutView.CropShape.RECTANGLE)
            }
        })
        spinner = findViewById<View>(R.id.spinner1) as Spinner
        spinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                pos: Int,
                id: Long
            ) {
                val ratios =
                    resources.getStringArray(R.array.ratios)
                try {
                    val ratioX = ratios[pos].split(":").toTypedArray()[0].toInt()
                    val ratioY = ratios[pos].split(":").toTypedArray()[1].toInt()
                    cropLayoutView?.setAspectRatio(ratioX, ratioY)
                } catch (e: Exception) {
                    cropLayoutView?.setFixedAspectRatio(false)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Another interface callback
            }
        }
        rbRectangle = findViewById(R.id.rb_rectangle)
        val intent: Intent = SafeIntent(intent)
        inputBm = Utility.getBitmapFromUriStr(intent, this)
        cropLayoutView?.setImageBitmap(inputBm)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_crop_image -> {
                val croppedImage = cropLayoutView!!.croppedImage
                cropLayoutView!!.setImageBitmap(croppedImage)
            }
            R.id.btn_rotate -> cropLayoutView!!.rotateClockwise()
            R.id.btn_flip_horizontally -> cropLayoutView!!.flipImageHorizontally()
            R.id.btn_flip_vertically -> cropLayoutView!!.flipImageVertically()
        }
    }

    companion object {
        private const val TAG = "CropImageActivity"
    }
}