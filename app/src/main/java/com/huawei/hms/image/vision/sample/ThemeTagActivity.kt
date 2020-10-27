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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVision.VisionCallBack
import com.huawei.hms.image.vision.ImageVisionImpl
import com.huawei.hms.image.vision.sample.ThemeTagActivity
import com.huawei.secure.android.common.util.LogsUtil
import org.json.JSONException
import org.json.JSONObject

/**
 * The type ThemeTag activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
class ThemeTagActivity : AppCompatActivity(), View.OnClickListener {
    private var context: Context? = null
    private var btn_result: Button? = null
    private var btn_image: Button? = null
    private var imageBitmap: Bitmap? = null
    private var token: String? = null
    var imageVisionTagAPI: ImageVisionImpl? = null
    private var iv: ImageView? = null
    private var tv: TextView? = null
    private var tv2: TextView? = null
    private var rbEn: RadioButton? = null
    private var rbCn: RadioButton? = null
    private var initCodeState = -2
    private var stopCodeState = -2
    private var rgTag: RadioGroup? = null
    private var btn_init: Button? = null
    private var btn_stop: Button? = null
    private val client_id = "102216043"
    private val client_secret =
        "e2cd4510c9f52b17d0e23ac0e2e46b84ac0732eb59a7a235a1e0f7a8e4feb80f"
    private val string =
        "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}"
    private var authJson: JSONObject? = null
    private var language = "cn"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_tag)
        iv = findViewById(R.id.iv)
        tv = findViewById(R.id.tv)
        context = this
        tv2 = findViewById(R.id.tv2)
        btn_result = findViewById(R.id.btn_result)
        btn_image = findViewById(R.id.btn_image)
        rbEn = findViewById(R.id.rb_en)
        rbCn = findViewById(R.id.rb_cn)
        btn_stop = findViewById(R.id.btn_stop)
        btn_init = findViewById(R.id.btn_init)
        rgTag = findViewById(R.id.rg_tag)
        rgTag?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            val radioButton =
                radioGroup.findViewById<RadioButton>(i)
            language = radioButton.text.toString()
            Toast.makeText(this@ThemeTagActivity, radioButton.text, Toast.LENGTH_SHORT)
                .show()
        })
        btn_init?.setOnClickListener(this)
        btn_stop?.setOnClickListener(this)
        btn_result?.setOnClickListener(this)
        btn_image?.setOnClickListener(this)
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == 801) {
                    try {
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

    private fun stopFilter() {
        if (null != imageVisionTagAPI) {
            val stopCode = imageVisionTagAPI!!.stop()
            tv2!!.text = "stopCode:$stopCode"
            iv!!.setImageBitmap(null)
            tv!!.text = ""
            imageBitmap = null
            stopCodeState = stopCode
            imageVisionTagAPI = null
        } else {
            tv2!!.text = "The service has not been enabled."
            stopCodeState = 0
        }
    }

    private fun initTag(context: Context?) {
        imageVisionTagAPI = ImageVision.getInstance(this)
        imageVisionTagAPI?.setVisionCallBack(object : VisionCallBack {
            override fun onSuccess(successCode: Int) {
                val initCode = imageVisionTagAPI?.init(context, authJson)
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

    private fun getThemeTag(language: String) {
        val requestJson = JSONObject()
        val taskJson = JSONObject()
        val tagBitmap = imageBitmap
        Thread(Runnable {
            try {
                token = context?.let {
                    Utility.getToken(
                        it,
                        client_id,
                        client_secret
                    )
                }
                authJson!!.put("token", token)
                taskJson.put("language", language)
                requestJson.put("requestId", "requestId")
                requestJson.put("taskJson", taskJson)
                requestJson.put("authJson", authJson)
                val result = imageVisionTagAPI!!.analyzeImageThemeTag(
                    requestJson, tagBitmap
                )
                iv!!.post {
                    tv!!.text = "response:" + result.response.toString()
                    tv2!!.text = "resultCode:" + result.resultCode
                }
            } catch (e: JSONException) {
                LogsUtil.e(TAG, "JSONException" + e.message)
            }
        }).start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_result -> {
                if ((initCodeState != 0) or (stopCodeState == 0)) {
                    tv2!!.text =
                        "The service has not been initialized. Please initialize the service before calling it."
                    return
                }
                getThemeTag(language)
            }
            R.id.btn_image -> Utility.getByAlbum(this)
            R.id.btn_init -> initTag(context)
            R.id.btn_stop -> stopFilter()
        }
    }

    companion object {
        const val TAG = "ThemeTagActivity"
    }

    init {
        try {
            authJson = JSONObject(string)
        } catch (e: JSONException) {
            LogsUtil.e(TAG, "tag exp" + e.message)
        }
    }
}