package com.meharenterprises.originconnect.ui.auth
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.remote.ApiService
import com.meharenterprises.originconnect.data.remote.UpdateProfileRequest
import com.meharenterprises.originconnect.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSetupActivity : AppCompatActivity() {
    @Inject lateinit var session: SessionManager
    @Inject lateinit var api: ApiService
    private var selectedPhotoUri: Uri? = null
    companion object { private const val PICK_IMAGE = 100 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_profile_setup)
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
        val tvAddPhoto = findViewById<TextView>(R.id.tvAddPhoto)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etAbout = findViewById<TextInputEditText>(R.id.etAbout)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val progress = findViewById<ProgressBar>(R.id.progress)

        imgAvatar.setOnClickListener { openGallery() }
        tvAddPhoto.setOnClickListener { openGallery() }

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val ok = s.toString().trim().length >= 2
                btnContinue.isEnabled = ok
                btnContinue.background = if (ok) getDrawable(R.drawable.bg_btn_primary) else getDrawable(R.drawable.bg_btn_disabled)
                btnContinue.setTextColor(if (ok) getColor(R.color.oc_text_primary) else getColor(R.color.oc_text_secondary))
            }
        })

        btnContinue.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.length < 2) { Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            progress.visibility = View.VISIBLE; btnContinue.isEnabled = false
            lifecycleScope.launch {
                try {
                    val auth = session.getAuthHeader()
                    if (selectedPhotoUri != null) {
                        try {
                            val stream = contentResolver.openInputStream(selectedPhotoUri!!)
                            val file = File(cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                            file.outputStream().use { stream?.copyTo(it) }
                            val body = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                            api.uploadMedia(body, auth)
                        } catch (_: Exception) {}
                    }
                    api.updateProfile(UpdateProfileRequest(displayName = name, about = etAbout.text.toString().trim().ifEmpty { null }), auth)
                    startActivity(Intent(this@ProfileSetupActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } catch (e: Exception) {
                    progress.visibility = View.GONE; btnContinue.isEnabled = true
                    Toast.makeText(this@ProfileSetupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGallery() {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE)
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = data?.data
            if (selectedPhotoUri != null) {
                findViewById<ImageView>(R.id.imgAvatar).setImageURI(selectedPhotoUri)
                findViewById<TextView>(R.id.tvAddPhoto).text = "Change photo"
            }
        }
    }
}
