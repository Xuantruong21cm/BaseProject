package com.base.common.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.WindowInsets
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

//replace name folder
var SUB_FOLDER: String = "BaseDownloadFolder/"
var SUB_DOWNLOAD_FOLDER : String = "/BaseDownloadFolder"

fun getScreenWidth(context: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = context.windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun getScreenHeight(context: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = context.windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.height() - insets.bottom - insets.top
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        context.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

fun getDownloadedPath(): String {
    var folderPath = ""
    folderPath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + SUB_DOWNLOAD_FOLDER
    val folder = File(folderPath)
    if (!folder.exists()) {
        val wallpaperDirectory = File(folderPath)
        wallpaperDirectory.mkdirs()
    }
    return folderPath
}

fun getDownloadedPath(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor =
        context.contentResolver.query(uri, projection, null, null, null)
            ?: return null
    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    val s = cursor.getString(columnIndex)
    cursor.close()
    return s
}

fun saveDownloadedFileMediaStore(context: Context, displayName: String, extension :String): DataSave? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        var relativeLocation = Environment.DIRECTORY_DOWNLOADS
        if (!TextUtils.isEmpty(SUB_FOLDER)) {
            relativeLocation += File.separator + SUB_FOLDER
        }

        val contentValue = ContentValues()
        contentValue.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValue.put("is_pending", true)
        contentValue.put(MediaStore.MediaColumns.MIME_TYPE, extension)
        contentValue.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
        contentValue.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
        val resoler: ContentResolver = context.contentResolver
        var uri: Uri? = null
        try {
            val contentUri: Uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            uri = resoler.insert(contentUri, contentValue)

            uri?.let {
                val file = File(relativeLocation, displayName)
                val outputStream: OutputStream? = resoler.openOutputStream(it)
                outputStream?.let { output ->
                    return DataSave(output, file, uri, "", contentValue)
                } ?: kotlin.run {
                    return null
                }
            }
        } catch (e: Exception) {
            if (uri != null) {
                resoler.delete(uri, null, null)
            }
            e.printStackTrace()
        }
    } else {
        return try {
            val mPath = Environment.getExternalStorageDirectory().path + SUB_DOWNLOAD_FOLDER
            val pathFile = File(mPath)
            if (!pathFile.exists()) {
                pathFile.mkdirs()
            }
            val path = getDownloadedPath() + File.separator + "" + displayName
            val filename = File(path)
            DataSave(FileOutputStream(filename), filename, null, filename.path, null)
        } catch (e: Exception) {
            null
        }
    }
    return null
}

data class DataSave(
    var outputStream: OutputStream,
    var file: File,
    var uri: Uri?,
    var path: String? = "",
    var contentValues: ContentValues?
)