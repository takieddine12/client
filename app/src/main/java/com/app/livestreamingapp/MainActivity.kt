package com.app.livestreamingapp


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQ_ID = 22
    }
    //private lateinit var remoteContainer : FrameLayout
    private lateinit var surfaceView : SurfaceView
    private lateinit var startCall : ImageView
    private lateinit var endCall : ImageView
    private lateinit var container : FrameLayout
    private lateinit var remoteVideoContainer : FrameLayout
    private val appId = "c24451635a5144aa85101bb7e211faee"
    private val channelName = "security"
    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Monitor remote users in the channel and obtain their uid
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { // After obtaining uid, set up the remote video view
                setupRemoteVideo(uid)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        startCall = findViewById(R.id.startCall)
        endCall = findViewById(R.id.endCall)
        container = findViewById(R.id.local_video_view_container)
        surfaceView = SurfaceView(baseContext)
        //remoteSurfaceView = SurfaceView(baseContext)
        startCall.setOnClickListener {
            if (checkPermissions()) {
                initialize()
            } else {
                ActivityCompat.requestPermissions(this, getRequiredPermissions()!!, PERMISSION_REQ_ID);
            }
        }
        endCall.setOnClickListener {
            if (mRtcEngine != null){
                leaveChannel()
                startCall.visibility = View.VISIBLE
                endCall.visibility = View.GONE

            }
        }

    }

    private fun initialize() {
        try {
            // Create an RtcEngineConfig instance and configure it
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            // Create and initialize an RtcEngine instance
            mRtcEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            throw RuntimeException("Check the error.")
        }


        mRtcEngine!!.enableVideo()

        // Enable local preview
        mRtcEngine!!.startPreview()
        // Create a SurfaceView object and make it a child object of FrameLayout
        container.addView(surfaceView)
        // Pass the SurfaceView object to the SDK and set the local view
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
        // Create an instance of ChannelMediaOptions and configure it
        val options = ChannelMediaOptions()
        // Set the user role to BROADCASTER or AUDIENCE according to the scenario
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        // In the video calling scenario, set the channel profile to CHANNEL_PROFILE_COMMUNICATION
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        // Use the temporary token to join the channel
        // Specify the user ID yourself and ensure it is unique within the channel
        mRtcEngine?.joinChannel(appId, channelName, System.currentTimeMillis().toInt(), options)
        startCall.visibility = View.INVISIBLE
        endCall.visibility = View.VISIBLE

    }


    private fun leaveChannel(){
        mRtcEngine?.leaveChannel()
        container.removeView(surfaceView)
    }

    private fun setupRemoteVideo(uid: Int) {
//        remoteSurfaceView.setZOrderMediaOverlay(true)
//        remoteVideoContainer.addView(remoteSurfaceView)
//        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
//        startCall.visibility = View.INVISIBLE
//        endCall.visibility = View.VISIBLE
    }

    private fun getRequiredPermissions(): Array<String>? {
        // Determine the permissions required when targetSDKVersion is 31 or above
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf<String>(
                android.Manifest.permission.RECORD_AUDIO,  // Recording permission
                android.Manifest.permission.CAMERA,  // Camera permission
                android.Manifest.permission.READ_PHONE_STATE,  // Permission to read phone status
                android.Manifest.permission.BLUETOOTH_CONNECT // Bluetooth connection permission
            )
        } else {
            arrayOf<String>(
                android.Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        }
    }

    private fun checkPermissions(): Boolean {
        for (permission in getRequiredPermissions()!!) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop local video preview
        mRtcEngine!!.stopPreview()
        // Leave the channel
        mRtcEngine!!.leaveChannel()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID && grantResults.isNotEmpty()){

        } else {
            Toast.makeText(this,"Please grant permissions",Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, getRequiredPermissions()!!, PERMISSION_REQ_ID)
        }
    }



}