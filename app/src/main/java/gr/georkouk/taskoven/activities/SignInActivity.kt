package gr.georkouk.taskoven.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import gr.georkouk.taskoven.MainActivity
import gr.georkouk.taskoven.R
import gr.georkouk.taskoven.databinding.ActivitySignInBinding
import gr.georkouk.taskoven.firebase.FirestoreClass
import gr.georkouk.taskoven.models.User

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setSupportActionBar(binding.toolbarSignInActivity)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.btnSignIn.setOnClickListener { signInRegisteredUser() }
    }

    private fun signInRegisteredUser() {
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirestoreClass().loadUserData(this@SignInActivity)
                    }
                    else {
                        Toast.makeText(
                            this@SignInActivity,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    /**
     * A function to validate the entries of a user.
     */
    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please enter email.")
            false
        }
        else if (TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please enter password.")
            false
        }
        else {
            true
        }
    }

    /**
     * A function to get the user details from the firestore database after authentication.
     */
    fun signInSuccess(user: User) {
        hideProgressDialog()

        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        this.finish()
    }

}