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

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.popup_create_project.view.create_btn
import kotlinx.android.synthetic.main.popup_project.view.*
import kotlinx.android.synthetic.main.popup_project.view.cancel_btn
import kotlinx.android.synthetic.main.project_list_view.view.*
import java.util.*
import kotlin.collections.ArrayList


@Suppress("UNCHECKED_CAST")
class ProjectsListFragment : Fragment(), ProjectViewAdapter.OnItemClickListener  {
    private var adapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var courseCode: String
    private lateinit var deptCode: String
    private var courseData:  ArrayList<Map<String, Any>?>? = null
    private var courseName: String? = null
    private var userName: String? = null
    private var type: String? = null
    private var userSID: String? = null
    private var userUID: String? = null
    private lateinit var popupJoinProjectWindow: PopupWindow
    private lateinit var popupCreateProjectWindow: PopupWindow
    private var createdProjectsList: ArrayList<String?> = ArrayList()
    private var joinedProjectsList: ArrayList<String?> = ArrayList()
    private var joinedStudentArray: ArrayList<Map<String, Any?>?>? = null
    private var noJoinedStudent:Boolean = false
    private var numOfProject: Int = 0
    private var requiredUserData: ArrayList<Map<String, Any>> = ArrayList()

    private fun initData() {
        layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        Log.d("ProjectsListActivity: ", (courseData == null).toString())
        if(!(courseData?.isNullOrEmpty())!!){
            adapter = ProjectViewAdapter(courseData as ArrayList<Map<String, Any>>, this)
        }
    }

    private fun initView(view: View) {
        val recyclerView = view.projects_list
        Log.d("AACXCDD", this.context.toString())
        if(this.context != null){
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.project_list_view, container, false )
        userName = arguments!!.getString("userName")!!
        userSID = arguments!!.getString("userSID")!!
        userUID = arguments!!.getString("userUID")!!
        type = arguments!!.getString("type")!!
        val createBtn: Button = view.create_btn

        val toolbar: Toolbar = activity!!.findViewById<View>(R.id.toolbar) as Toolbar

        when(type){
            "projectsList" -> {
                courseCode = arguments!!.getString("courseCode")!!
                deptCode = arguments!!.getString("deptCode")!!
                createdProjectsList = arguments!!.getStringArrayList("createdProjectsList") as ArrayList<String?>
                joinedProjectsList = arguments!!.getStringArrayList("joinedProjectsList") as ArrayList<String?>
                readCourseData(view)
                view.create_btn.setOnClickListener {
                    showCreateProjectPopup(view)
                }
                createBtn.visibility = View.VISIBLE
                toolbar.title = arguments!!.getString("courseName")!!

            }
            "createdProjects" -> {
                createdProjectsList = arguments!!.getStringArrayList("createdProjectsList") as ArrayList<String?>
                findUserCreatedProjects(view)
                createBtn.visibility = View.INVISIBLE
                toolbar.title = "Created Projects"
            }
            "joinedProjects" -> {
                joinedProjectsList = arguments!!.getStringArrayList("joinedProjectsList") as ArrayList<String?>
                findUserJoinedProjects(view)
                toolbar.title = "Joined Projects"
                createBtn.visibility = View.INVISIBLE
            }
        }
        popupJoinProjectWindow = PopupWindow(this.context)
        popupCreateProjectWindow = PopupWindow(this.context)
        return view
    }

    
    private fun readCourseData(view: View){
        FirebaseDatabase.getInstance().reference
            .child("departments")
            .child(deptCode)
            .child("courses_list")
            .child(courseCode)
            .addValueEventListener(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        courseData = (p0.value as Map<String, Any>)["projects_list"] as ArrayList<Map<String, Any>?>?
                        numOfProject = if(courseData.isNullOrEmpty()){
                                            0
                                        }else{
                                            courseData!!.size
                                        }
                        courseData = courseData?.filterNotNull() as ArrayList<Map<String, Any>?>?
                        if(!courseData.isNullOrEmpty()){
                            initData()
                            initView(view)
                        }
                    }
                }
            })
    }

    private fun findUserJoinedProjects(view: View){
        courseData = ArrayList()
        joinedProjectsList.forEach {item ->
            FirebaseDatabase.getInstance().reference
                .child(item!!)
                .addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()){
                            courseData!!.add(p0.value as Map<String, Any>)

                            numOfProject = if(courseData.isNullOrEmpty()){
                                0
                            }else{
                                courseData!!.size
                            }
                            courseName = "Joined Projects"

                            if(!courseData.isNullOrEmpty()){
                                initData()
                                initView(view)
                            }
                        }else{
                            Log.d( "p0 not exists():", "Null")
                        }
                    }
                })
        }
    }

    private fun findUserCreatedProjects(view: View) {
        courseData = ArrayList()
        createdProjectsList.forEach {item ->
            FirebaseDatabase.getInstance().reference
                .child(item!!)
                .addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()){
                            courseData!!.add(p0.value as Map<String, Any>)
                            numOfProject = if(courseData.isNullOrEmpty()){
                                0
                            }else{
                                courseData!!.size
                            }
                            courseName = "Created Projects"
                            if(!courseData.isNullOrEmpty()){
                                initData()
                                initView(view)
                            }
                        }
                    }
                })
        }
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun showJoinPopupWindow(view: View, position: Int) {
        val popupView: View = LayoutInflater.from(this.context).inflate(R.layout.popup_project, null)
        popupJoinProjectWindow.contentView = popupView
        val joinBtn = popupView.findViewById(R.id.join_btn) as Button
        val createNameText: TextView = popupView.findViewById(R.id.creator_name) as TextView
        val numOfMember: TextView = popupView.findViewById(R.id.member_num) as TextView
        val memberListView: TextView = popupView.findViewById(R.id.member_name) as TextView
        val projDesc: TextView = popupView.findViewById(R.id.project_desc) as TextView
        val projectTitle: TextView = popupView.findViewById(R.id.projectTitle) as TextView
        val courseCode: TextView = popupView.findViewById(R.id.course_code) as TextView
        joinedStudentArray = courseData?.get(position)?.get("joined_students") as ArrayList<Map<String, Any?>?>?
        noJoinedStudent = joinedStudentArray.isNullOrEmpty() || joinedStudentArray!!.size == 0
        var memberList = ""
        var num = 0
        createNameText.text = courseData?.get(position)?.get("creator_name") as String + " (" +courseData?.get(position)?.get("creator_sid") as String +")"
        projectTitle.text = courseData?.get(position)?.get("project_title") as String
        courseCode.text = (courseData?.get(position)?.get("course_code") as String)
        projDesc.text = courseData?.get(position)?.get("project_desc") as String
        val index: Int?
        val path: String?
        val tempDeptCode: String = courseCode.text.substring(0, 4)
        val tempCourseCode: String = courseCode.text as String


        joinBtn.isEnabled = userSID != courseData?.get(position)?.get("creator_sid") as String

        if(noJoinedStudent){
            memberListView.text = "None"
        }else{
            num = joinedStudentArray!!.size
            if(num+1 == (courseData?.get(position)?.get("max_number") as Long).toInt()){
                joinBtn.isEnabled = false
            }
            for(i: Map<String, Any?>? in joinedStudentArray!!){
                memberList += i!!["student_name"].toString()+" ("+i["student_sid"].toString()+")\n"
                if(i["student_sid"] == userSID){
                    joinBtn.isEnabled = false
                }
            }
            memberListView.text = memberList
        }

        numOfMember.text = (num+1).toString()+" / "+courseData?.get(position)?.get("max_number")

        popupView.cancel_btn.setOnClickListener{
            popupJoinProjectWindow.dismiss()
        }

        var onClickListener: View.OnClickListener? = null

        when(type){
            "projectsList" -> {
                index = (courseData?.get(position)?.get("index") as Long).toInt()
                joinBtn.text = "Join"
                onClickListener = joinProjectClicked(num, position, index)
            }
            "createdProjects" -> {
                index = (courseData?.get(position)?.get("index") as Long).toInt()
                joinBtn.text = "Delete"
                joinBtn.isEnabled = true
                Log.d("path in courseData:", courseData?.toString()!!)
                path = "departments/$tempDeptCode/courses_list/$tempCourseCode/projects_list/$index"
                Log.d("Delete path:", path)
                onClickListener = deleteProjectClicked(path, position)
            }
            "joinedProjects" -> {
                joinBtn.text = "Quit"
                joinBtn.isEnabled = true
                path = joinedProjectsList[position]
                onClickListener = quitProjectClicked(position, path!!)
            }
        }

        popupView.join_btn.setOnClickListener(onClickListener)
        popupJoinProjectWindow.width = ViewGroup.LayoutParams.MATCH_PARENT
        popupJoinProjectWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupJoinProjectWindow.isOutsideTouchable = true
        popupJoinProjectWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private fun joinProjectClicked(num: Int, position: Int, index: Int): View.OnClickListener? {
        return View.OnClickListener {
            joinProject(index)
            popupJoinProjectWindow.dismiss()
        }
    }
    private fun quitProjectClicked(position: Int, path: String): View.OnClickListener? {
        return View.OnClickListener {
            joinedProjectsList.removeAt(position)

            FirebaseDatabase.getInstance().reference
                .child("users/$userUID/joined_projects/")
                .setValue(
                    addPathMap(joinedProjectsList)
                )

            FirebaseDatabase.getInstance().reference
                .child("$path/joined_students")
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()){
                            val tempList: ArrayList<Map<String, String>> = (p0.value as ArrayList<Map<String, String>>)
                            for((i, x) in tempList.withIndex()){
                                if(x["student_sid"] == userSID){
                                    tempList.removeAt(i)
                                    break
                                }
                            }
                            FirebaseDatabase.getInstance().reference
                                .child("$path/joined_students")
                                .setValue(tempList)
                        }
                    }
                })
            popupJoinProjectWindow.dismiss()

            Toast.makeText(this.context, "Quit project successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteProjectClicked(path: String, position: Int): View.OnClickListener? {
        return View.OnClickListener {
            FirebaseDatabase.getInstance().reference
                .child(path)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.ref.removeValue()
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

            var joinedMember = (courseData?.get(position)?.get("joined_students") as ArrayList<Map<String, Any>?>?)
            if(!joinedMember.isNullOrEmpty()){
                joinedMember = joinedMember.filterNotNull() as ArrayList<Map<String, Any>?>

                for(x in joinedMember) {
                    readUserData(x!!["student_uid"] as String)
                    val tempUID: String = x["student_uid"] as String
                    FirebaseDatabase.getInstance().reference
                        .child("users/$tempUID/joined_projects")
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }
                            override fun onDataChange(p0: DataSnapshot) {
                                if(p0.exists()){
                                    val tempList: ArrayList<Map<String, String>> = (p0.value as ArrayList<Map<String, String>>)
                                    for((i, value) in tempList.withIndex()){
                                        if(value["path"] == path){
                                            tempList.removeAt(i)
                                            break
                                        }
                                    }
                                    FirebaseDatabase.getInstance().reference
                                        .child("users/$tempUID/joined_projects/")
                                        .setValue(tempList)
                                }
                            }
                        })
                }
            }

            for((i, v) in createdProjectsList.withIndex()){
                if(v == path){
                    createdProjectsList.removeAt(i)
                    break
                }
            }

            FirebaseDatabase.getInstance().reference
                .child("users/$userUID/created_projects/")
                .setValue(
                    addPathMap(createdProjectsList)
                ).addOnSuccessListener {
                    Toast.makeText(this.context, "Deleted project successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.d("joinProject:", "Write failed!")
                }

            popupJoinProjectWindow.dismiss()

            Toast.makeText(this.context, "Delete project successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("InflateParams")
    private fun showCreateProjectPopup(view: View) {
        val popupView: View = LayoutInflater.from(this.context).inflate(R.layout.popup_create_project, null)
        popupCreateProjectWindow.contentView = popupView
        val projectTitle: EditText = popupView.findViewById(R.id.project_title) as EditText
        val projectDesc: EditText = popupView.findViewById(R.id.project_desc) as EditText
        val maxNum: EditText = popupView.findViewById(R.id.max_num) as EditText

        popupView.cancel_btn.setOnClickListener{
            popupCreateProjectWindow.dismiss()
        }
        val path = "departments/$deptCode/courses_list/$courseCode/projects_list/$numOfProject"
        popupView.create_btn.setOnClickListener{
            if(maxNum.text.toString() != "" && projectTitle.text.toString() != "" && projectDesc.text.toString() != ""){
                val newProject = Project(
                    userName,
                    userSID,
                    projectTitle.text.toString(),
                    projectDesc.text.toString(),
                    maxNum.text.toString().toInt(),
                    courseCode,
                    numOfProject
                )
                val newProjectValues: Map<String, Any?> = newProject.toMap()

                val childUpdates = HashMap<String, Any>()
                childUpdates[path] = newProjectValues

                FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)


                createdProjectsList.add(path)
                FirebaseDatabase.getInstance().reference
                    .child("users/$userUID/created_projects/")
                    .setValue(
                        addPathMap(createdProjectsList)
                    ).addOnSuccessListener {
                        Toast.makeText(this.context, "Create project successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Log.d("joinProject:", "Write failed!")
                    }
                popupCreateProjectWindow.dismiss()
            }else{
                Toast.makeText(this.context, "Items cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        popupCreateProjectWindow.isFocusable = true
        popupCreateProjectWindow.update()
        popupCreateProjectWindow.width = ViewGroup.LayoutParams.MATCH_PARENT
        popupCreateProjectWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupCreateProjectWindow.isOutsideTouchable = true
        popupCreateProjectWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private fun joinProject(index: Int){
        if(noJoinedStudent){
            joinedStudentArray = arrayListOf<Map<String, Any?>>(mapOf(
                "student_name" to userName,
                "student_sid" to userSID,
                "student_uid" to userUID
            )) as ArrayList<Map<String, Any?>?>?
        }else{
            joinedStudentArray?.add(
                mapOf(
                    "student_name" to userName,
                    "student_sid" to userSID,
                    "student_uid" to userUID
                )
            )
        }

        FirebaseDatabase.getInstance().reference
            .child("departments/$deptCode/courses_list/$courseCode/projects_list/$index/joined_students")
            .setValue(
                joinedStudentArray
            )

        joinedProjectsList.add( "departments/$deptCode/courses_list/$courseCode/projects_list/$index")

        FirebaseDatabase.getInstance().reference
            .child("users/$userUID/joined_projects/")
            .setValue(
                addPathMap(joinedProjectsList)
            )

        Toast.makeText(this.context, "Join project successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun addPathMap(joinedProjectsList: ArrayList<String?>): ArrayList<Map<String, String>?>{
        val joinedProjectsListMap: ArrayList<Map<String, String>?> = ArrayList()

        joinedProjectsList.forEach {
            joinedProjectsListMap.add(mapOf(
                "path" to it!!
            ))
        }

        return joinedProjectsListMap
    }

    override fun onItemClick(view: View, position: Int){
        showJoinPopupWindow(view, position)
    }

    private fun readUserData(uid: String){
        FirebaseDatabase.getInstance().reference
            .child("uid")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        requiredUserData.add(mapOf(
                            uid to p0.value as Map<String, Map<String, Any>?>
                        ))
                    }
                }
            })
    }
}
