package com.brooks.cameraalbumtest

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val takePhoto = 1
    lateinit var imageUri: Uri
    lateinit var outputImage: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        takePhotoBtn.setOnClickListener {
            // 创建File对象，用于存储拍照后的图片
            outputImage = File(externalCacheDir, "output_image.jpg")
            if (outputImage.exists()) {
                outputImage.delete()
            }
            outputImage.createNewFile()
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "com.brooks.cameraalbumtest.fileprovider", outputImage)
            } else {
                Uri.fromFile(outputImage)
            }
            // 启动相机程序

            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            setResult(takePhoto, intent)
//            startActivityForResult(intent, takePhoto)

//            //跳转方式
            startActivity.launch(intent)
        }
    }

    val startActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        Log.d("resultcode的值", ": ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
            imageView.setImageBitmap(rotateIfRequired(bitmap))
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when(resultCode) {
//            takePhoto -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    // 将拍摄的照片显示出来
//                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
//                    imageView.setImageBitmap(rotateIfRequired(bitmap))
//                }
//            }
//        }
//    }

    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        Log.d("图片方向", "rotateIfRequired: $exif")
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()  // 将不再需要的Bitmap对象回收
        return rotatedBitmap
    }
}