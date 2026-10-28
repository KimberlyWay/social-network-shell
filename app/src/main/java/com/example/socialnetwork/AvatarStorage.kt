package com.example.socialnetwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.IOException

// Копируем выбранное изображение внутрь приложения: доступ к галерее после выбора не нужен.
class AvatarStorage(private val context: Context) {
    private val avatarFile get() = File(context.filesDir, "profile_avatar.jpg")

    fun load(): Bitmap? = if (avatarFile.exists()) BitmapFactory.decodeFile(avatarFile.path) else null

    fun save(uri: Uri) {
        val source = File.createTempFile("avatar_source", ".tmp", context.cacheDir)
        val result = File.createTempFile("avatar_result", ".tmp", context.filesDir)
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Image is unavailable")
            input.use { stream ->
                source.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = stream.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > 20 * 1024 * 1024) throw IOException("Image is too large")
                        output.write(buffer, 0, count)
                    }
                }
            }
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(source.path, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) throw IOException("Unsupported image")
            var sample = 1
            while (options.outWidth / sample > 512 || options.outHeight / sample > 512) sample *= 2
            val decoded = BitmapFactory.decodeFile(source.path,
                BitmapFactory.Options().apply { inSampleSize = sample })
                ?: throw IOException("Cannot decode image")
            val bitmap = orient(decoded, source)
            val side = minOf(bitmap.width, bitmap.height)
            val square = Bitmap.createBitmap(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2, side, side)
            val scaled = Bitmap.createScaledBitmap(square, 256, 256, true)
            result.outputStream().use {
                if (!scaled.compress(Bitmap.CompressFormat.JPEG, 85, it)) throw IOException("Cannot save image")
            }
            if (!result.renameTo(avatarFile)) throw IOException("Cannot replace avatar")
        } finally {
            source.delete()
            result.delete()
        }
    }

    private fun orient(bitmap: Bitmap, file: File): Bitmap {
        val orientation = try {
            ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: IOException) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        }
        return if (matrix.isIdentity) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
