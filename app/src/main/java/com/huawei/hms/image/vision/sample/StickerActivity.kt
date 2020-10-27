/*
 * Copyright 2020. Huawei TechnoLogies Co., Ltd. All rights reserved.
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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.image.vision.sample.StickerActivity
import com.huawei.hms.image.vision.sticker.StickerLayout
import com.huawei.hms.image.vision.sticker.StickerLayout.StickerLayoutListener
import com.huawei.hms.image.vision.sticker.item.TextEditInfo
import com.huawei.secure.android.common.util.LogsUtil

class StickerActivity : AppCompatActivity(), View.OnClickListener {
    private val context: Context? = null
    private var inputBm: Bitmap? = null
    private var btn_removeSticks: Button? = null
    private var fonts: EditText? = null
    private var btn_picture: Button? = null
    private var mStickerLayout: StickerLayout? = null
    private var textEditInfo: TextEditInfo? = null
    private var iv: ImageView? = null
    private var tv: TextView? = null
    var rootPath = ""
    var mButton12: Button? = null
    var mButton13: Button? = null
    var mButton14: Button? = null
    var mButton15: Button? = null
    var mButton16: Button? = null
    var mButton17: Button? = null
    var mButton18: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker)
        try {
            rootPath = baseContext.filesDir.path + "/vgmap/"
            initData()
        } catch (e: Exception) {
            LogsUtil.e(TAG, "Exception: " + e.message)
        }
        mStickerLayout = findViewById(R.id.sticker_container)
        iv = findViewById(R.id.imageView)
        tv = findViewById(R.id.tv)
        btn_picture = findViewById(R.id.btn_picture)
        btn_picture?.setOnClickListener(this)
        mButton12 = findViewById(R.id.button12)
        mButton12?.setOnClickListener(this)
        mButton13 = findViewById(R.id.button13)
        mButton13?.setOnClickListener(this)
        mButton14 = findViewById(R.id.button14)
        mButton14?.setOnClickListener(this)
        mButton15 = findViewById(R.id.button15)
        mButton15?.setOnClickListener(this)
        mButton16 = findViewById(R.id.button16)
        mButton16?.setOnClickListener(this)
        mButton17 = findViewById(R.id.button17)
        mButton17?.setOnClickListener(this)
        mButton18 = findViewById(R.id.button18)
        mButton18?.setOnClickListener(this)
        btn_removeSticks = findViewById(R.id.btn_removeSticks)
        btn_removeSticks?.setOnClickListener(this)
        fonts = findViewById(R.id.fonts)
        fonts?.setVisibility(View.GONE)
        fonts?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                textEditInfo!!.text = s.toString()
                mStickerLayout?.updateStickerText(textEditInfo)
                mStickerLayout?.postInvalidate()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mStickerLayout?.setStickerLayoutListener(object : StickerLayoutListener {
            override fun onStickerLayoutClick() {
                fonts?.setVisibility(View.GONE)
            }

            override fun onStickerTouch(index: Int) {}
            override fun onTextEdit(textEditInfo: TextEditInfo) {
                this@StickerActivity.textEditInfo = textEditInfo
                fonts?.setVisibility(View.VISIBLE)
                fonts?.setText(textEditInfo.text)
            }

            override fun needDisallowInterceptTouchEvent(isNeed: Boolean) {}
        })
    }

    private fun initData() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            this@StickerActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Utility.copyAssetsFileToDirs(
                baseContext,
                "vgmap",
                rootPath
            )
        } else {
            ActivityCompat.requestPermissions(
                this@StickerActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (null != data) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK && requestCode == 801) {
                try {
                    inputBm =
                        Utility.getBitmapFromUri(data, this)
                    iv!!.setImageBitmap(inputBm)
                } catch (e: Exception) {
                    LogsUtil.e(TAG, "Exception: " + e.message)
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button12 -> addSticker(rootPath + "textArt1", "")
            R.id.button13 -> addSticker(rootPath + "textArt2", "")
            R.id.button14 -> addSticker(rootPath + "textArt3", "")
            R.id.button15 -> addSticker(rootPath + "textArt4", "")
            R.id.button16 -> addSticker(rootPath + "sticker1", "sticker_10_editable.png")
            R.id.button17 -> addSticker(rootPath + "sticker2", "sticker_6_editable.png")
            R.id.button18 -> addSticker(rootPath + "sticker3", "sticker_6_editable.png")
            R.id.btn_picture -> Utility.getByAlbum(this)
            R.id.btn_removeSticks -> removeStickers()
            else -> {
            }
        }
    }

    private fun addSticker(rootPath: String, fileName: String) {
        val resultCode = mStickerLayout!!.addSticker(rootPath, fileName)
        tv!!.text = "resultCode：$resultCode"
    }

    private fun removeStickers() {
        mStickerLayout!!.removeAllSticker()
    }

    companion object {
        private const val TAG = "StickerActivity"
        const val PERMISSION_REQUEST_CODE = 0x01
    }
}