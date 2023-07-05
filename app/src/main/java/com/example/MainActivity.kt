package com.example

import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.getryttask.R
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mUsageStatsManager: UsageStatsManager? = null
    private var mUsageListAdapter: UsageListAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var progressBar: ProgressBar

    private val completedJob = Job()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + completedJob)
    private val foregroundScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (completedJob.isActive){
            completedJob.cancel()
        }
    }

    private fun initViews() {
        supportActionBar!!.title = getString(R.string.weekly_apps_usage)
        mUsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        progressBar = findViewById(R.id.progress_bar)
        mRecyclerView = findViewById(R.id.recyclerview_app_usage)
        mLayoutManager = mRecyclerView!!.layoutManager
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            progressBar.visibility = View.VISIBLE
            runInBackground()
        } else {
            showDialog()
        }

    }

    private fun runInBackground() {
        try {
            backgroundScope.launch {
                getUsageStatistics()
            }
        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }

    private fun showDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setCancelable(false)
        alertDialog.setTitle(getString(R.string.permission_requires))
        alertDialog.setMessage(getString(R.string.permission_note))
        alertDialog.setPositiveButton(getString(R.string.enable_permission)) { dialog, _ ->
            dialog.cancel()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        alertDialog.setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
            dialog.cancel()
            finish()
        }
        alertDialog.create().show()
    }

    private fun checkPermission(): Boolean {
        return try {
            val packageManager = packageManager
            val applicationInfo: ApplicationInfo =
                packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getUsageStatistics() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_WEEK, -7)
        val queryUsageStats = mUsageStatsManager!!.queryUsageStats(
            UsageStatsManager.INTERVAL_WEEKLY, cal.timeInMillis, System.currentTimeMillis()
        )
        if (queryUsageStats.size == 0) {
            Toast.makeText(
                this,
                getString(R.string.explanation_access_to_appusage_is_not_enabled),
                Toast.LENGTH_LONG
            ).show()
        }
        queryUsageStats.sortWith { p0, p1 -> p1!!.totalTimeInForeground.compareTo(p0!!.totalTimeInForeground) }
        foregroundScope.launch {
            progressBar.visibility = View.GONE
            mUsageListAdapter = UsageListAdapter(queryUsageStats)
            mRecyclerView!!.adapter = mUsageListAdapter
        }
    }

}