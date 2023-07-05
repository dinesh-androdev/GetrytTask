/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example

import android.app.usage.UsageStats
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.getryttask.R
import java.util.*
import kotlin.collections.ArrayList

class UsageListAdapter(private val mCustomUsageStatsList:List<UsageStats>) : RecyclerView.Adapter<UsageListAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val packageName: TextView
        val lastTimeUsed: TextView

        init {
            packageName = v.findViewById<View>(R.id.package_name_tv) as TextView
            lastTimeUsed = v.findViewById<View>(R.id.last_time_used_tv) as TextView
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.usage_row, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.packageName.text = mCustomUsageStatsList[position].packageName
        viewHolder.lastTimeUsed.text = convertLongToTimeChar(mCustomUsageStatsList[position].totalTimeInForeground)
    }

    override fun getItemCount(): Int {
        return mCustomUsageStatsList.size
    }

    private fun convertLongToTimeChar(usedTime: Long): String? {
        var hour = ""
        var min = ""
        var sec = ""
        val h = (usedTime / 1000 / 60 / 60).toInt()
        if (h != 0) hour = h.toString() + "h "
        val m = (usedTime / 1000 / 60 % 60).toInt()
        if (m != 0) min = m.toString() + "m "
        val s = (usedTime / 1000 % 60).toInt()
        sec = if (s == 0 && (h != 0 || m != 0)) "" else s.toString() + "s"
        return hour + min + sec
    }
}