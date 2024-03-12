package gr.georkouk.taskoven.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import gr.georkouk.taskoven.MainActivity
import gr.georkouk.taskoven.databinding.ActivitySplashBinding
import gr.georkouk.taskoven.firebase.FirestoreClass


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.tvAppName.typeface = Typeface.createFromAsset(assets, "CrazyJazzy.ttf")

        //elegxos an xreiazetai login
        Handler().postDelayed({
            val currentUserID = FirestoreClass().getCurrentUserID()

            if (currentUserID.isNotEmpty()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            else {
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish()
        }, 2500)

    }

}