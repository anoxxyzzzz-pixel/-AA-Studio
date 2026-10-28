package com.webapk.converter
import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.graphics.Color
import android.widget.*
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity:Activity() {
    private lateinit var server:EditText; private lateinit var site:EditText; private lateinit var appName:EditText; private lateinit var packageId:EditText
    private lateinit var overlay:CheckBox; private lateinit var button:Button; private lateinit var status:TextView
    override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(22,20,22,24); setBackgroundColor(Color.rgb(245,247,251)) }
        root.addView(TextView(this).apply { text="Website to APK"; textSize=27f; setTextColor(Color.rgb(28,39,58)); setPadding(0,0,0,8) })
        root.addView(TextView(this).apply { text="Masukkan URL, atur nama aplikasi, lalu build APK melalui server."; textSize=14f; setTextColor(Color.DKGRAY) })
        server=addField(root,"Alamat build server","https://alamat-server-anda")
        site=addField(root,"URL website","https://example.com")
        appName=addField(root,"Nama aplikasi hasil","Aplikasi Saya")
        packageId=addField(root,"Package ID","com.contoh.aplikasi")
        overlay=CheckBox(this).apply { text="Sertakan fitur floating overlay"; isChecked=true; setTextColor(Color.DKGRAY) }; root.addView(overlay)
        button=Button(this).apply { text="Buat dan unduh APK"; setOnClickListener { build() } }; root.addView(button)
        status=TextView(this).apply { text="Perlu build server dengan Android SDK dan Gradle."; setTextColor(Color.DKGRAY); setPadding(0,14,0,0) }; root.addView(status)
        setContentView(ScrollView(this).apply { addView(root) })
    }
    private fun addField(root:LinearLayout,label:String,hintText:String):EditText {
        root.addView(TextView(this).apply { text=label; setTextColor(Color.rgb(45,55,70)); setPadding(0,12,0,2) })
        return EditText(this).apply { hint=hintText; setSingleLine(true); textSize=15f; setPadding(12,4,12,4); setBackgroundColor(Color.WHITE); root.addView(this) }
    }
    private fun build() {
        val base=server.text.toString().trim().trimEnd('/'); val url=site.text.toString().trim(); val name=appName.text.toString().trim(); val pkg=packageId.text.toString().trim()
        if (!(base.startsWith("http://")||base.startsWith("https://"))) { status.text="Alamat server harus memakai http:// atau https://"; return }
        if (!url.startsWith("https://")) { status.text="URL website harus memakai HTTPS"; return }
        if(name.isBlank()||pkg.isBlank()){status.text="Nama dan package ID wajib diisi";return}
        button.isEnabled=false; status.text="Membangun APK di server. Proses bisa beberapa menit…"
        thread {
            try {
                val c=URL("$base/build").openConnection() as HttpURLConnection
                c.requestMethod="POST"; c.connectTimeout=15000; c.readTimeout=900000; c.doOutput=true; c.setRequestProperty("Content-Type","application/json")
                val body=JSONObject().put("url",url).put("app_name",name).put("package_id",pkg).put("overlay",overlay.isChecked).toString()
                c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                if(c.responseCode !in 200..299) throw Exception(c.errorStream?.bufferedReader()?.readText()?.take(2000) ?: "HTTP ${c.responseCode}")
                val bytes=c.inputStream.use { it.readBytes() }; if(bytes.size<1000) throw Exception("Respons server bukan APK yang valid")
                val safe=name.replace(Regex("[^A-Za-z0-9_-]+"),"_").trim('_').ifBlank{"WebsiteApp"}; val filename="$safe-debug.apk"
                if(android.os.Build.VERSION.SDK_INT>=29) {
                    val cv=ContentValues().apply { put(MediaStore.Downloads.DISPLAY_NAME,filename); put(MediaStore.Downloads.MIME_TYPE,"application/vnd.android.package-archive"); put(MediaStore.Downloads.IS_PENDING,1) }
                    val uri=contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,cv) ?: throw Exception("Gagal membuat file Download")
                    contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: throw Exception("Gagal menyimpan APK")
                    cv.clear(); cv.put(MediaStore.Downloads.IS_PENDING,0); contentResolver.update(uri,cv,null,null)
                } else { val dir=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?:filesDir; File(dir,filename).writeBytes(bytes) }
                runOnUiThread { status.text="Berhasil mengunduh $filename ke folder Download."; Toast.makeText(this,"APK berhasil diunduh",Toast.LENGTH_LONG).show() }; c.disconnect()
            } catch(e:Exception) { runOnUiThread { status.text="Gagal: ${e.message}" } }
            finally { runOnUiThread { button.isEnabled=true } }
        }
    }
}
