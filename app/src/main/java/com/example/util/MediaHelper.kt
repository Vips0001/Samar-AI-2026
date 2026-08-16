package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object MediaHelper {

    fun uriToBase64(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return null
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            Pair(mimeType, base64)
        } catch (e: Exception) {
            null
        }
    }

    fun saveBase64ImageToCache(context: Context, base64Data: String, prefix: String = "gen_img"): File? {
        return try {
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { it.write(decodedBytes) }
            file
        } catch (e: Exception) {
            null
        }
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, prefix: String = "avatar"): File? {
        return try {
            val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "attachment"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex)
                    }
                }
            }
        } catch (_: Exception) {}
        return name
    }
}
