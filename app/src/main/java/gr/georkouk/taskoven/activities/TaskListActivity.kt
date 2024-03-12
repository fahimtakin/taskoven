package gr.georkouk.taskoven.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import gr.georkouk.taskoven.R
import gr.georkouk.taskoven.adapters.TaskListItemsAdapter
import gr.georkouk.taskoven.databinding.ActivityTaskListBinding
import gr.georkouk.taskoven.firebase.FirestoreClass
import gr.georkouk.taskoven.models.Board
import gr.georkouk.taskoven.models.Card
import gr.georkouk.taskoven.models.Task
import gr.georkouk.taskoven.models.User
import gr.georkouk.taskoven.utils.Constants

class TaskListActivity : BaseActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private lateinit var mBoardDocumentId: String
    private lateinit var mBoardDetails: Board
    lateinit var mAssignedMembersDetailList: ArrayList<User>


    companion object {
        const val MEMBERS_REQUEST_CODE: Int = 13

        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize(){
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }


        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        binding.toolbarTaskListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && (requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE)) {

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
        }
        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * A function to get the result of Board Detail.
     */
    fun boardDetails(board: Board) {
        mBoardDetails = board

        hideProgressDialog()

        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this@TaskListActivity, mBoardDetails.assignedTo)
    }

    /**
     * A function to get the task list name from the adapter class which we will be using
     * to create a new task list in the database.
     */
    fun createTaskList(taskListName: String) {
        Log.e("Task List Name", taskListName)

        // Create and Assign the task details
        val task = Task(taskListName, FirestoreClass().getCurrentUserID())

        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    /**
     * A function to update the taskList
     */
    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    /**
     * A function to delete the task list from database.
     */
    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    /**
     * A function to get the result of add or updating the task list.
     */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)
    }

    /**
     * A function to create a card and update it in the task list.
     */
    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())

        val card = Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    /**
     * A function for viewing and updating card details.
     */
    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    /**
     * A function to get assigned members detail list.
     */
    fun boardMembersDetailList(list: ArrayList<User>) {
        mAssignedMembersDetailList = list

        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        binding.rvTaskList.adapter = adapter
    }

    /**
     * A function to update the card list in the particular task list.
     */
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        mBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

}