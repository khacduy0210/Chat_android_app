package com.example.chat.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.MainActivity
import com.example.chat.NewMessageActivity
import com.example.chat.R
import com.example.chat.models.ChatMessage
import com.example.chat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessagesActivitiy : AppCompatActivity() {
    companion object{
        var currentUser : User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages_activitiy)
        val adapter = GroupAdapter<GroupieViewHolder>()

        val recyclerview_lastest_message : RecyclerView = findViewById(R.id.recycleview_lastest_messages)
        recyclerview_lastest_message.adapter = adapter
//        setupDummyRow()
        ListenForLastestMessages()
        fetchCurrentUser()
        VerifyUserIsLoggedIn()
    }
    class LastestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.message_textview_lastest_message).text = chatMessage.text
        }

        override fun getLayout(): Int {
            return R.layout.lastest_message_row
        }
    }
    val LastestMessageMap = HashMap<String, ChatMessage>()
    private fun refreshrecycleViewMessages(){
        adapter.clear()
        LastestMessageMap.values.forEach {
            adapter.add(LastestMessageRow(it))
        }
    }

    private fun ListenForLastestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/lastest-message/$fromId")

        val recyclerview_lastest_message : RecyclerView = findViewById(R.id.recycleview_lastest_messages)
        recyclerview_lastest_message.adapter = adapter
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                LastestMessageMap[snapshot.key!!] = chatMessage
                refreshrecycleViewMessages()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                LastestMessageMap[snapshot.key!!] = chatMessage
                refreshrecycleViewMessages()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    val adapter = GroupAdapter<GroupieViewHolder>()
//    private fun setupDummyRow(){
//        val adapter = GroupAdapter<GroupieViewHolder>()
////        adapter.add(LastestMessageRow())
////        adapter.add(LastestMessageRow())
////        adapter.add(LastestMessageRow())
//
//
//    }
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("LatestMessage", "Current user ${currentUser?.profileImageUrl}")
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun VerifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}