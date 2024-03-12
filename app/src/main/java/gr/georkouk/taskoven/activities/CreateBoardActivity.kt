package gr.georkouk.taskoven.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import gr.georkouk.taskoven.R
import gr.georkouk.taskoven.databinding.ActivityCreateBoardBinding
import gr.georkouk.taskoven.firebase.FirestoreClass
import gr.georkouk.taskoven.models.Board
import gr.georkouk.taskoven.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateBoardBinding
    private lateinit var mUserName: String
    private var mSelectedImageFileUri: Uri? = null
    private var mBoardImageURL: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize(){
        setSupportActionBar(binding.toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding.toolbarCreateBoardActivity.setNavigationOnClickListener { onBackPressed() }

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding.ivBoardImage.setOnClickListener { view ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@CreateBoardActivity)
            }
            else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding.btnCreate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))

                createBoard()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@CreateBoardActivity)
            }
            else {
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage. You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null) {
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@CreateBoardActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivBoardImage)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createBoard() {
        //  A list is created to add the assigned menu_members.
        //  This can be modified later on as of now the user itself will be the member of the board.
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        val board = Board(
            binding.etBoardName.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        FirestoreClass().createBoard(this@CreateBoardActivity, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(this@CreateBoardActivity, mSelectedImageFileUri)
        )

        sRef.putFile(mSelectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.e(
                    "Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        mBoardImageURL = uri.toString()

                        createBoard()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@CreateBoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

}