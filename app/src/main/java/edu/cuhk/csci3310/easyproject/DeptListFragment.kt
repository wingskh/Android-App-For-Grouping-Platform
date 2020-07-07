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

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.ArrayList

class DeptListFragment internal constructor(
) : Fragment(), RecyclerViewAdapter.OnItemClickListener {
    private var adapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var type: String? = null
    private var userName: String? = null
    private var userSID: String? = null
    private var userUID: String? = null
    private var joinedProjectsList: ArrayList<String?> = ArrayList()
    private var createdProjectsList: ArrayList<String?> = ArrayList()
    private lateinit var courseListFragment: DeptListFragment
    private lateinit var projectsListFragment: ProjectsListFragment
    private var itemsList: ArrayList<ArrayList<String>> = ArrayList()


    private fun initData() {
        layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        adapter = RecyclerViewAdapter(itemsList, this)
    }

    private fun initView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.items_list)
        if(this.context == null){
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

    private fun readCSV(csvFile: Int, code: String? = null) {
        itemsList = ArrayList()
        val inputStream = this.resources.openRawResource(csvFile)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        try {
            bufferedReader.readLine()
            var itemDetail: Array<String>

            val lineList = bufferedReader.lines()
            lineList.forEach{ it ->
                itemDetail = it.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if(csvFile==R.raw.course_list){
                    Log.d("itemDetail[0].substring(0, 3): ", itemDetail[1])
                    Log.d("itemDetail[0].substring(0, 3): ", code!!)
                }
                if(code == null || itemDetail[2] == code){
                    itemsList.add(ArrayList(listOf(*itemDetail)))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.department_list_view, container, false )
        type = arguments!!.getString("type")
        userName = arguments!!.getString("userName")!!
        userSID = arguments!!.getString("userSID")!!
        userUID = arguments!!.getString("userUID")!!
        joinedProjectsList = arguments!!.getStringArrayList("joinedProjectsList")!!
        createdProjectsList = arguments!!.getStringArrayList("createdProjectsList")!!

        when(type){
            "department" -> {
                readCSV(R.raw.department_list)
                val toolbar = activity!!.findViewById<View>(R.id.toolbar) as Toolbar
                toolbar.title = "Department"
            }
            "course" -> {
                readCSV(R.raw.course_list, arguments!!.getString("courseCode"))
                val toolbar = activity!!.findViewById<View>(R.id.toolbar) as Toolbar
                toolbar.title = arguments!!.getString("deptName")!!
            }else -> {
                readCSV(R.raw.department_list)
            }
        }
        Log.d("Department type: ", type!!)
        Log.d("Department itemsList: ", (itemsList.size).toString())
        initData()
        initView(view)
        return view
    }

    override fun onItemClick(position: Int) {
        if(type == "department") {
            val args = Bundle()
            args.putString("type", "course")
            args.putString("courseCode", itemsList[position][0])
            args.putString("deptName", itemsList[position][1])
            args.putString("userName", userName)
            args.putString("userSID", userSID)
            args.putString("userUID", userUID)
            args.putStringArrayList("createdProjectsList", createdProjectsList)
            args.putStringArrayList("joinedProjectsList", joinedProjectsList)

            courseListFragment = DeptListFragment()
            courseListFragment.arguments = args

            fragmentManager!!
                .beginTransaction()
                .replace(R.id.func_fragment_main, courseListFragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }else{
            val args = Bundle()
            args.putString("courseCode", itemsList[position][0])
            args.putString("deptCode", itemsList[position][2])
            args.putString("courseName", itemsList[position][1])
            args.putString("userName", userName)
            args.putString("userSID", userSID)
            args.putString("userUID", userUID)
            args.putString("type", "projectsList")
            args.putStringArrayList("createdProjectsList", createdProjectsList)
            args.putStringArrayList("joinedProjectsList", joinedProjectsList)
            projectsListFragment = ProjectsListFragment()
            projectsListFragment.arguments = args

            fragmentManager!!
                .beginTransaction()
                .replace(R.id.func_fragment_main, projectsListFragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()

        }
    }
}
