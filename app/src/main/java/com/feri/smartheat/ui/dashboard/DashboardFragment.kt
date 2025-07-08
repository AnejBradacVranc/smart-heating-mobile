package com.feri.smartheat.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.feri.smartheat.databinding.FragmentDashboardBinding
import com.feri.smartheat.ui.SharedViewModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var modelProducer: CartesianChartModelProducer

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the model producer
        modelProducer = CartesianChartModelProducer()
        binding.chartView.modelProducer = modelProducer

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        // Observe distance (temperature) from SharedViewModel
        sharedViewModel.distance.observe(viewLifecycleOwner) { distance ->
            // Add to history in ViewModel
            dashboardViewModel.addTemperatureReading(distance.toFloat())
        }

        // Observe other data if needed
        sharedViewModel.humidity.observe(viewLifecycleOwner) { humidity ->
            Log.d("DashboardFragment", "Received humidity: $humidity")
            // Handle humidity data
        }

        sharedViewModel.furnaceTemp.observe(viewLifecycleOwner) { furnaceTemp ->
            Log.d("DashboardFragment", "Received furnace temp: $furnaceTemp")
            // Handle furnace temperature data
        }

        // Observe temperature history from ViewModel and update chart
        dashboardViewModel.roomTempHistory.observe(viewLifecycleOwner) { tempHistory ->
            if (tempHistory.isNotEmpty()) {
                lifecycleScope.launch {
                    modelProducer.runTransaction {
                        lineSeries {
                            series(tempHistory)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}