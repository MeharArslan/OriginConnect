package com.meharenterprises.originconnect.ui.profile
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.remote.ApiService
import com.meharenterprises.originconnect.data.remote.UpdatePhotoRequest
import com.meharenterprises.originconnect.data.remote.UpdateProfileRequest
import com.yalantis.ucrop.UCrop
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
    private var croppedUri: Uri? = null

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null) else toast("Camera permission denied")
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
        bmp?.let {
            val f = File(cacheDir, "cam_${System.currentTimeMillis()}.jpg")
            f.outputStream().use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
            startCrop(Uri.fromFile(f))
        }
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { startCrop(it) }
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val result = UCrop.getOutput(data!!)
            result?.let { uri ->
                croppedUri = uri
                try { findViewById<ImageView>(R.id.imgProfileAvatar).setImageURI(uri) } catch (_: Exception) {}
            }
        }
    }

    private fun startCrop(source: Uri) {
        val dest = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        UCrop.of(source, dest)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .start(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_profile)

        val tb         = findViewById<Toolbar>(R.id.profileToolbar)
        val img        = findViewById<ImageView>(R.id.imgProfileAvatar)
        val tvPhone    = findViewById<TextView>(R.id.tvProfilePhone)
        val etName     = findViewById<TextInputEditText>(R.id.etProfileName)
        val spinner    = findViewById<Spinner>(R.id.spinnerAbout)
        val etAbout    = findViewById<TextInputEditText>(R.id.etProfileAbout)
        val btnSave    = findViewById<Button>(R.id.btnSaveProfile)
        val progress   = findViewById<ProgressBar>(R.id.profileProgress)
        val btnCamera  = findViewById<MaterialButton>(R.id.btnCamera)
        val btnGallery = findViewById<MaterialButton>(R.id.btnGallery)

        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"

        val presets = listOf("Available", "Busy", "At work", "At school",
            "Battery about to die", "In a meeting", "At the gym",
            "Sleeping", "Urgent calls only", "Custom...")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, presets)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p: AdapterView<*>?) {}
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos < presets.size - 1) { etAbout.setText(presets[pos]); etAbout.isEnabled = false }
                else { etAbout.isEnabled = true; etAbout.text?.clear() }
            }
        }

        lifecycleScope.launch {
            tvPhone.text = session.getUserPhone() ?: ""
            val name = session.getUserName() ?: ""
            if (name.isNotEmpty() && !name.startsWith("+")) etName.setText(name)
            // Load existing photo
            val photo = session.getUserPhoto()
            if (!photo.isNullOrEmpty()) {
                try {
                    coil.load(img, photo) { transformations(coil.transform.CircleCropTransformation()) }
                } catch (_: Exception) {}
            }
        }

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                cameraLauncher.launch(null)
            else cameraPermission.launch(Manifest.permission.CAMERA)
        }
        btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        img.setOnClickListener { galleryLauncher.launch("image/*") }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.length < 2) { toast("Name must be at least 2 characters"); return@setOnClickListener }
            val about = etAbout.text.toString().trim()
            progress.visibility = View.VISIBLE; btnSave.isEnabled = false
            lifecycleScope.launch {
                try {
                    val auth = session.getAuthHeader()
                    if (croppedUri != null) {
                        try {
                            val stream = contentResolver.openInputStream(croppedUri!!)
                            val file = File(cacheDir, "profile_upload_${System.currentTimeMillis()}.jpg")
                            file.outputStream().use { stream?.copyTo(it) }
                            val body = MultipartBody.Part.createFormData("file", file.name,
                                file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                            val res = api.uploadMedia(body, auth)
                            if (res.isSuccessful) {
                                val url = res.body()?.url
                                if (!url.isNullOrEmpty()) {
                                    api.updatePhoto(UpdatePhotoRequest(url), auth)
                                    session.saveSession(session.getAccessToken()!!, session.getRefreshToken()!!,
                                        session.getUserId()!!, session.getUserPhone()!!, name, url)
                                }
                            } else toast("Photo upload failed — check connection")
                        } catch (e: Exception) { toast("Photo error: ${e.message}") }
                    }
                    val res = api.updateProfile(UpdateProfileRequest(displayName = name, about = about.ifEmpty { null }), auth)
                    if (res.isSuccessful) {
                        session.saveSession(session.getAccessToken()!!, session.getRefreshToken()!!,
                            session.getUserId()!!, session.getUserPhone()!!, name, session.getUserPhoto())
                        toast("Profile saved")
                        setResult(Activity.RESULT_OK); finish()
                    } else toast("Save failed — check connection")
                } catch (e: Exception) { toast("Error: ${e.message}") }
                finally { progress.visibility = View.GONE; btnSave.isEnabled = true }
            }
        }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
    override fun onOptionsItemSelected(item: MenuItem): Boolean { if (item.itemId == android.R.id.home) { finish(); return true }; return super.onOptionsItemSelected(item) }
}
