package gr.georkouk.taskoven.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import gr.georkouk.taskoven.activities.TaskListActivity
import gr.georkouk.taskoven.databinding.ItemTaskBinding
import gr.georkouk.taskoven.models.Task
import java.util.*


open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)

        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams = layoutParams

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            if (position == list.size - 1) {
                holder.tvAddTaskList.visibility = View.VISIBLE
                holder.llTaskItem.visibility = View.GONE
            }
            else {
                holder.tvAddTaskList.visibility = View.GONE
                holder.llTaskItem.visibility = View.VISIBLE
            }

            holder.tvTaskListTitle.text = model.title

            holder.tvAddTaskList.setOnClickListener {
                holder.tvAddTaskList.visibility = View.GONE
                holder.cvAddTaskListName.visibility = View.VISIBLE
            }

            holder.ibCloseListName.setOnClickListener {
                holder.tvAddTaskList.visibility = View.VISIBLE
                holder.cvAddTaskListName.visibility = View.GONE
            }

            holder.ibDoneListName.setOnClickListener {
                val listName = holder.etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                }
                else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            holder.ibEditListName.setOnClickListener {
                holder.etEditTaskListName.setText(model.title)
                holder.llTitleView.visibility = View.GONE
                holder.cvEditTaskListName.visibility = View.VISIBLE
            }

            holder.ibCloseEditableView.setOnClickListener {
                holder.llTitleView.visibility = View.VISIBLE
                holder.cvEditTaskListName.visibility = View.GONE
            }

            holder.ibDoneEditListName.setOnClickListener {
                val listName = holder.etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                }
                else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            holder.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            holder.tvAddCard.setOnClickListener {
                holder.tvAddCard.visibility = View.GONE
                holder.cvAddCard.visibility = View.VISIBLE

                holder.ibCloseCardName.setOnClickListener {
                    holder.tvAddCard.visibility = View.VISIBLE
                    holder.cvAddCard.visibility = View.GONE
                }

                holder.ibDoneCardName.setOnClickListener {
                    val cardName = holder.etCardName.text.toString()
                    if (cardName.isNotEmpty()) {
                        if (context is TaskListActivity) {
                            context.addCardToTaskList(position, cardName)
                        }
                    }
                    else {
                        Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            holder.rvCardList.layoutManager = LinearLayoutManager(context)
            holder.rvCardList.setHasFixedSize(true)

            val adapter = CardListItemsAdapter(context, model.cards)
            holder.rvCardList.adapter = adapter

            adapter.setOnClickListener(object :
                CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        context.cardDetails(position, cardPosition)
                    }
                }
            })

            val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            holder.rvCardList.addItemDecoration(dividerItemDecoration)

            //  Creates an ItemTouchHelper that will work with the given Callback.
            val helper = ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: ViewHolder,
                    target: ViewHolder): Boolean {

                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition

                    Collections.swap(list[position].cards, draggedPosition, targetPosition)

                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    return false
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {

                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
                    super.clearView(recyclerView, viewHolder)

                    if (mPositionDraggedFrom != -1
                            && mPositionDraggedTo != -1
                            && mPositionDraggedFrom != mPositionDraggedTo) {

                        (context as TaskListActivity).updateCardsInTaskList(position, list[position].cards)
                    }

                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
            })

            helper.attachToRecyclerView(holder.rvCardList)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    class MyViewHolder(binding: ItemTaskBinding) : ViewHolder(binding.root){
        var rvCardList = binding.rvCardList
        var tvAddTaskList = binding.tvAddTaskList
        var tvTaskListTitle = binding.tvTaskListTitle
        var etCardName = binding.etCardName
        var etTaskListName = binding.etTaskListName
        var ibDoneCardName = binding.ibDoneCardName
        var tvAddCard = binding.tvAddCard
        var cvAddCard = binding.cvAddCard
        var ibCloseCardName = binding.ibCloseCardName
        var ibDeleteList = binding.ibDeleteList
        var llTaskItem = binding.llTaskItem
        var llTitleView = binding.llTitleView
        var cvAddTaskListName = binding.cvAddTaskListName
        var ibCloseListName = binding.ibCloseListName
        var ibDoneListName = binding.ibDoneListName
        var ibEditListName = binding.ibEditListName
        var etEditTaskListName = binding.etEditTaskListName
        var cvEditTaskListName = binding.cvEditTaskListName
        var ibCloseEditableView = binding.ibCloseEditableView
        var ibDoneEditListName = binding.ibDoneEditListName
    }

}