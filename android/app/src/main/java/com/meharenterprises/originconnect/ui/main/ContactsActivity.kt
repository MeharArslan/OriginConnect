package com.meharenterprises.originconnect.ui.main
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Contact
import com.meharenterprises.originconnect.ui.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsActivity : AppCompatActivity() {
    private val vm: ContactsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        val toolbar = findViewById<Toolbar>(R.id.contactsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select contact"
        toolbar.setNavigationOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerContacts)
        val etSearch = findViewById<EditText>(R.id.etContactSearch)
        val progress = findViewById<ProgressBar>(R.id.contactsProgress)
        val tvEmpty = findViewById<TextView>(R.id.tvContactsEmpty)

        val adapter = ContactsListAdapter { contact -> vm.openConversation(contact.user.id) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) { vm.filter(s.toString()) }
        })

        lifecycleScope.launch {
            vm.state.collectLatest { state ->
                when (state) {
                    is ContactsState.Loading -> { progress.visibility = View.VISIBLE; recycler.visibility = View.GONE; tvEmpty.visibility = View.GONE }
                    is ContactsState.Success -> {
                        progress.visibility = View.GONE
                        if (state.filtered.isEmpty()) {
                            recycler.visibility = View.GONE; tvEmpty.visibility = View.VISIBLE
                            tvEmpty.text = if (state.contacts.isEmpty()) "No contacts on OriginConnect yet.
Invite friends to get started." else "No contacts match your search."
                        } else { recycler.visibility = View.VISIBLE; tvEmpty.visibility = View.GONE; adapter.submitList(state.filtered) }
                    }
                    is ContactsState.Error -> { progress.visibility = View.GONE; tvEmpty.visibility = View.VISIBLE; tvEmpty.text = state.message }
                }
            }
        }

        lifecycleScope.launch {
            vm.openChat.collectLatest { result ->
                when (result) {
                    is OpenChatResult.Loading -> progress.visibility = View.VISIBLE
                    is OpenChatResult.Ready -> {
                        progress.visibility = View.GONE
                        startActivity(Intent(this@ContactsActivity, ChatActivity::class.java).apply {
                            putExtra("CONVERSATION_ID", result.conversationId)
                            putExtra("OTHER_USER_ID", result.otherUserId)
                        })
                        vm.resetOpenChat(); finish()
                    }
                    is OpenChatResult.Error -> {
                        progress.visibility = View.GONE
                        Toast.makeText(this@ContactsActivity, result.msg, Toast.LENGTH_SHORT).show()
                        vm.resetOpenChat()
                    }
                    else -> progress.visibility = View.GONE
                }
            }
        }
        vm.loadContacts()
    }
}

class ContactsListAdapter(private val onClick: (Contact) -> Unit) :
    ListAdapter<Contact, ContactsListAdapter.VH>(DIFF) {
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(a: Contact, b: Contact) = a.contactId == b.contactId
            override fun areContentsTheSame(a: Contact, b: Contact) = a == b
        }
        private val colors = listOf(0xFFE53935.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF3F51B5.toInt(), 0xFF1976D2.toInt(), 0xFF0097A7.toInt(), 0xFF388E3C.toInt(), 0xFFF57C00.toInt())
    }
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgContactAvatar)
        val name: TextView = v.findViewById(R.id.tvContactName)
        val phone: TextView = v.findViewById(R.id.tvContactPhone)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_contact, p, false)).also { vh -> vh.itemView.setOnClickListener { onClick(getItem(vh.bindingAdapterPosition)) } }
    override fun onBindViewHolder(h: VH, pos: Int) {
        val c = getItem(pos); h.name.text = c.nickname ?: c.user.displayName; h.phone.text = c.user.phone
        val color = colors[c.user.id.hashCode().and(0x7FFFFFFF) % colors.size]
        val S = 160; val hf = S/2f; val bmp = Bitmap.createBitmap(S,S,Bitmap.Config.ARGB_8888); val cv = Canvas(bmp); val pt = Paint(Paint.ANTI_ALIAS_FLAG)
        pt.color = color; cv.drawCircle(hf,hf,hf,pt); pt.color = Color.WHITE
        val init = c.user.displayName.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "?"; pt.textSize = S*0.44f; pt.textAlign = Paint.Align.CENTER; pt.typeface = Typeface.create("sans-serif-medium",Typeface.NORMAL); val fm = pt.fontMetrics; cv.drawText(init,hf,hf-(fm.ascent+fm.descent)/2f,pt)
        h.img.setImageBitmap(bmp)
    }
}
