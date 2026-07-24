package com.meharenterprises.originconnect.ui.chat.adapter
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(var myId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        const val SENT = 1; const val RECV = 2
        val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
            override fun areContentsTheSame(a: Message, b: Message) = a == b
        }
    }

    inner class SentVH(v: View) : RecyclerView.ViewHolder(v) {
        val body:   TextView  = v.findViewById(R.id.txtBody)
        val time:   TextView  = v.findViewById(R.id.txtTime)
        val status: ImageView = v.findViewById(R.id.txtStatus)
    }
    inner class RecvVH(v: View) : RecyclerView.ViewHolder(v) {
        val body: TextView = v.findViewById(R.id.txtBody)
        val time: TextView = v.findViewById(R.id.txtTime)
    }

    override fun getItemViewType(pos: Int) = if (getItem(pos).senderId == myId) SENT else RECV

    override fun onCreateViewHolder(p: ViewGroup, t: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(p.context)
        return if (t == SENT)
            SentVH(inf.inflate(R.layout.item_oc_message_sent, p, false))
        else
            RecvVH(inf.inflate(R.layout.item_oc_message_recv, p, false))
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        val msg  = getItem(pos)
        val body = if (msg.isDeletedForEveryone) "🚫 This message was deleted" else msg.content ?: ""
        val time = formatTime(msg.createdAt)
        when (h) {
            is SentVH -> {
                h.body.text = body
                h.time.text = time
                val icon = when (msg.status) {
                    "read"      -> R.drawable.ic_tick_double
                    "delivered" -> R.drawable.ic_tick_double
                    "sent"      -> R.drawable.ic_tick_single
                    else        -> R.drawable.ic_tick_single
                }
                h.status.setImageResource(icon)
                if (msg.status == "read") {
                    h.status.setColorFilter(h.status.context.getColor(R.color.oc_primary))
                } else {
                    h.status.setColorFilter(0xBBFFFFFF.toInt())
                }
            }
            is RecvVH -> { h.body.text = body; h.time.text = time }
        }
    }

    private fun formatTime(iso: String): String = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(sdf.parse(iso) ?: Date())
    } catch (_: Exception) { "" }
}
