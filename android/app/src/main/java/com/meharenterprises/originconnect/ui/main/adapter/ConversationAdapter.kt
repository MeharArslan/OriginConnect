package com.meharenterprises.originconnect.ui.main.adapter
import android.graphics.*
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(a: Conversation, b: Conversation) = a.id == b.id
            override fun areContentsTheSame(a: Conversation, b: Conversation) = a == b
        }
        private val colors = listOf(
            0xFFE53935.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(),
            0xFF3F51B5.toInt(), 0xFF1976D2.toInt(), 0xFF0097A7.toInt(),
            0xFF388E3C.toInt(), 0xFFF57C00.toInt(), 0xFF795548.toInt(), 0xFF455A64.toInt()
        )
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
        val onlineIndicator: View = view.findViewById(R.id.onlineIndicator)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSnippet: TextView = view.findViewById(R.id.tvSnippet)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvUnread: TextView = view.findViewById(R.id.tvUnread)
        val imgTick: ImageView = view.findViewById(R.id.imgTick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return VH(v).also { vh -> vh.itemView.setOnClickListener { onClick(getItem(vh.bindingAdapterPosition)) } }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val conv = getItem(position)
        val color = colors[conv.otherUserId.hashCode().and(0x7FFFFFFF) % colors.size]
        holder.imgAvatar.setImageBitmap(makeAvatar(conv.otherUserId, color))
        holder.tvName.text = conv.otherUserId.take(12)
        holder.tvName.setTypeface(null, if (conv.unreadCount > 0) Typeface.BOLD else Typeface.NORMAL)
        holder.tvSnippet.text = conv.lastMessageContent ?: ""
        holder.tvSnippet.setTypeface(null, if (conv.unreadCount > 0) Typeface.BOLD else Typeface.NORMAL)
        holder.tvTime.text = formatTime(conv.lastMessageAt)
        if (conv.unreadCount > 0) {
            holder.tvUnread.visibility = View.VISIBLE
            holder.tvUnread.text = if (conv.unreadCount > 99) "99+" else conv.unreadCount.toString()
            holder.tvTime.setTextColor(holder.itemView.context.getColor(R.color.oc_primary))
        } else {
            holder.tvUnread.visibility = View.GONE
            holder.tvTime.setTextColor(holder.itemView.context.getColor(R.color.oc_text_secondary))
        }
    }

    private fun formatTime(millis: Long?): String {
        if (millis == null || millis <= 0) return ""
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().also { it.timeInMillis = millis }
        return when {
            now.get(Calendar.DATE) == cal.get(Calendar.DATE) ->
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
            now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) ->
                SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
            else -> SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(millis))
        }
    }

    private fun makeAvatar(seed: String, color: Int): Bitmap {
        val S = 256; val h = S / 2f
        val bmp = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color; canvas.drawCircle(h, h, h, paint)
        paint.color = Color.WHITE
        val isPhone = seed.replace("+","").replace(" ","").replace("-","").all { it.isDigit() }
        if (isPhone) {
            canvas.drawCircle(h, S*0.36f, S*0.21f, paint)
            canvas.drawOval(RectF(S*0.11f, S*0.60f, S*0.89f, S*1.08f), paint)
        } else {
            val initial = seed.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString() ?: "?"
            paint.textSize = S*0.44f; paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            val fm = paint.fontMetrics
            canvas.drawText(initial, h, h-(fm.ascent+fm.descent)/2f, paint)
        }
        return bmp
    }
}
