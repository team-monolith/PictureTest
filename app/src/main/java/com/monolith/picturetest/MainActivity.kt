package com.monolith.picturetest

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    var image: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var pictureButton: Button = findViewById(R.id.btnLoad)

        var clearButton: Button = findViewById(R.id.btnClear)

        var convertButton: Button = findViewById(R.id.btnConvert)

        var reconvertButton: Button = findViewById(R.id.btnReconvert)

        var connectButton: Button = findViewById(R.id.btnConnect)

        //ボタンが押されたらギャラリーを開く
        pictureButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, READ_REQUEST_CODE)
        }

        clearButton.setOnClickListener {
            ImageClear()
        }

        convertButton.setOnClickListener {
            Convert()
        }

        reconvertButton.setOnClickListener {
            ReConvert()
        }

        connectButton.setOnClickListener {
            Connect()
        }

    }

    //READ_REQUEST_CODEの定義
    companion object {
        private const val READ_REQUEST_CODE: Int = 42
    }

    //写真が選択された後の動き
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            READ_REQUEST_CODE -> {
                try {
                    resultData?.data?.also { uri ->
                        val inputStream = contentResolver?.openInputStream(uri)
                        image = Bitmap.createScaledBitmap(
                            BitmapFactory.decodeStream(inputStream),
                            250,
                            250,
                            true
                        )
                        val imageView = findViewById<ImageView>(R.id.imageView)
                        imageView.setImageBitmap(image)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    //画面上の画像データを削除
    fun ImageClear() {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageIcon(null)
    }

    //画面上の画像を保存しtxtデータに変換
    fun Convert(){
        /*val file = File("$filesDir", "pictureBuffer.png")
        FileOutputStream(file).use { fileOutputStream ->
            image!!.compress(Bitmap.CompressFormat.PNG, 50, fileOutputStream)
            fileOutputStream.flush()
        }
        val newFile = File("$filesDir", "pictureBuffer.txt")
        file.renameTo(newFile)*/

        val baos = ByteArrayOutputStream()
        image!!.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        FileWrite(Base64.encodeToString(b, Base64.NO_WRAP))
    }

    //保存されたtxtデータをpngに変換し画面に表示
    fun ReConvert() {
        val file = File("$filesDir", "pictureBuffer.txt")
        val newFile = File("$filesDir", "pictureBuffer.png")
        file.renameTo(newFile)
        val buf: Bitmap = BitmapFactory.decodeFile("$filesDir/pictureBuffer.png")
        Handler().post {
            findViewById<ImageView>(R.id.imageView).setImageBitmap(buf)
        }
    }

    fun setStringImage(data: String) {
        val decodedByte: ByteArray = Base64.decode(data, 0)
        val buf= BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        findViewById<ImageView>(R.id.imageView).setImageBitmap(buf)
    }

    fun Connect() {
        val POSTDATA = HashMap<String, String>()
        POSTDATA.put("", "")

        "https://compass-user.work/s.php".httpPost(POSTDATA.toList())
            .response { _, response, result ->
                when (result) {
                    is Result.Success -> {
                        setStringImage(String(response.data))
                    }
                    is Result.Failure -> {
                    }
                }
            }
    }

    fun FileWrite(str: String) {
        var strbuf=str.replace("<br />","")
        val file = File("$filesDir/", "pictureBuffer.txt")
        file.writeText(strbuf)
    }
}