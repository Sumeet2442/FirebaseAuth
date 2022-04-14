package com.example.firebaseauth.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauth.R
import com.example.firebaseauth.models.Message

class MessageAdapter(private val layout:Int,private val messages:List<Message>):
    RecyclerView.Adapter<MessageAdapter.Holder>() {

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val user:TextView = itemView.findViewById(R.id.txtUser)
        val msg:TextView = itemView.findViewById(R.id.txtMess)

        fun bind(message: Message){
            user.text=message.name
            msg.text=message.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.message_cards,parent,false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount() = messages.size
    }