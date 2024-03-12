package gr.georkouk.taskoven.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import gr.georkouk.taskoven.adapters.LabelColorListItemsAdapter
import gr.georkouk.taskoven.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null
    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())
        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)

        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        binding.rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)

}