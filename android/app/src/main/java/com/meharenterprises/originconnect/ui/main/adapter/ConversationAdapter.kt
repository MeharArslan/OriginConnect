package com.meharenterprises.originconnect.ui.main.adapter
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(private val onClick: (Conversation) -> Unit) :
    ListAdapter<Conversation, ConversationAdapter.VH>(DIFF) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtSnippet: TextView = view.findViewById(R.id.txtSnippet)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val txtBadge: TextView = view.findViewById(R.id.txtBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_oc_conversation, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val conv = getItem(position)
        holder.txtName.text = conv.otherUserId.take(8)
        holder.txtSnippet.text = conv.lastMessageContent ?: ""
        holder.txtTime.text = conv.lastMessageAt?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
        } ?: ""
        if (conv.unreadCount > 0) {
            holder.txtBadge.visibility = View.VISIBLE
            holder.txtBadge.text = if (conv.unreadCount > 99) "99+" else conv.unreadCount.toString()
        } else {
            holder.txtBadge.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onClick(conv) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(a: Conversation, b: Conversation) = a.id == b.id
            override fun areContentsTheSame(a: Conversation, b: Conversation) = a == b
        }
    }
}
