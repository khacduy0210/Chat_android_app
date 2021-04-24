package com.example.chat.messages

import android.graphics.Color
import android.graphics.Color.BLUE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.NewMessageActivity
import com.example.chat.R
import com.example.chat.models.ChatMessage
import com.example.chat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG = "ChatLog"
    }
    var toUser : User? = null
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
//        setupDummyData()
        val recyclerView_chat_log : RecyclerView = findViewById(R.id.recyclerview_chat_log)
        recyclerView_chat_log.adapter = adapter

        ListenForMessage()
        //click button send to send message
        val send_button : Button = findViewById(R.id.send_button_chat_log)
        val editText_chat_log : EditText = findViewById(R.id.edittext_chat_log)
        val text = editText_chat_log.text.toString()
        if ( text != "") {
            send_button.setOnClickListener {
                Log.d(TAG, "Try to send message!")
                performSendMessage()
            }
        }
    }
    private fun ListenForMessage(){
        val fromId = FirebaseAuth.getInstance().uid.toString()
        val toId = toUser?.uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/users-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat_message = snapshot.getValue(ChatMessage::class.java)
                if (chat_message != null) {
                    Log.d(TAG, chat_message.text)
                    Log.d(TAG, chat_message.fromId)
                    Log.d(TAG, FirebaseAuth.getInstance().uid.toString())
                    if (chat_message.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivitiy.currentUser
                        adapter.add(ChatFromItem(chat_message.text, currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chat_message.text, toUser!!))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }
    //send message and save to database

    private fun performSendMessage() {
        val editText_chat_log : EditText = findViewById(R.id.edittext_chat_log)
        val text = editText_chat_log.text.toString()

//        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val fromId = FirebaseAuth.getInstance().uid.toString()
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid.toString()

        val ref = FirebaseDatabase.getInstance().getReference("/users-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/users-messages/$toId/$fromId").push()
        val chat_message = ChatMessage(ref.key!!, text, fromId, toId,System.currentTimeMillis() / 1000)
            ref.setValue(chat_message)
                    .addOnSuccessListener {
                        Log.d(TAG, "Saved our chat message: ${ref.key}")
                        editText_chat_log.text.clear()
                        recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                    }
            toRef.setValue(chat_message)

            val lastestMessageRef = FirebaseDatabase.getInstance().getReference("/lastest-message/$fromId/$toId")
            lastestMessageRef.setValue(chat_message)
            val lastestMessageToRef = FirebaseDatabase.getInstance().getReference("/lastest-message/$toId/$fromId")
            lastestMessageToRef.setValue(chat_message)

    }
}
class ChatFromItem(val text: String,val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
        Picasso.get().load(user.profileImageUrl.toString()).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_from_row))
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}
class ChatToItem(val text: String,val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
        Picasso.get().load(user.profileImageUrl.toString()).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row))
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}