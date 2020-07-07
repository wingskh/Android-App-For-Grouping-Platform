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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import java.util.ArrayList

@Suppress("UNCHECKED_CAST")
class ProjectViewAdapter internal constructor(
    private val ProjectList:  ArrayList<Map<String, Any>>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ProjectViewAdapter.ViewHolder>() {
    private var joinedStudents: ArrayList<Map<String, String>>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.project_item, parent, false)
        return ViewHolder(v, onItemClickListener)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("onBindViewHolder ProjectList: ", ProjectList.toString())
        Log.d("onBindViewHolder size: ", ProjectList.size.toString())
        Log.d("onBindViewHolder position: ", position.toString())
        Log.d("onBindViewHolder ProjectList[position]: ", ProjectList[position].toString())
        joinedStudents = ProjectList[position]["joined_students"] as ArrayList<Map<String, String>>?
        holder.projectTitle.text = ProjectList[position]["project_title"].toString()
        holder.courseCode.text = ProjectList[position]["course_code"].toString()
        holder.courseCode.text = holder.courseCode.text.substring(0, 4)+"\n"+holder.courseCode.text.substring(4, holder.courseCode.text.length)
        holder.numView.text = if(joinedStudents.isNullOrEmpty()) "1/"+ProjectList[position]["max_number"]
                                else (joinedStudents!!.size+1).toString()+"/"+ProjectList[position]["max_number"]
    }

    override fun getItemCount(): Int {
        return ProjectList.size
    }

    class ViewHolder internal constructor(
        itemView: View,
        private var onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var projectTitle: TextView = itemView.findViewById(R.id.projectTitle)
        internal var courseCode: TextView = itemView.findViewById(R.id.courseCode)
        internal var numView: TextView = itemView.findViewById(R.id.numView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onItemClickListener.onItemClick(view, adapterPosition)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view:View, position: Int)
    }
}