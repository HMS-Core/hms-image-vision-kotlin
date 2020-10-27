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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVision.VisionCallBack
import com.huawei.hms.image.vision.ImageVisionImpl
import com.huawei.hms.image.vision.bean.ImageLayoutInfo
import com.huawei.hms.image.vision.bean.ResultCode
import com.huawei.secure.android.common.util.LogsUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * The type SmartLayout activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
class SmartLayoutActivity : AppCompatActivity(), View.OnClickListener {
    private var context: Context? = null
    private var show_image_view: RelativeLayout? = null
    private var btn_submit: Button? = null
    private var et_title: EditText? = null
    private var et_copyRight: EditText? = null
    private var et_description: EditText? = null
    private var et_anchor: EditText? = null
    private var et_info: EditText? = null
    private var btn_image: Button? = null
    var imageVisionLayoutAPI: ImageVisionImpl? = null
    private var iv: ImageView? = null
    private var imageBitmap: Bitmap? = null
    private var tv: TextView? = null
    private var tv2: TextView? = null
    private var token: String? = null
    var img_btn: ImageView? = null
    private var btn_init: Button? = null
    private var initCodeState = -2
    private var stopCodeState = -2
    private var width = 2222
    private var height = 3333
    private var btn_stop: Button? = null
    private val client_id = "102216043"
    private val client_secret =
        "e2cd4510c9f52b17d0e23ac0e2e46b84ac0732eb59a7a235a1e0f7a8e4feb80f"
    private val string =
        "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}"
    private var authJson: JSONObject? = null
    private val requestJson = JSONObject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_layout)
        iv = findViewById(R.id.iv)
        tv = findViewById(R.id.tv)
        tv2 = findViewById(R.id.tv2)
        val manager = windowManager
        val metrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(metrics)
        width = metrics.widthPixels
        height = metrics.heightPixels
        context = this
        btn_submit = findViewById(R.id.btn_submit)
        btn_image = findViewById(R.id.btn_image)
        btn_stop = findViewById(R.id.btn_stop)
        btn_init = findViewById(R.id.btn_init)
        img_btn = findViewById(R.id.cloud_img_btn)
        show_image_view = findViewById(R.id.cloud_show_image_view)
        et_title = findViewById(R.id.et_title)
        et_info = findViewById(R.id.et_info)
        et_description = findViewById(R.id.et_description)
        et_copyRight = findViewById(R.id.et_copyRight)
        et_anchor = findViewById(R.id.et_anchor)
        btn_submit?.setOnClickListener(this)
        btn_image?.setOnClickListener(this)
        btn_init?.setOnClickListener(this)
        btn_stop?.setOnClickListener(this)
    }

    /**
     * Process the obtained image.
     */
    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    801 -> try {
                        imageBitmap =
                            Utility.getBitmapFromUri(data, this)
                        iv!!.setImageBitmap(imageBitmap)
                    } catch (e: Exception) {
                        LogsUtil.e(TAG, "Exception: " + e.message)
                    }
                }
            }
        }
    }

    private fun layoutAdd(
        title: String,
        info: String,
        description: String,
        copyRight: String,
        anchor: String
    ) {
        if (imageBitmap == null) {
            tv2!!.text = "resultCode:" + ResultCode.FILTER_INTERFACE_REQUEST_PARAMETER_ERROR
            return
        }
        val reBitmap = imageBitmap
        Thread(Runnable {
            try {
                token = Utility.getToken(
                    context,
                    client_id,
                    client_secret
                )
                authJson!!.put("token", token)
                createRequestJson(title, description, copyRight, anchor, info)
                val imageLayoutInfo = imageVisionLayoutAPI!!.analyzeImageLayout(
                    requestJson,
                    reBitmap
                )
                runOnUiThread {
                    iv!!.setImageBitmap(null)
                    val resizebitmap = Bitmap.createScaledBitmap(
                        Utility.cutSmartLayoutImage(
                            reBitmap
                        )!!,
                        width, height, false
                    )
                    img_btn!!.background = BitmapDrawable(resources, resizebitmap)
                    setView(imageLayoutInfo)
                    viewSaveToImage(show_image_view)
                    tv!!.text = "response:" + imageLayoutInfo.response.toString()
                    tv2!!.text = "resultCode:" + imageLayoutInfo.resultCode
                }
            } catch (e: JSONException) {
                LogsUtil.e(TAG, "JSONException" + e.message)
            }
        }).start()
    }

    private fun createRequestJson(
        title: String,
        description: String,
        copyRight: String,
        anchor: String,
        info: String?
    ) {
        try {
            val taskJson = JSONObject()
            taskJson.put("title", title)
            taskJson.put("imageUrl", "imageUrl")
            taskJson.put("description", description)
            taskJson.put("copyRight", copyRight)
            taskJson.put("isNeedMask", false)
            taskJson.put("anchor", anchor)
            val jsonArray = JSONArray()
            if (info != null && info.length > 0) {
                val split = info.split(",").toTypedArray()
                for (i in split.indices) {
                    jsonArray.put(split[i])
                }
            } else {
                jsonArray.put("info8")
            }
            taskJson.put("styleList", jsonArray)
            requestJson.put("requestId", "")
            requestJson.put("taskJson", taskJson)
            requestJson.put("authJson", authJson)
        } catch (e: JSONException) {
            LogsUtil.e(TAG, e.message)
        }
    }

    private fun setView(imageLayoutInfo: ImageLayoutInfo) {
        try {
            if (imageLayoutInfo.viewGroup != null) {
                if (imageLayoutInfo.maskView != null) {
                    show_image_view!!.addView(imageLayoutInfo.maskView)
                }
                imageLayoutInfo.viewGroup.x = imageLayoutInfo.response.getInt("locationX").toFloat()
                imageLayoutInfo.viewGroup.y = imageLayoutInfo.response.getInt("locationY").toFloat()
                show_image_view!!.addView(imageLayoutInfo.viewGroup)
                imageLayoutInfo.viewGroup
                    .measure(
                        View.MeasureSpec.makeMeasureSpec(
                            0,
                            View.MeasureSpec.UNSPECIFIED
                        ),
                        View.MeasureSpec.makeMeasureSpec(
                            0,
                            View.MeasureSpec.UNSPECIFIED
                        )
                    )
                imageLayoutInfo.viewGroup
                    .layout(
                        0, 0, imageLayoutInfo.viewGroup.measuredWidth,
                        imageLayoutInfo.viewGroup.measuredHeight
                    )
            }
        } catch (e: JSONException) {
            LogsUtil.e(TAG, e.message)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_submit -> {
                if ((initCodeState != 0) or (stopCodeState == 0)) {
                    tv2!!.text =
                        "The service has not been initialized. Please initialize the service before calling it."
                    return
                }
                val title = et_title!!.text.toString()
                val description = et_description!!.text.toString()
                val copyRight = et_copyRight!!.text.toString()
                val anchor = et_anchor!!.text.toString()
                val info = et_info!!.text.toString()
                layoutAdd(title, info, description, copyRight, anchor)
            }
            R.id.btn_image -> Utility.getByAlbum(this)
            R.id.btn_init -> initApi(context)
            R.id.btn_stop -> stopFilter()
        }
    }

    private fun stopFilter() {
        if (null != imageVisionLayoutAPI) {
            val stopCode = imageVisionLayoutAPI!!.stop()
            tv2!!.text = "stopCode:$stopCode"
            iv!!.setImageBitmap(null)
            imageBitmap = null
            stopCodeState = stopCode
            tv!!.text = ""
            imageVisionLayoutAPI = null
        } else {
            tv2!!.text = "The service has not been enabled."
            stopCodeState = 0
        }
    }

    private fun initApi(context: Context?) {
        imageVisionLayoutAPI = ImageVision.getInstance(this)
        imageVisionLayoutAPI?.setVisionCallBack(object : VisionCallBack {
            override fun onSuccess(successCode: Int) {
                val initCode = imageVisionLayoutAPI?.init(context, authJson)
                tv2!!.text = "initCode:$initCode"
                if (initCode != null) {
                    initCodeState = initCode
                }
                stopCodeState = -2
            }

            override fun onFailure(errorCode: Int) {
                LogsUtil.e(
                    TAG,
                    "getImageVisionAPI failure, errorCode = $errorCode"
                )
                tv2!!.text = "initFailed"
            }
        })
    }

    private fun viewSaveToImage(view: View?) {
        view!!.isDrawingCacheEnabled = true
        view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        view.drawingCacheBackgroundColor = Color.WHITE
        val cachebmp = loadBitmapFromView(view)
        val fos: FileOutputStream
        var imagePath = ""
        try {
            val isHasSDCard =
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            if (isHasSDCard) {
                val sdRoot = Environment.getExternalStorageDirectory()
                val file = File(
                    sdRoot,
                    Calendar.getInstance().timeInMillis.toString() + ".png"
                )
                fos = FileOutputStream(file)
                imagePath = file.absolutePath
            } else {
                throw Exception("create failed!")
            }
            cachebmp.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        view.destroyDrawingCache()
    }

    private fun loadBitmapFromView(v: View?): Bitmap {
        v!!.measure(
            View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            ),
            View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            )
        )
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        val w = v.width
        val h = v.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)
        v.layout(0, 0, w, h)
        v.draw(c)
        return bmp
    }

    companion object {
        const val TAG = "SmartLayoutActivity"
    }

    init {
        try {
            authJson = JSONObject(string)
        } catch (e: JSONException) {
            LogsUtil.e(TAG, "layout exp" + e.message)
        }
    }
}