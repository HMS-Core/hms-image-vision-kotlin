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
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVision.VisionCallBack
import com.huawei.hms.image.vision.ImageVisionImpl
import com.huawei.secure.android.common.util.LogsUtil
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * The type Filter activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
class FilterActivity : AppCompatActivity(), View.OnClickListener {
    var executorService =
        Executors.newFixedThreadPool(1)
    private var btn_submit: Button? = null
    private val picPath: String? = null
    private var btn_init: Button? = null
    private var btn_picture: Button? = null
    private var btn_stop: Button? = null
    private var btn_filter: EditText? = null
    private var btn_compress: EditText? = null
    private var btn_intensity: EditText? = null
    private var iv: ImageView? = null
    private var tv: TextView? = null
    private var tv2: TextView? = null
    private val context: Context? = null
    private var bitmap: Bitmap? = null
    private var initCodeState = -2
    private var stopCodeState = -2
    var imageVisionFilterAPI: ImageVisionImpl? = null
    var string =
        "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}"
    private var authJson: JSONObject? = null

    /**
     * The Image vision api.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        iv = findViewById(R.id.iv)
        tv = findViewById(R.id.tv)
        tv2 = findViewById(R.id.tv2)
        btn_filter = findViewById(R.id.btn_filter)
        btn_init = findViewById(R.id.btn_init)
        btn_picture = findViewById(R.id.btn_picture)
        btn_intensity = findViewById(R.id.btn_intensity)
        btn_compress = findViewById(R.id.btn_compress)
        btn_submit = findViewById(R.id.btn_submit)
        btn_stop = findViewById(R.id.btn_stop)
        btn_submit?.setOnClickListener(this)
        btn_init?.setOnClickListener(this)
        btn_stop?.setOnClickListener(this)
        btn_picture?.setOnClickListener(this)
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
        if (null != data) {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    801 -> try {
                        bitmap =
                            Utility.getBitmapFromUri(data, this)
                        iv!!.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        LogsUtil.e(TAG, "Exception: " + e.message)
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_submit -> {
                val filterType = btn_filter!!.text.toString()
                val intensity = btn_intensity!!.text.toString()
                val compress = btn_compress!!.text.toString()
                if ((initCodeState != 0 )or (stopCodeState == 0)) {
                    tv2!!.text =
                        "The service has not been initialized. Please initialize the service before calling it."
                    return
                }
                startFilter(filterType, intensity, compress, authJson)
            }
            R.id.btn_init -> initFilter(context)
            R.id.btn_picture -> Utility.getByAlbum(this)
            R.id.btn_stop -> stopFilter()
        }
    }

    private fun stopFilter() {
        if (null != imageVisionFilterAPI) {
            val stopCode = imageVisionFilterAPI!!.stop()
            tv2!!.text = "stopCode:$stopCode"
            iv!!.setImageBitmap(null)
            bitmap = null
            tv!!.text = ""
            imageVisionFilterAPI = null
            stopCodeState = stopCode
        } else {
            tv2!!.text = "The service has not been enabled."
            stopCodeState = 0
        }
    }

    private fun initFilter(context: Context?) {
        imageVisionFilterAPI = ImageVision.getInstance(this)
        imageVisionFilterAPI?.setVisionCallBack(object : VisionCallBack {
            override fun onSuccess(successCode: Int) {
                val initCode = imageVisionFilterAPI?.init(context, authJson)
                if (initCode != null) {
                    initCodeState = initCode
                }
                stopCodeState = -2
                tv2!!.text = "initCode = $initCode"
            }

            override fun onFailure(errorCode: Int) {
                tv2!!.text = "Failed"
                LogsUtil.e(
                    TAG,
                    "ImageVisionAPI fail, errorCode: $errorCode"
                )
            }
        })
    }

    private fun startFilter(
        filterType: String, intensity: String, compress: String,
        authJson: JSONObject?
    ) {
        val runnable = Runnable {
            val jsonObject = JSONObject()
            val taskJson = JSONObject()
            try {
                taskJson.put("intensity", intensity)
                taskJson.put("filterType", filterType)
                taskJson.put("compressRate", compress)
                jsonObject.put("requestId", "1")
                jsonObject.put("taskJson", taskJson)
                jsonObject.put("authJson", authJson)
                val visionResult = imageVisionFilterAPI!!.getColorFilter(
                    jsonObject,
                    bitmap
                )
                iv!!.post {
                    val image = visionResult.image
                    iv!!.setImageBitmap(image)
                    tv!!.text = visionResult.response.toString() + "resultCode:" + visionResult
                        .resultCode
                }
            } catch (e: JSONException) {
                LogsUtil.e(TAG, "JSONException: " + e.message)
            }
        }
        executorService.execute(runnable)
    }

    companion object {
        const val TAG = "FilterActivity"
    }

    init {
        try {
            authJson = JSONObject(string)
        } catch (e: JSONException) {
            LogsUtil.e(TAG, "filter exp" + e.message)
        }
    }
}