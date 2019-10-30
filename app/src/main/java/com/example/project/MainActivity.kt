package com.example.project

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.LoginFilter
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            performregister()
        }
        //intent to login activity
        al.setOnClickListener {
            val i= Intent(this,Main2Activity::class.java)
            startActivity(i)
        }
        iv.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type="image/*"
            startActivityForResult(i,0)
        }

    }
    var photouri:Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==0&&requestCode==Activity.RESULT_OK&&data!=null)
        {
          var photouri:Uri?=data.data
//            val source=ImageDecoder.createSource(contentResolver,photouri)
//            val bitmap=MediaStore.Images.Media.getBitmap(contentResolver,photouri)
           // val bitmapDrawable= BitmapDrawable(this.resources,bitmap)
         //   iv.setBackground(bitmapDrawable)
//            val bitmap = ImageDecoder.decodeBitmap(source)
//            iv.setImageBitmap(bitmap)
            photouri?.let {
                if(Build.VERSION.SDK_INT < 28) {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, photouri)
                    iv.setImageBitmap(bitmap)
                } else {
                    val source = ImageDecoder.createSource(this.contentResolver, photouri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    iv.setImageBitmap(bitmap)
                }
            }


        }
    }
    private fun performregister()
    {
        // register
        val email=editText.text.toString()
        val password=editText2.text.toString()
        if(email.isEmpty()||password.isEmpty())
        {
            Toast.makeText(this,"Please enter Email Address And Password", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful)
                {
                    return@addOnCompleteListener
                }
                uploadImagetoFirbaseStorage()
                Log.d("Main", "createUserWithEmail:success")
            }
            .addOnFailureListener {
                Log.i("Main","Fail to create${it.message}")
                Toast.makeText(this,"Fail to create${it.message}", Toast.LENGTH_SHORT).show()
            }

    } //end

    private fun uploadImagetoFirbaseStorage() {
        if (photouri==null)
            return
        val filename= UUID.randomUUID().toString()
          val ref=  FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(photouri!!)
            .addOnSuccessListener {
                Log.i("Register","Successfully upload images:${it.metadata?.path}")
            }
        ref.downloadUrl.addOnSuccessListener {
            Log.i("RegisterActivity","File Location:${it}")
            saveUserToFireBaseDatabase(it.toString())
        }
            .addOnFailureListener {

            }

    }

    private fun saveUserToFireBaseDatabase(profileImageUrl: String) {
         val uid = FirebaseAuth.getInstance().uid?:""
       val ref=  FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user=User(uid,usernameedittext.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Finally saved To database")
            }
    }
}
class User(val uid:String,val username:String,val profileImageUrl:String)
{

}
