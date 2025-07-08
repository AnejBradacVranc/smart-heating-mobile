package com.feri.smartheat.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.feri.smartheat.databinding.FragmentHomeBinding
import com.feri.smartheat.ui.SharedViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val connectButton: Button = binding.connectButton

        homeViewModel.buttonText.observe(viewLifecycleOwner) {
            binding.connectButton.text = it
        }

        sharedViewModel.distance.observe(viewLifecycleOwner) {
            binding.fuelLevelText.text = it
        }

        sharedViewModel.humidity.observe(viewLifecycleOwner) {
            binding.roomHumidityText.text = it
        }

        sharedViewModel.roomTemp.observe(viewLifecycleOwner) {
            binding.roomTempText.text = it
        }

        sharedViewModel.furnaceTemp.observe(viewLifecycleOwner) {
            binding.furnaceTempText.text = it
        }

        connectButton.setOnClickListener {
            sharedViewModel.connectToBroker()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}