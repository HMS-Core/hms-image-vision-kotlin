/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2019. All rights reserved.
 */
package com.huawei.hms.image.vision.sample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.huawei.secure.android.common.ssl.SSFCompatiableSystemCA
import com.huawei.secure.android.common.ssl.hostname.StrictHostnameVerifier
import com.huawei.secure.android.common.util.IOUtil
import com.huawei.secure.android.common.util.LogsUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * The type Smart layout utility.
 *
 * @author huawei
 * @since 1.0.3.300
 */
object Utility {
    private const val TAG = "Utility"

    /**
     * Gets token.
     *
     * @param context the context
     * @param client_id the client id
     * @param client_secret the client secret
     * @return the token
     */
    fun getToken(
        context: Context?,
        client_id: String,
        client_secret: String
    ): String? {
        var token: String? = ""
        try {
            val body =
                "grant_type=client_credentials&client_id=$client_id&client_secret=$client_secret"
            token = commonHttpsRequest(
                context,
                body,
                context!!.resources.getString(R.string.urlToken)
            )
            if (token != null && token.contains(" ")) {
                token = token.replace(" ".toRegex(), "+")
                token = URLEncoder.encode(token, "UTF-8")
            }
        } catch (e: UnsupportedEncodingException) {
            LogsUtil.e(TAG, e.message)
        }
        return token
    }

    /**
     * Common https request string.
     *
     * @param context the context
     * @param body the body
     * @param urlStr the url str
     * @return the string
     */
    fun commonHttpsRequest(
        context: Context?,
        body: String?,
        urlStr: String?
    ): String? {
        val connectTimeoutValue = 5000
        val readTimeoutValue = 5000
        try {
            val url = URL(urlStr)
            val connection =
                url.openConnection() as HttpsURLConnection
            val sf = SSFCompatiableSystemCA.getInstance(context)
            if (sf != null && sf is SSLSocketFactory) {
                connection.sslSocketFactory = sf
                connection.hostnameVerifier = StrictHostnameVerifier()
            }
            connection.connectTimeout = connectTimeoutValue
            connection.readTimeout = readTimeoutValue
            connection.doOutput = true
            connection.doInput = true
            connection.requestMethod = "POST"
            connection.useCaches = false
            connection.instanceFollowRedirects = true
            connection.useCaches = false
            connection.setRequestProperty("Charsert", "UTF-8")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.connect()
            var writer: BufferedWriter? = null
            try {
                writer = BufferedWriter(
                    OutputStreamWriter(
                        connection.outputStream,
                        "UTF-8"
                    )
                )
                writer.write(body)
            } catch (e: IOException) {
                LogsUtil.e(
                    TAG,
                    "IOException1: " + e.message
                )
            } finally {
                IOUtil.closeSecure(writer)
            }
            val sb = StringBuffer("")
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(
                    InputStreamReader(
                        connection.inputStream,
                        "UTF-8"
                    )
                )
                var lines: String?
                while (reader.readLine().also { lines = it } != null) {
                    lines = URLDecoder.decode(lines, "utf-8")
                    sb.append(lines)
                }
            } catch (e: IOException) {
                LogsUtil.e(
                    TAG,
                    "IOException2: " + e.message
                )
            } finally {
                IOUtil.closeSecure(reader)
                connection?.disconnect()
            }
            var accessToken = ""
            if (sb.toString().length > 0) {
                val jsonObject = JSONObject(sb.toString())
                accessToken = jsonObject.getString("access_token")
            }
            return accessToken
        } catch (e: MalformedURLException) {
            LogsUtil.e(
                TAG,
                "MalformedURLException: " + e.message
            )
        } catch (e: UnsupportedEncodingException) {
            LogsUtil.e(
                TAG,
                "UnsupportedEncodingException: " + e.message
            )
        } catch (e: IOException) {
            LogsUtil.e(TAG, "IOException3: " + e.message)
        } catch (e: CertificateException) {
            LogsUtil.e(
                TAG,
                "CertificateException: " + e.message
            )
        } catch (e: NoSuchAlgorithmException) {
            LogsUtil.e(
                TAG,
                "NoSuchAlgorithmException: " + e.message
            )
        } catch (e: KeyStoreException) {
            LogsUtil.e(
                TAG,
                "KeyStoreException: " + e.message
            )
        } catch (e: KeyManagementException) {
            LogsUtil.e(
                TAG,
                "KeyManagementException: " + e.message
            )
        } catch (e: JSONException) {
            LogsUtil.e(
                TAG,
                "JSONException: " + e.message
            )
        }
        return null
    }

    /**
     * Gets by album.
     *
     * @param act the act
     */
    fun getByAlbum(act: Activity) {
        val getAlbum = Intent(Intent.ACTION_GET_CONTENT)
        val mimeTypes =
            arrayOf("image/jpeg", "image/png", "image/webp")
        getAlbum.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        getAlbum.type = "image/*"
        getAlbum.addCategory(Intent.CATEGORY_OPENABLE)
        act.startActivityForResult(getAlbum, 801)
    }

    /**
     * Gets bitmap from uri.
     *
     * @param intent the intent
     * @param context the context
     * @return the bitmap from uri
     */
    fun getBitmapFromUri(intent: Intent, context: Context): Bitmap? {
        val uri = intent.data
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: Exception) {
            LogsUtil.e(TAG, e.message)
            null
        }
    }

    /**
     * Gets bitmap from uri str.
     *
     * @param intent the intent
     * @param context the context
     * @return the bitmap from uri str
     */
    fun getBitmapFromUriStr(intent: Intent?, context: Context): Bitmap? {
        var picPath: String? = ""
        var uri: Uri? = null
        if (null != intent) {
            picPath = intent.getStringExtra("uri")
        }
        if (picPath != null) {
            uri = Uri.parse(picPath)
        }
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: Exception) {
            LogsUtil.e(TAG, e.message)
            null
        }
    }

    /**
     * Cut smart layout image bitmap.
     *
     * @param bitmap the bitmap
     * @return the bitmap
     */
    fun cutSmartLayoutImage(bitmap: Bitmap?): Bitmap? {
        val cutBitmap: Bitmap
        if (bitmap!!.height.toFloat() / bitmap.width.toFloat() == 16f / 9f) {
            return bitmap
        }
        cutBitmap = if (bitmap.width / 9 < bitmap.height / 16) {
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width / 9 * 9,
                bitmap.width / 9 * 16
            )
        } else {
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.height / 16 * 9,
                bitmap.height / 16 * 16
            )
        }
        return cutBitmap
    }

    fun copyAssetsFileToDirs(
        context: Context,
        oldPath: String,
        newPath: String
    ): Boolean {
        val inputStream: InputStream? = null
        val outputStream: FileOutputStream? = null
        try {
            val fileNames = context.assets.list(oldPath)
            if (fileNames!!.size > 0) {
                val file = File(newPath)
                file.mkdirs()
                for (fileName in fileNames) {
                    copyAssetsFileToDirs(
                        context,
                        "$oldPath/$fileName",
                        "$newPath/$fileName"
                    )
                }
            } else {
                val `is` = context.assets.open(oldPath)
                val fos = FileOutputStream(File(newPath))
                val buffer = ByteArray(1024)
                var byteCount = 0
                while (`is`.read(buffer).also { byteCount = it } != -1) {
                    fos.write(buffer, 0, byteCount)
                }
                fos.flush()
                `is`.close()
                fos.close()
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message)
            return false
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close()
                    } catch (e: IOException) {
                        Log.e(
                            TAG,
                            e.message
                        )
                    }
                }
            }
        }
        return true
    }
}