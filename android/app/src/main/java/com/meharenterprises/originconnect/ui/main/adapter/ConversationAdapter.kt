package com.meharenterprises.originconnect.ui.main.adapter
import android.graphics.*
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.ContactNameCache
import com.meharenterprises.originconnect.data.model.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val nameCache: ContactNameCache,
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(a: Conversation, b: Conversation) = a.id == b.id
            override fun areContentsTheSame(a: Conversation, b: Conversation) = a == b
        }
        private val COLORS = listOf(0xFFE53935,0xFFE91E63,0xFF9C27B0,0xFF3F51B5,
            0xFF1976D2,0xFF0097A7,0xFF388E3C,0xFFF57C00).map { it.toInt() }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img:     ImageView = view.findViewById(R.id.imgAvatar)
        val online:  View      = view.findViewById(R.id.onlineIndicator)
        val name:    TextView  = view.findViewById(R.id.tvName)
        val snippet: TextView  = view.findViewById(R.id.tvSnippet)
        val time:    TextView  = view.findViewById(R.id.tvTime)
        val badge:   TextView  = view.findViewById(R.id.tvUnread)
        val tick:    ImageView = view.findViewById(R.id.imgTick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))
            .also { vh -> vh.itemView.setOnClickListener { onClick(getItem(vh.bindingAdapterPosition)) } }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val conv = getItem(pos)
        val displayName = nameCache.resolveUserId(conv.otherUserId) ?: ""
        val photoUrl    = nameCache.getPhotoUrl(conv.otherUserId)
        val color       = COLORS[conv.otherUserId.hashCode().and(0x7FFFFFFF) % COLORS.size]

        if (!photoUrl.isNullOrEmpty()) {
            holder.img.load(photoUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(makeAvatarDrawable(displayName, color, holder.img.context))
                error(makeAvatarDrawable(displayName, color, holder.img.context))
            }
        } else {
            holder.img.setImageBitmap(makeAvatar(displayName, color))
        }

        holder.name.text = displayName.ifEmpty { "Unknown" }
        holder.name.setTypeface(null, if (conv.unreadCount > 0) Typeface.BOLD else Typeface.NORMAL)
        holder.snippet.text = conv.lastMessageContent ?: ""
        holder.snippet.setTypeface(null, if (conv.unreadCount > 0) Typeface.BOLD else Typeface.NORMAL)
        holder.time.text = fmt(conv.lastMessageAt)
        holder.time.setTextColor(
            if (conv.unreadCount > 0) holder.itemView.context.getColor(R.color.oc_primary)
            else holder.itemView.context.getColor(R.color.oc_text_secondary)
        )
        if (conv.unreadCount > 0) {
            holder.badge.visibility = View.VISIBLE
            holder.badge.text = if (conv.unreadCount > 99) "99+" else "${conv.unreadCount}"
        } else holder.badge.visibility = View.GONE
    }

    private fun fmt(ms: Long?): String {
        if (ms == null || ms <= 0) return ""
        val now = Calendar.getInstance(); val cal = Calendar.getInstance().also { it.timeInMillis = ms }
        return when {
            now.get(Calendar.DATE) == cal.get(Calendar.DATE) -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ms))
            now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ms))
            else -> SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(ms))
        }
    }

    private fun makeAvatarDrawable(name: String, color: Int, ctx: android.content.Context): android.graphics.drawable.BitmapDrawable {
        return android.graphics.drawable.BitmapDrawable(ctx.resources, makeAvatar(name, color))
    }

    private fun makeAvatar(name: String, color: Int): Bitmap {
        val S = 256; val h = S / 2f
        val bmp = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp); val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color; cv.drawCircle(h, h, h, p); p.color = Color.WHITE
        val init = name.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "?"
        p.textSize = S * 0.44f; p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        val fm = p.fontMetrics; cv.drawText(init, h, h - (fm.ascent + fm.descent) / 2f, p)
        return bmp
    }
}
