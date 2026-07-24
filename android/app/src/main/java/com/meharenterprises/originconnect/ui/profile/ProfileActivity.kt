package com.meharenterprises.originconnect.ui.profile
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { photoUri = it; updateAvatarPreview(it) }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val file = File(cacheDir, "cam_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out) }
            photoUri = Uri.fromFile(file)
            updateAvatarPreview(photoUri!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_profile)

        val tb = findViewById<Toolbar>(R.id.profileToolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"

        val imgAvatar    = findViewById<ImageView>(R.id.imgProfileAvatar)
        val tvPhone      = findViewById<TextView>(R.id.tvProfilePhone)
        val etName       = findViewById<TextInputEditText>(R.id.etProfileName)
        val spinnerAbout = findViewById<Spinner>(R.id.spinnerAbout)
        val etAbout      = findViewById<TextInputEditText>(R.id.etProfileAbout)
        val btnSave      = findViewById<Button>(R.id.btnSaveProfile)
        val progress     = findViewById<ProgressBar>(R.id.profileProgress)
        val tvCamera     = findViewById<TextView>(R.id.tvCameraOption)
        val tvGallery    = findViewById<TextView>(R.id.tvGalleryOption)

        // About presets - exactly like WhatsApp
        val presets = listOf("Available", "Busy", "At work", "At school",
            "Battery about to die", "Can't talk, OriginConnect only",
            "In a meeting", "At the gym", "Sleeping", "Urgent calls only", "Custom")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, presets)
        spinnerAbout.adapter = spinnerAdapter

        spinnerAbout.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p: AdapterView<*>?) {}
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos < presets.size - 1) {
                    etAbout.setText(presets[pos])
                    etAbout.isEnabled = false
                } else {
                    etAbout.isEnabled = true
                    etAbout.text?.clear()
                    etAbout.requestFocus()
                }
            }
        }

        // Load current profile
        lifecycleScope.launch {
            tvPhone.text = session.getUserPhone() ?: ""
            val currentName = session.getUserName() ?: ""
            // Don't show phone number as name
            if (currentName.isNotEmpty() && !currentName.startsWith("+")) {
                etName.setText(currentName)
            }
        }

        tvCamera.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
                else -> cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
        tvGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        imgAvatar.setOnClickListener { tvGallery.performClick() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.length < 2) { Toast.makeText(this, "Name must be at least 2 characters", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val about = etAbout.text.toString().trim()
            progress.visibility = View.VISIBLE
            btnSave.isEnabled = false
            lifecycleScope.launch {
                try {
                    val auth = session.getAuthHeader()
                    // Upload photo
                    if (photoUri != null) {
                        try {
                            val stream = contentResolver.openInputStream(photoUri!!)
                            val file = File(cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                            file.outputStream().use { stream?.copyTo(it) }
                            val body = MultipartBody.Part.createFormData("file", file.name,
                                file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                            api.uploadMedia(body, auth)
                        } catch (_: Exception) { Toast.makeText(this@ProfileActivity, "Photo upload failed", Toast.LENGTH_SHORT).show() }
                    }
                    // Save name + about
                    val res = api.updateProfile(UpdateProfileRequest(displayName = name, about = about.ifEmpty { null }), auth)
                    if (res.isSuccessful) {
                        session.saveSession(
                            session.getAccessToken() ?: "",
                            session.getRefreshToken() ?: "",
                            session.getUserId() ?: "",
                            session.getUserPhone() ?: "",
                            name,
                            session.getUserPhoto()
                        )
                        Toast.makeText(this@ProfileActivity, "Profile saved", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Save failed, try again", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    progress.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        }
    }

    private fun launchCamera() = cameraLauncher.launch(null)
    private fun updateAvatarPreview(uri: Uri) = try { findViewById<ImageView>(R.id.imgProfileAvatar).setImageURI(uri) } catch (_: Exception) {}
    override fun onOptionsItemSelected(item: MenuItem): Boolean { if (item.itemId == android.R.id.home) { finish(); return true }; return super.onOptionsItemSelected(item) }
}
