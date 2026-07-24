package com.meharenterprises.originconnect.ui.chat.adapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(var myId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DIFF) {

    var onDelete: ((String, Boolean) -> Unit)? = null

    companion object {
        const val SENT = 1; const val RECV = 2
        val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
            override fun areContentsTheSame(a: Message, b: Message) = a == b
        }
    }

    inner class SentVH(v: View) : RecyclerView.ViewHolder(v) {
        val body: TextView   = v.findViewById(R.id.txtBody)
        val time: TextView   = v.findViewById(R.id.txtTime)
        val tick: ImageView  = v.findViewById(R.id.txtStatus)
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
                h.body.text = body; h.time.text = time
                when (msg.status) {
                    "read"      -> { h.tick.setImageResource(R.drawable.ic_tick_double); h.tick.setColorFilter(0xFF4FC3F7.toInt()) }
                    "delivered" -> { h.tick.setImageResource(R.drawable.ic_tick_double); h.tick.clearColorFilter() }
                    else        -> { h.tick.setImageResource(R.drawable.ic_tick_single); h.tick.clearColorFilter() }
                }
                h.itemView.setOnLongClickListener { showMenu(h.itemView.context, msg, true); true }
            }
            is RecvVH -> {
                h.body.text = body; h.time.text = time
                h.itemView.setOnLongClickListener { showMenu(h.itemView.context, msg, false); true }
            }
        }
    }

    private fun showMenu(ctx: Context, msg: Message, isMine: Boolean) {
        val options = mutableListOf("Copy", "Reply")
        if (isMine) {
            options.add("Delete for Me")
            options.add("Delete for Everyone")
        } else {
            options.add("Delete for Me")
        }
        AlertDialog.Builder(ctx)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Copy" -> {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("msg", msg.content ?: ""))
                        Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
                    }
                    "Delete for Me"       -> onDelete?.invoke(msg.id, false)
                    "Delete for Everyone" -> onDelete?.invoke(msg.id, true)
                    "Reply" -> Toast.makeText(ctx, "Reply — coming soon", Toast.LENGTH_SHORT).show()
                }
            }.show()
    }

    private fun formatTime(iso: String): String = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(sdf.parse(iso) ?: Date())
    } catch (_: Exception) { "" }
}
