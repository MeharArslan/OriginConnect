package com.meharenterprises.originconnect.ui.profile
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.remote.ApiService
import com.meharenterprises.originconnect.data.remote.UpdateProfileRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    @Inject lateinit var session: SessionManager
    @Inject lateinit var api: ApiService
    private var photoUri: Uri? = null
    companion object { private const val PICK_IMAGE = 101 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_profile)

        val tb = findViewById<Toolbar>(R.id.profileToolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"

        val imgAvatar  = findViewById<ImageView>(R.id.imgProfileAvatar)
        val tvPhone    = findViewById<TextView>(R.id.tvProfilePhone)
        val etName     = findViewById<TextInputEditText>(R.id.etProfileName)
        val etAbout    = findViewById<TextInputEditText>(R.id.etProfileAbout)
        val btnSave    = findViewById<Button>(R.id.btnSaveProfile)
        val progress   = findViewById<ProgressBar>(R.id.profileProgress)
        val spinnerAbout = findViewById<Spinner>(R.id.spinnerAbout)

        // About predefined list
        val aboutOptions = listOf(
            "Available", "Busy", "At work", "At school",
            "Battery about to die", "Can't talk, WhatsApp only",
            "In a meeting", "At the gym", "Sleeping", "Custom..."
        )
        spinnerAbout.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, aboutOptions)

        spinnerAbout.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos < aboutOptions.size - 1) {
                    etAbout.setText(aboutOptions[pos])
                    etAbout.isEnabled = false
                } else {
                    etAbout.isEnabled = true
                    etAbout.requestFocus()
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Load current data
        lifecycleScope.launch {
            tvPhone.text = session.getUserPhone() ?: ""
            etName.setText(session.getUserName() ?: "")
        }

        imgAvatar.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE)
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.length < 2) { Toast.makeText(this, "Name too short", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val about = etAbout.text.toString().trim()
            progress.visibility = View.VISIBLE
            btnSave.isEnabled = false
            lifecycleScope.launch {
                try {
                    val auth = session.getAuthHeader()
                    if (photoUri != null) {
                        try {
                            val stream = contentResolver.openInputStream(photoUri!!)
                            val file = File(cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                            file.outputStream().use { stream?.copyTo(it) }
                            val body = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                            api.uploadMedia(body, auth)
                        } catch (e: Exception) { /* continue */ }
                    }
                    api.updateProfile(UpdateProfileRequest(displayName = name, about = about.ifEmpty { null }), auth)
                    session.saveSession(
                        session.getAccessToken() ?: "",
                        session.getRefreshToken() ?: "",
                        session.getUserId() ?: "",
                        session.getUserPhone() ?: "",
                        name,
                        session.getUserPhoto()
                    )
                    Toast.makeText(this@ProfileActivity, "Profile saved", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    progress.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        }
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data
            if (photoUri != null) findViewById<ImageView>(R.id.imgProfileAvatar).setImageURI(photoUri)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
