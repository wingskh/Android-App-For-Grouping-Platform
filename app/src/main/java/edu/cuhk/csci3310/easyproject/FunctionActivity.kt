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


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_func_drawer.*
import kotlinx.android.synthetic.main.app_bar_func_drawer.*
import kotlinx.android.synthetic.main.popup_take_picture.view.*


class FunctionActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var userName: String = ""
    private lateinit var manager: FragmentManager
    private var userSID: String = ""
    private var userUID: String = ""
    private lateinit var deptListFragment: DeptListFragment
    private lateinit var projectsListFragment: ProjectsListFragment
    private var createdProjectsList: ArrayList<String?> = ArrayList()
    private var joinedProjectsList: ArrayList<String?> = ArrayList()
    private lateinit var popupCameraWindow: PopupWindow
    private val permissionCode: Int = 1000
    private val imageCaptureCode: Int = 1001
    private var icon: ImageView? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle :Bundle? =intent.extras
        userName = bundle?.getString("userName").toString()
        userSID = bundle?.getString("userSID").toString()
        userUID = bundle?.getString("userUID").toString()
        createdProjectsList = bundle?.getStringArrayList("createdProjectsList") as ArrayList<String?>
        joinedProjectsList = bundle.getStringArrayList("joinedProjectsList") as ArrayList<String?>
        this.title = "Hi, $userName!"

        setContentView(R.layout.activity_func_drawer)
        val navigationView: NavigationView =findViewById(R.id.nav_view)
        val headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_func_drawer)
        headerLayout.findViewById<TextView>(R.id.textView_name).text = userName
        headerLayout.findViewById<TextView>(R.id.textView).text = userSID

        icon = headerLayout.findViewById(R.id.icon)
        FirebaseDatabase.getInstance().reference
            .child("users/$userUID/icon_uri/")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        icon!!.setImageURI(Uri.parse(p0.value as String))
                    }
                }
            })

        icon!!.setOnClickListener(this::showCameraPopupWindow)

        popupCameraWindow = PopupWindow(this)
        toggle()
        toolbar.title = "Easy Project"
        initActivity()
    }

    @SuppressLint("ObsoleteSdkInt", "InflateParams")
    private fun showCameraPopupWindow(view: View) {
        val popupView: View = LayoutInflater.from(this).inflate(R.layout.popup_take_picture, null)
        popupCameraWindow.contentView = popupView

        popupView.cancel_btn.setOnClickListener{
            popupCameraWindow.dismiss()
        }

        popupView.take_btn.setOnClickListener{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, permissionCode)
                }else{
                    openCamera()
                    popupCameraWindow.dismiss()
                    supportFragmentManager.popBackStack()
                }
            }else{
                openCamera()
                popupCameraWindow.dismiss()
                supportFragmentManager.popBackStack()
            }
        }


        popupCameraWindow.width = ViewGroup.LayoutParams.MATCH_PARENT
        popupCameraWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupCameraWindow.isOutsideTouchable = true
        popupCameraWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private fun openCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Icon")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, imageCaptureCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            icon!!.setImageURI(imageUri)

            FirebaseDatabase.getInstance().reference
                .child("users/$userUID/icon_uri/")
                .setValue(
                    imageUri.toString()
                )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera()
                }else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun toggle(){
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun initActivity(){
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_camera)
        }

        manager = supportFragmentManager
        val args = Bundle()
        args.putString("type", "department")
        args.putString("userName", userName)
        args.putString("userSID", userSID)
        args.putString("userUID", userUID)
        args.putStringArrayList("createdProjectsList", createdProjectsList)
        args.putStringArrayList("joinedProjectsList", joinedProjectsList)
        deptListFragment = DeptListFragment()
        deptListFragment.arguments = args

        if(manager.fragments.size == 1){
            manager
                .beginTransaction()
                .replace(R.id.func_fragment_main, deptListFragment)
                .commit()
        }

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.nav_project -> {
                val args = Bundle()
                args.putString("type", "department")
                args.putString("userName", userName)
                args.putString("userSID", userSID)
                args.putString("userUID", userUID)
                args.putStringArrayList("createdProjectsList", createdProjectsList)
                args.putStringArrayList("joinedProjectsList", joinedProjectsList)

                deptListFragment = DeptListFragment()
                deptListFragment.arguments = args
                supportFragmentManager
                    .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.func_fragment_main, deptListFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
            }

            R.id.nav_joined-> {
                val args = Bundle()
                args.putString("userName", userName)
                args.putString("userSID", userSID)
                args.putString("type", "joinedProjects")
                args.putString("userUID", userUID)
                args.putStringArrayList("joinedProjectsList", joinedProjectsList)

                projectsListFragment = ProjectsListFragment()
                projectsListFragment.arguments = args

                supportFragmentManager
                    .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.func_fragment_main, projectsListFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()

            }
            R.id.nav_created-> {
                val args = Bundle()
                args.putString("userName", userName)
                args.putString("userSID", userSID)
                args.putString("userUID", userUID)
                args.putString("type", "createdProjects")
                args.putStringArrayList("createdProjectsList", createdProjectsList)
                Log.d("Nav Activity:", createdProjectsList.toString())
                projectsListFragment = ProjectsListFragment()
                projectsListFragment.arguments = args

                supportFragmentManager
                    .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.func_fragment_main, projectsListFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()

            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount <= 0){
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle(R.string.app_name)
                .setMessage("Confirm to logout?")
                .setPositiveButton("Yes"
                ) { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent) }
                .setNegativeButton("Cancel"
                ) { dialog, _ -> dialog.cancel() }
                .show()
        }else{
            super.onBackPressed()
        }
    }
}
