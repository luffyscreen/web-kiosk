package org.screenlite.webkiosk.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import org.screenlite.webkiosk.service.StayOnTopService

class KioskApplication : Application() {
    private var resumedActivities = 0

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                Log.d("KioskApplication", "Applied KEEP_SCREEN_ON to ${activity.localClassName}")
            }

            override fun onActivityResumed(activity: Activity) {
                if (++resumedActivities == 1) {
                    StayOnTopService.isActivityVisible = true
                    Log.i("KioskApplication", "${activity.localClassName} resumed → App visible")
                } else {
                    Log.i("KioskApplication", "${activity.localClassName} resumed → resumedActivities=$resumedActivities")
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (--resumedActivities == 0) {
                    StayOnTopService.isActivityVisible = false
                    Log.i("KioskApplication", "${activity.localClassName} paused → App NOT visible")
                } else {
                    Log.i("KioskApplication", "${activity.localClassName} paused → resumedActivities=$resumedActivities")
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
