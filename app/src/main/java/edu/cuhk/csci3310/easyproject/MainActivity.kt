/*
 * CSCI3310 Individual Project
 *         You should type also ALL the comment lines (text in gray)
 * I declare that the assignment here submitted is original
 * except for source material explicitly acknowledged,
 * and that the same or closely related material has not been
 * previously submitted for another course.
 * I also acknowledge that I am aware of University policy and
 * regulations on honesty in academic work, and of the disciplinary
 * guidelines and procedures applicable to breaches of such
 * policy and regulations, as contained in the website.
 *
 * University Guideline on Academic Honesty:
 *   http://www.cuhk.edu.hk/policy/academichonesty
 * Faculty of Engineering Guidelines to Academic Honesty:
 *   https://www.erg.cuhk.edu.hk/erg/AcademicHonesty
 *
 * Student Name: Sun Ka Ho
 * Student ID  : 1155098418
 * Date        : 21/5/2020
 */
package edu.cuhk.csci3310.easyproject

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference
    private lateinit var userSID: String
    private lateinit var userPassword: String
    private var userName: String? = ""
    private lateinit var userSidView: EditText
    private lateinit var userPasswordView: EditText
    private lateinit var loginButton: Button
    private var cuhkMail: String = "@link.cuhk.edu.hk"

    fun loginClick(@Suppress("UNUSED_PARAMETER")v: View) {
        userSID = userSidView.text.toString()
        userPassword = userPasswordView.text.toString()
        if(userSID.isNotEmpty() && userPassword.isNotEmpty()){
            mAuth.signInWithEmailAndPassword(userSID+cuhkMail, userPassword).addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    readUserName(this, mAuth.currentUser?.uid.toString())
                }else{
                    Toast.makeText(this, "Incorrect SID or Password!", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(this, "Please fill in both SID and Password!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startFunctionActivity(
        uid: String,
        createdProjectsList: ArrayList<String?>,
        joinedProjectsList: ArrayList<String?>){
        startActivity(Intent(this, FunctionActivity::class.java).apply {
            putExtra("userSID", userSID)
            putExtra("userName", userName)
            putExtra("userUID", uid)
            putStringArrayListExtra("createdProjectsList", createdProjectsList)
            putStringArrayListExtra("joinedProjectsList", joinedProjectsList)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.login_btn)
        userSidView = findViewById(R.id.user_sid)
        userPasswordView = findViewById(R.id.user_password)
        database = FirebaseDatabase.getInstance().getReference("users")
        val logo: ImageView = findViewById(R.id.icon_view)

        val valueAnimator = ValueAnimator.ofFloat(0f, 360f)

        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            logo.rotation = value
        }

        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 10000
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.start()
    }

    private fun readUserName(context: Context,id: String){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(id)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        val userData: Map<String, Any?> = p0.value as Map<String, Any?>
                        val createdProjectsListMap: ArrayList<Map<String, Any>?>?
                        createdProjectsListMap = (userData["created_projects"] as ArrayList<Map<String, Any>?>?)
                        val joinedProjectsListMap: ArrayList<Map<String, Any>?>?
                        joinedProjectsListMap = (userData["joined_projects"] as ArrayList<Map<String, Any>?>?)
                        val createdProjectsList: ArrayList<String?> = ArrayList()
                        val joinedProjectsList: ArrayList<String?> = ArrayList()

                        if(!createdProjectsListMap.isNullOrEmpty()){
                            createdProjectsListMap.forEach {
                                createdProjectsList.add(it?.get("path") as String?)
                            }
                        }
                        if(!joinedProjectsListMap.isNullOrEmpty()){
                            joinedProjectsListMap.forEach {
                                joinedProjectsList.add(it?.get("path") as String?)
                            }
                        }
                        
                        userName = userData["name"] as String
                        startFunctionActivity(id, createdProjectsList, joinedProjectsList)
                    }else{
                        Toast.makeText(context, "Error: Cannot find username!",Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}
