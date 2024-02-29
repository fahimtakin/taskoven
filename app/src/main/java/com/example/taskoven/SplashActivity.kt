package com.example.taskoven

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        val mTextView = findViewById<TextView>(R.id.tv_app_name)

        val typeFace: Typeface = Typeface.createFromAsset(assets, "CrazyJazzy.ttf")

        mTextView.typeface = typeFace


    }
}