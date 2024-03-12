package gr.georkouk.taskoven.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gr.georkouk.taskoven.R
import gr.georkouk.taskoven.databinding.ActivitySignUpBinding
import gr.georkouk.taskoven.firebase.FirestoreClass
import gr.georkouk.taskoven.models.User

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setSupportActionBar(binding.toolbarSignUpActivity)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnSignUp.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val name: String = binding.etName.text.toString().trim { it <= ' ' }
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!

                        val user = User(firebaseUser.uid, name, registeredEmail)

                        // call the registerUser function of FirestoreClass to make an entry in the database.
                        FirestoreClass().registerUser(this@SignUpActivity, user)
                    }
                    else {
                        Toast.makeText(
                            this@SignUpActivity,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            else -> {
                true
            }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this@SignUpActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        hideProgressDialog()

        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()

        finish()
    }

}