package com.example.proxy

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.gson.Gson
import com.google.gson.JsonObject

import org.json.JSONArray
import org.json.JSONException
import org.json.simple.JSONObject

import java.io.IOException
import java.io.InputStreamReader


open class ApplicationStart : Application(), Application.ActivityLifecycleCallbacks {
    companion object {

        const val TAG = "ApplicationStart"

    }

    val FILE_NAME: String = "File-name"
    val READ_BLOCK_SIZE = 10000

    override fun onCreate() {
        super.onCreate()
        println("Notification onCreatetaeAxiaAplication")
        registerActivityLifecycleCallbacks(this)
        AndroidNetworking.initialize(getApplicationContext());
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {


    }


    override fun onActivityStarted(p0: Activity) {
        Log.i(TAG, "onActivityCreated $p0")
        val win: Window = p0.window
        val localCallback: Window.Callback = win.callback
        win.callback = MyWindowCallback(localCallback, p0)
        (win.callback as MyWindowCallback).writeFileFirstTime()


    }

    override fun onActivityResumed(p0: Activity) {
        Log.i(TAG, "onActivityResumed $p0")

    }

    override fun onActivityPaused(p0: Activity) {
        Log.i(TAG, "onActivityPaused $p0")

    }

    override fun onActivityStopped(p0: Activity) {
        Log.i(TAG, "onActivityStopped $p0")


        val jsonObject = org.json.JSONObject()
        try {

            jsonObject.put("Content", ReadJsonOnMouseEvent(p0))
            jsonObject.put("LiveTestCaseName", packageName)
            jsonObject.put("TestResult", 0);
        } catch (e: JSONException) {
            e.printStackTrace()
        }



        AndroidNetworking.post("http://api-sofy-test.azurewebsites.net/api/Applications/1d167171-31ef-4d1f-8c33-0ed6394ac1ac/CreateLiveTestCaseAndTestRun")
            .addJSONObjectBody(jsonObject) // posting json
            .setTag("test")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("SofyAuth", "67935C5B-A009-4DC8-9EC6-2F8E9D58E6DC")

            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    // do anything with response
                    Log.i(TAG, "Success")
                }

                override fun onError(error: ANError) {
                    // handle error
                    Log.i(TAG, "Error $error ")
                }
            })

    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityDestroyed(p0: Activity) {

    }

    private fun ReadJsonOnMouseEvent(activity: Activity): String {
        var convertedObject: JsonObject? = null
        var s: String? = ""
        try {
            var fileInputStream = activity.openFileInput(FILE_NAME)
            val inputBuffer = CharArray(READ_BLOCK_SIZE)
            val InputRead = InputStreamReader(fileInputStream)

            var charRead: Int = 0
            while (InputRead.read(inputBuffer).also { charRead = it } > 0) {
                // char to string conversion
                val readstring = String(inputBuffer, 0, charRead)
                s += readstring
                convertedObject = Gson().fromJson(s, JsonObject::class.java)
            }

            InputRead.close()
            println("Input Read json as string$s")
            println("Input convertedObject ${convertedObject.toString()}")


        } catch (e: IOException) {
            e.printStackTrace();
        } finally {



            return  convertedObject.toString()
        }
    }
}
