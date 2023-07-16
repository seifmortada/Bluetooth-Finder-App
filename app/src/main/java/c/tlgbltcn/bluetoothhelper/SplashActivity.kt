package c.tlgbltcn.bluetoothhelper

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT = 1000L // 1.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the splash screen layout as the content view
        setContentView(R.layout.activity_splash)

        // Get the ImageView reference
        val imageView = findViewById<ImageView>(R.id.imageView3)

        // Load the animation
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_rotate)

        // Set the animation listener
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // Delay for some time after the animation completes before moving to the main activity
                Handler().postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }, SPLASH_TIME_OUT + animation.duration)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        // Start the animation on the ImageView
        imageView.startAnimation(animation)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Hide the status bar and enable full-screen immersive mode
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }
}
