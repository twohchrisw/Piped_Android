package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class ProcessMenuRecyclerAdapter(val menuMode: Int, val clickListener: ProcessMenuRecyclerClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    enum class MainMenuItems(val value: Int)
    {
        processDetails(0), tasks(1), equipment(2), consumables(3), count(4)
    }

    enum class TaskMenuItems(val value: Int) {
        surveying(0), swabbing(1), filling(2), peTest(3), diTest(4), chlor(5), dechlor(6), flushing(7), flushing2(8), sampling(9), count(10)
    }

    interface ProcessMenuRecyclerClickListener {
        fun listItemClicked(menuMode: Int, menuItem: Int)
    }

    override fun getItemCount(): Int {
        if (menuMode == ProcessMenuActivity.MENU_MODE_MAIN)
        {
            return MainMenuItems.count.value
        }

        if (menuMode == ProcessMenuActivity.MENU_MODE_TASKS)
        {
            return TaskMenuItems.count.value
        }

        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_one_line_text, parent, false)
        return ViewHolderOneLineText(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val viewHolder = holder as ViewHolderOneLineText
        val mainText = viewHolder.mainText

        if (menuMode == ProcessMenuActivity.MENU_MODE_MAIN)
        {
            val hasEnteredInfo = AppGlobals.instance.activeProcess.hasEnteredProcessDetails()

            when (position) {

                MainMenuItems.processDetails.value -> mainText?.text = "Process Details"
                MainMenuItems.tasks.value -> {
                    mainText?.text = "Tasks"
                    if (!hasEnteredInfo)
                    {
                        mainText?.alpha = 0.5f
                    }
                    else
                    {
                        mainText?.alpha = 1.0f
                    }
                }
                MainMenuItems.equipment.value -> {
                    mainText?.text = "Equipment"
                    if (!hasEnteredInfo)
                    {
                        mainText?.alpha = 0.5f
                    }
                    else
                    {
                        mainText?.alpha = 1.0f
                    }
                }
                MainMenuItems.consumables.value ->  {
                    mainText?.text = "Consumables / Hire"
                    if (!hasEnteredInfo)
                    {
                        mainText?.alpha = 0.5f
                    }
                    else
                    {
                        mainText?.alpha = 1.0f
                    }
                }

            }
        }

        if (menuMode == ProcessMenuActivity.MENU_MODE_TASKS)
        {
            when (position)
            {
                TaskMenuItems.surveying.value -> mainText?.text = "Surveying"
                TaskMenuItems.swabbing.value -> mainText?.text = "Swabbing"
                TaskMenuItems.filling.value -> mainText?.text = "Filling Only"
                TaskMenuItems.peTest.value -> mainText?.text = "Pressure Testing PE"
                TaskMenuItems.diTest.value -> mainText?.text = "Pressure Testing DI"
                TaskMenuItems.chlor.value -> mainText?.text = "Chlorination"
                TaskMenuItems.dechlor.value -> mainText?.text = "Dechlorination"
                TaskMenuItems.flushing.value -> mainText?.text = "Flushing"
                TaskMenuItems.flushing2.value -> mainText?.text = "Flushing 2"
                TaskMenuItems.sampling.value -> mainText?.text = "Sampling"
            }
        }

        viewHolder.itemView.setOnClickListener({
            clickListener.listItemClicked(menuMode, position)
        })
    }
}