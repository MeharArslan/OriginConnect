package com.meharenterprises.originconnect.ui.main
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.meharenterprises.originconnect.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        title = "Contacts"
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
    }
}
