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

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Project(
    var creatorName: String? = "",
    var creatorSID: String? = "",
    var projectTitle: String? = "",
    var projectDesc: String? = "",
    var maxNum: Int? = 1,
    var courseCode: String? = "",
    var numOfProject: Int? = 0
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "creator_name" to creatorName,
            "creator_sid" to creatorSID,
            "joined_students" to arrayListOf<Map<String, Any>?>(),
            "max_number" to maxNum,
            "project_desc" to projectDesc,
            "project_title" to projectTitle,
            "course_code" to courseCode,
            "index" to numOfProject
        )
    }
}