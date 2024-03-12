package gr.georkouk.taskoven.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.WindowManager
import gr.georkouk.taskoven.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.tvAppNameIntro.typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")

        binding.btnSignInIntro.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

        binding.btnSignUpIntro.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
    }

}