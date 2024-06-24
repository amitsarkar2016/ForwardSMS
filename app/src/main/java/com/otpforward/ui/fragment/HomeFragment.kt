package com.otpforward.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.otpforward.R
import com.otpforward.databinding.DialogAddRuleBinding
import com.otpforward.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var builder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSetup()
        handleClicks()
        handleViewModels()

    }

    private fun showAddUpdateDialog() {
        builder = AlertDialog.Builder(requireContext())
        val binding = DialogAddRuleBinding.inflate(LayoutInflater.from(requireContext()))
        builder?.setView(binding.root)
        dialog = builder?.create()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.addUpdate.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun handleViewModels() {

    }

    private fun handleClicks() {
        binding.addRule.setOnClickListener {
            showAddUpdateDialog()
        }
    }

    private fun handleSetup() {

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}