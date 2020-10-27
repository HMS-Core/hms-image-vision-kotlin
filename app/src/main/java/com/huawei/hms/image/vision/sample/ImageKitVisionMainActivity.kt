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

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.huawei.hms.image.vision.sample.ImageKitVisionMainActivity
import com.huawei.secure.android.common.intent.SafeIntent
import com.huawei.secure.android.common.util.LogsUtil
import java.io.File
import java.util.*

/**
 * The  ImageKitVision activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
class ImageKitVisionMainActivity : AppCompatActivity() {
    private var btn_filter: Button? = null
    private var btn_themetag: Button? = null
    private var btn_smartlayout: Button? = null
    private var btn_sticker: Button? = null
    private var btn_crop: Button? = null
    private val context: Context? = null
    var mPermissionList: MutableList<String> =
        ArrayList()
    var permissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val mRequestCode = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_filter = findViewById(R.id.btn_filter)
        btn_themetag = findViewById(R.id.btn_themetag)
        btn_smartlayout = findViewById(R.id.btn_smartlayout)
        btn_sticker = findViewById(R.id.btn_sticker)
        btn_crop = findViewById(R.id.btn_crop)
        btn_filter?.setOnClickListener(View.OnClickListener {
            val intent1 =
                Intent(this@ImageKitVisionMainActivity, FilterActivity::class.java)
            startActivity(intent1)
        })
        btn_smartlayout?.setOnClickListener(View.OnClickListener {
            val intent1 = Intent(
                this@ImageKitVisionMainActivity,
                SmartLayoutActivity::class.java
            )
            startActivity(intent1)
        })
        btn_themetag?.setOnClickListener(View.OnClickListener {
            val intent1 = Intent(
                this@ImageKitVisionMainActivity,
                ThemeTagActivity::class.java
            )
            startActivity(intent1)
        })
        btn_sticker?.setOnClickListener(View.OnClickListener {
            val intent1 =
                Intent(this@ImageKitVisionMainActivity, StickerActivity::class.java)
            startActivity(intent1)
        })
        btn_crop?.setOnClickListener(View.OnClickListener {
            getByAlbum(
                this@ImageKitVisionMainActivity,
                GET_BY_CROP
            )
        })
        if (Build.VERSION.SDK_INT >= 23) {
            initPermission()
        }
    }

    @SuppressLint("WrongConstant")
    private fun initPermission() {
        // Clear the permissions that fail the verification.
        mPermissionList.clear()
        //Check whether the required permissions are granted.
        for (i in permissions.indices) {
            if (PermissionChecker.checkSelfPermission(this, permissions[i])
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Add permissions that have not been granted.
                mPermissionList.add(permissions[i])
            }
        }
        //Apply for permissions.
        if (mPermissionList.size > 0) { //The permission has not been granted. Please apply for the permission.
            ActivityCompat.requestPermissions(this, permissions, mRequestCode)
        }
    }

    /**
     * Process the obtained image.
     */
    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (null != data) {
            super.onActivityResult(requestCode, resultCode, data)
            try {
                if (resultCode == Activity.RESULT_OK) {
                    val uri: Uri?
                    when (requestCode) {
                        GET_BY_CROP -> {
                            val intent: Intent =
                                SafeIntent(data)
                            uri = intent.data
                            val intent4 = Intent(
                                this@ImageKitVisionMainActivity,
                                CropImageActivity::class.java
                            )
                            intent4.putExtra("uri", uri.toString())
                            startActivity(intent4)
                        }
                    }
                }
            } catch (e: Exception) {
                LogsUtil.i("onActivityResult", "Exception")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            0 -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val photoURI = FileProvider.getUriForFile(
                        this@ImageKitVisionMainActivity,
                        this@ImageKitVisionMainActivity.applicationContext
                            .packageName
                                + ".fileprovider",
                        File(context!!.filesDir, "temp.jpg")
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(
                        cameraIntent,
                        GET_BY_CAMERA
                    )
                } else {
                    Toast.makeText(
                        this@ImageKitVisionMainActivity,
                        "No permission.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                return
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
        private const val GET_BY_CROP = 804
        private const val GET_BY_ALBUM1 = 801
        private const val GET_BY_CAMERA = 805

        /**
         * Obtain pictures from the album
         */
        fun getByAlbum(act: Activity, type: Int) {
            val getAlbum = Intent(Intent.ACTION_GET_CONTENT)
            getAlbum.type = "image/*"
            act.startActivityForResult(getAlbum, type)
        }
    }
}