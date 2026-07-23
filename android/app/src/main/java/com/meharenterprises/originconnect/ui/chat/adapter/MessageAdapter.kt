package com.meharenterprises.originconnect.ui.chat.adapter
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(var myId: String) : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        const val VIEW_SENT = 1
        const val VIEW_RECV = 2
        val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
            override fun areContentsTheSame(a: Message, b: Message) = a == b
        }
    }

    inner class SentVH(view: View) : RecyclerView.ViewHolder(view) {
        val txtBody: TextView = view.findViewById(R.id.txtBody)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
    }

    inner class RecvVH(view: View) : RecyclerView.ViewHolder(view) {
        val txtBody: TextView = view.findViewById(R.id.txtBody)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).senderId == myId) VIEW_SENT else VIEW_RECV

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_SENT)
            SentVH(inf.inflate(R.layout.item_oc_message_sent, parent, false))
        else
            RecvVH(inf.inflate(R.layout.item_oc_message_recv, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            fmt.format(sdf.parse(msg.createdAt) ?: Date())
        } catch (_: Exception) { fmt.format(Date()) }

        val body = if (msg.isDeletedForEveryone) "Message deleted" else msg.content ?: ""
        when (holder) {
            is SentVH -> {
                holder.txtBody.text = body
                holder.txtTime.text = time
                holder.txtStatus.text = when (msg.status) { "read" -> "✓✓"; "delivered" -> "✓✓"; "sent" -> "✓"; else -> "⏳" }
            }
            is RecvVH -> { holder.txtBody.text = body; holder.txtTime.text = time }
        }
    }
}
