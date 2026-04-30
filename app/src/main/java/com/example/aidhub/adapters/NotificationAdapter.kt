package com.example.aidhub.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.data.dataStractures.Notification
import com.example.aidhub.databinding.ItemNotificationBinding
import com.example.aidhub.R
import com.example.data.dataStractures.NotificationType

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onNotificationClicked: (Notification) -> Unit
) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return notifications.size

    }

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {

            val unreadColor =
                ContextCompat.getColor(binding.root.context, R.color.unread_background)
            val readColor = ContextCompat.getColor(binding.root.context, R.color.read_background)

            if (notification.read) {
                binding.holderView.setCardBackgroundColor(readColor)
                binding.holderView.cardElevation = 2f
                binding.unreadBadge.visibility = ViewGroup.GONE
            } else {
                binding.holderView.setCardBackgroundColor(unreadColor)
                binding.holderView.cardElevation = 8f
                binding.unreadBadge.visibility = ViewGroup.VISIBLE
            }

            binding.txtTitle.text = notification.title
            binding.txtBody.text = notification.body
            val time = notification.timestamp.toDate().time
            binding.txtTime.text = DateUtils.getRelativeTimeSpanString(time)
            if (notification.type == NotificationType.HELPER_FOUND.displayName)
                binding.imgNotification.setImageResource(R.drawable.img_help_notification)
            if (notification.type == NotificationType.NEW_REQUEST.displayName)
                binding.imgNotification.setImageResource(R.drawable.img_new_request_notification)
            if (notification.type == NotificationType.REQUEST_CLOSED.displayName)
                binding.imgNotification.setImageResource(R.drawable.img_complete_notification)
            if (notification.type == NotificationType.POST_LIKE.displayName)
                binding.imgNotification.setImageResource(R.drawable.img_like_notification)


            binding.root.setOnClickListener { onNotificationClicked(notification) }

        }
    }

}
