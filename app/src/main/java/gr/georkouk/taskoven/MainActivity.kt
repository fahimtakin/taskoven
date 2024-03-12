package gr.georkouk.taskoven

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import gr.georkouk.taskoven.activities.BaseActivity
import gr.georkouk.taskoven.activities.CreateBoardActivity
import gr.georkouk.taskoven.activities.IntroActivity
import gr.georkouk.taskoven.activities.MyProfileActivity
import gr.georkouk.taskoven.activities.TaskListActivity
import gr.georkouk.taskoven.adapters.BoardItemsAdapter
import gr.georkouk.taskoven.databinding.ActivityMainBinding
import gr.georkouk.taskoven.firebase.FirestoreClass
import gr.georkouk.taskoven.models.Board
import gr.georkouk.taskoven.models.User
import gr.georkouk.taskoven.utils.Constants

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mUserName: String

    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize(){
        setSupportActionBar(binding.included.toolbarMainActivity)
        binding.included.toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        binding.included.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }

        binding.navView.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        // Variable is used get the value either token is updated in the database or not.
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this@MainActivity, true)
        }
        else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                updateFCMToken(token)
            })

//            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity) {
//                instanceIdResult ->
//                updateFCMToken(instanceIdResult.token)
//            }
        }

        binding.included.fabCreateBoard.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        else {
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this@MainActivity)
        }
        else if (resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardsList(this@MainActivity)
        }
        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this@MainActivity, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    /**
     * A function to get the current user details from firebase.
     */
    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()

        mUserName = user.name

        val headerView = binding.navView.getHeaderView(0)
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image)

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage)

        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        navUsername.text = user.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this@MainActivity)
        }
    }

    /**
     * A function to update the user's FCM token into the database.
     */
    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this@MainActivity, userHashMap)
    }

    /**
     * A function to populate the result of BOARDS list in the UI i.e in the recyclerView.
     */
    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        if (boardsList.size > 0) {
            binding.included.mainContentInclude.rvBoardsList.visibility = View.VISIBLE
            binding.included.mainContentInclude.tvNoBoardsAvailable.visibility = View.GONE

            binding.included.mainContentInclude.rvBoardsList.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.included.mainContentInclude.rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)
            binding.included.mainContentInclude.rvBoardsList.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }
        else {
            binding.included.mainContentInclude.rvBoardsList.visibility = View.GONE
            binding.included.mainContentInclude.tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }

    /**
     * A function to notify the token is updated successfully in the database.
     */
    fun tokenUpdateSuccess() {
        hideProgressDialog()

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this@MainActivity, true)
    }

}