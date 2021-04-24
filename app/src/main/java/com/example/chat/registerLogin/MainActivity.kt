package com.example.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.chat.messages.LatestMessagesActivitiy
import com.example.chat.models.User
import com.example.chat.registerLogin.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val register : Button = findViewById(R.id.register_button_register)
        register.setOnClickListener {
            performRegister()

        }

        //change to login activity
        val have_acc : TextView = findViewById(R.id.already_have_an_account_text_view)
        have_acc.setOnClickListener {
            Log.d(TAG, "Try to show login activity")

            //launch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        //
        // Select photo from device
        val select_photo : ImageView = findViewById(R.id.select_image_image_view_register)
        select_photo.setOnClickListener {
            Log.d(TAG, "Try to select a photo")

            //choose photo
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var SelectedPhoto : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Photo was selected")

            SelectedPhoto = data.data
            val selectphoto_circle : de.hdodenhof.circleimageview.CircleImageView = findViewById(R.id.selectphoto_circle_imageview_register)
            selectphoto_circle.setImageURI(SelectedPhoto)
            //val select_photo : ImageView = findViewById(R.id.select_image_image_view_register)
            //select_photo.setImageURI(SelectedPhoto)
        }
    }

    private fun performRegister(){
        val email_view : EditText = findViewById(R.id.email_edittext_register)
        val email = email_view.text.toString()
        val password_view : EditText = findViewById(R.id.password_edittext_register)
        val password = password_view.text.toString()

        Log.d(TAG, "Email is: $email")
        Log.d(TAG,"Password is: $password")

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(baseContext,"Please enter email/pw!",Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6){
            Toast.makeText(baseContext,"Password is too short, minimmum 6 characters!",Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        Log.d(TAG,"Created user successfully with uid: ${task.result?.user?.uid}")
                        Toast.makeText(baseContext, "Created user successfully!",Toast.LENGTH_SHORT).show()
                        UploadImageToFirebaseStorage()
                        return@addOnCompleteListener
                    }
                    else {
                        Log.w(TAG,"Create user failed: ${task.exception?.message}")
                        Toast.makeText(baseContext,"Create user failed: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                }
    }
    private fun UploadImageToFirebaseStorage() {
        if (SelectedPhoto == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/Images/$filename")

        ref.putFile(SelectedPhoto!!)
                .addOnSuccessListener {
                    Log.d(TAG,"Successfully upload image: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG,"File location: $it")

                        SaveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG,"Failed to upload image to storage: ${it.message}")
                }
    }
    private fun SaveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val username : EditText = findViewById(R.id.username_edittext_register)
        val password : EditText = findViewById(R.id.password_edittext_register)
        val user = User(uid,username.text.toString(), profileImageUrl,password.text.toString())

        ref.setValue(user)
                .addOnSuccessListener {

                    Log.d(TAG, "Finally saved user to Firebase Database")

                    val intent = Intent(this, LatestMessagesActivitiy::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d(TAG,"Failed to save on Firebase Database: ${it.message}")
                }
    }
}
