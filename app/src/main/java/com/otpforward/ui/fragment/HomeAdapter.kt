package com.otpforward.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.otpforward.data.model.UserSettings
import com.otpforward.databinding.ItemHomeBinding
import com.otpforward.utils.SwipeAdapter

class HomeAdapter(
    private val homeListCallBack: HomeListCallBack,
) : ListAdapter<UserSettings, HomeAdapter.ViewHolder>(diffUtil), SwipeAdapter<UserSettings> {

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<UserSettings>() {
            override fun areItemsTheSame(oldItem: UserSettings, newItem: UserSettings): Boolean {
                return oldItem.data == newItem.data
            }

            override fun areContentsTheSame(oldItem: UserSettings, newItem: UserSettings): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserSettings) {
            binding.sim.text = item.simName
            binding.to.text = item.sendTo
            binding.date.text = item.date
            binding.type.text = if (item.type == SettingType.CARD_OTP) item.data else item.type.value
            binding.isActive.text = if (item.isActive) "Active" else "InActive"
            binding.root.setOnClickListener {
                homeListCallBack.onItemClick(item)
            }
        }
    }
}