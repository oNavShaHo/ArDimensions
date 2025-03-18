package com.example.measuredimensions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.core.Point
import com.google.ar.core.Plane
import com.google.ar.core.Session
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.ARSession

class MainActivity : ComponentActivity() {

    private lateinit var sceneView: ARSceneView
    private lateinit var distanceTextView: TextView

    private var bottomPoint: Pose? = null
    private var topPoint: Pose? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ARSceneView and TextView
        sceneView = findViewById(R.id.sceneView)
        distanceTextView = findViewById(R.id.distanceTextView)
 
        // Configure ARSession
        sceneView.sessionConfiguration = { session: Session, config: Config ->
            config.depthMode = if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                Config.DepthMode.AUTOMATIC
            } else {
                Config.DepthMode.DISABLED
            }
        }

        // Set touch listener for placing points
        sceneView.setOnTouchListener { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Perform hit test using hitTestAR
                val hitResult: HitResult? = sceneView.hitTestAR(
                    xPx = event.x,
                    yPx = event.y,
                    planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING, Plane.Type.VERTICAL),
                    point = true,
                    depthPoint = true,
                    trackingStates = setOf(TrackingState.TRACKING)
                )
                hitResult?.let {
                    val hitPose = it.hitPose
                    if (bottomPoint == null) {
                        // Set bottom point
                        bottomPoint = hitPose
                        Toast.makeText(this, "Bottom point placed", Toast.LENGTH_SHORT).show()
                    } else if (topPoint == null) {
                        // Set top point and calculate height
                        topPoint = hitPose
                        Toast.makeText(this, "Top point placed", Toast.LENGTH_SHORT).show()

                        val height = calculateHeight(bottomPoint!!, topPoint!!)
                        distanceTextView.text = "Height: $height meters"
                    }
                }
            }
            true
        }
    }

    // Function to calculate the distance between two Pose points
    private fun calculateHeight(bottomPose: Pose, topPose: Pose): Float {
        val dx = topPose.tx() - bottomPose.tx()
        val dy = topPose.ty() - bottomPose.ty()
        val dz = topPose.tz() - bottomPose.tz()

        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    override fun onResume() {
        super.onResume()
        sceneView.onSessionResumed
    }

    override fun onPause() {
        super.onPause()
        sceneView.onSessionPaused
    }
}
