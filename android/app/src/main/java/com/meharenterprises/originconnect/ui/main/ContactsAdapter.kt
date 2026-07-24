package com.meharenterprises.originconnect.ui.main
import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.ContactNameCache
import com.meharenterprises.originconnect.data.model.Contact

class ContactsAdapter(
    private val nameCache: ContactNameCache,
    private val onClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactsAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(a: Contact, b: Contact) = a.contactId == b.contactId
            override fun areContentsTheSame(a: Contact, b: Contact) = a == b
        }
        private val COLORS = listOf(0xFFE53935,0xFFE91E63,0xFF9C27B0,0xFF3F51B5,
            0xFF1976D2,0xFF0097A7,0xFF388E3C,0xFFF57C00).map { it.toInt() }
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView  = v.findViewById(R.id.imgContactAvatar)
        val name: TextView  = v.findViewById(R.id.tvContactName)
        val phone: TextView = v.findViewById(R.id.tvContactPhone)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_contact, p, false))
            .also { vh -> vh.itemView.setOnClickListener { onClick(getItem(vh.bindingAdapterPosition)) } }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val c = getItem(pos)
        // Local device name takes priority over backend displayName
        val localName = nameCache.resolvePhone(c.user.phone)
        val displayName = localName ?: c.nickname ?: c.user.displayName
        h.name.text = displayName
        h.phone.text = c.user.phone
        val color = COLORS[c.user.id.hashCode().and(0x7FFFFFFF) % COLORS.size]
        h.img.setImageBitmap(makeAvatar(displayName, color))
    }

    private fun makeAvatar(name: String, color: Int): Bitmap {
        val S = 160; val hf = S / 2f
        val bmp = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp); val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color; cv.drawCircle(hf, hf, hf, p); p.color = Color.WHITE
        val init = name.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "?"
        p.textSize = S * 0.44f; p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        val fm = p.fontMetrics; cv.drawText(init, hf, hf - (fm.ascent + fm.descent) / 2f, p)
        return bmp
    }
}
