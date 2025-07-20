package com.feri.smartheat.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.feri.smartheat.databinding.FragmentDashboardBinding
import com.feri.smartheat.ui.SharedViewModel
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.views.cartesian.ScrollHandler
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var roomTempChartModelProducer: CartesianChartModelProducer
    private lateinit var roomHumidityChartModelProducer: CartesianChartModelProducer
    private lateinit var furnaceTempChartModelProducer: CartesianChartModelProducer
    private lateinit var fuelLevelChartModelProducer: CartesianChartModelProducer

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        roomTempChartModelProducer = CartesianChartModelProducer()
        roomHumidityChartModelProducer = CartesianChartModelProducer()
        furnaceTempChartModelProducer = CartesianChartModelProducer()
        fuelLevelChartModelProducer = CartesianChartModelProducer()

        binding.roomTempChartView.run {
            scrollHandler = ScrollHandler(
                autoScrollCondition = AutoScrollCondition.OnModelGrowth,
                initialScroll = Scroll.Absolute.End
            )
            modelProducer = roomTempChartModelProducer
            isHorizontalScrollBarEnabled = true
        }
        binding.roomHumidityChartView.run {
            scrollHandler = ScrollHandler(
                autoScrollCondition = AutoScrollCondition.OnModelGrowth,
                initialScroll = Scroll.Absolute.End
            )
            modelProducer = roomHumidityChartModelProducer
            isHorizontalScrollBarEnabled = true
        }
        binding.furnaceTempChartView.run {
            scrollHandler = ScrollHandler(
                autoScrollCondition = AutoScrollCondition.OnModelGrowth,
                initialScroll = Scroll.Absolute.End
            )
            modelProducer = furnaceTempChartModelProducer
            isHorizontalScrollBarEnabled = true
        }
        binding.fuelLevelChartView.run {
            scrollHandler = ScrollHandler(
                autoScrollCondition = AutoScrollCondition.OnModelGrowth,
                initialScroll = Scroll.Absolute.End
            )
            modelProducer = fuelLevelChartModelProducer
            isHorizontalScrollBarEnabled = true
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.fetchHistory( onSuccess = {
            // Observe distance (fuel level) from SharedViewModel
            sharedViewModel.fuelPercentageHistory.observe(viewLifecycleOwner) { distanceHistory ->
                if (distanceHistory.isNotEmpty()) {
                    lifecycleScope.launch {
                        fuelLevelChartModelProducer.runTransaction {
                            lineSeries {
                                series(
                                    y = distanceHistory.map { it }
                                )
                            }
                        }
                        binding.fuelLevelChartView.scrollHandler.scroll(Scroll.Absolute.End)
                    }
                }
            }

            // Observe humidity data
            sharedViewModel.humidityHistory.observe(viewLifecycleOwner) { humidityHistory ->
                if (humidityHistory.isNotEmpty()) {
                    lifecycleScope.launch {
                        roomHumidityChartModelProducer.runTransaction {
                            lineSeries {
                                series(
                                    y = humidityHistory.map { it }
                                )
                            }
                        }
                        binding.roomHumidityChartView.scrollHandler.scroll(Scroll.Absolute.End)
                    }
                }
            }

            sharedViewModel.furnaceTempHistory.observe(viewLifecycleOwner) { furnaceTempHistory ->
                if (furnaceTempHistory.isNotEmpty()) {
                    lifecycleScope.launch {
                        furnaceTempChartModelProducer.runTransaction {
                            lineSeries {
                                series(
                                    y = furnaceTempHistory.map { it }
                                )
                            }
                        }
                        binding.furnaceTempChartView.scrollHandler.scroll(Scroll.Absolute.End)
                    }
                }
            }

            // Observe temperature history from ViewModel and update chart
            sharedViewModel.roomTempHistory.observe(viewLifecycleOwner) { tempHistory ->
                if (tempHistory.isNotEmpty()) {
                    lifecycleScope.launch {
                        roomTempChartModelProducer.runTransaction {
                            lineSeries {
                                series(
                                    y = tempHistory.map { it }
                                )
                            }
                        }
                        binding.roomTempChartView.scrollHandler.scroll(Scroll.Absolute.End)
                    }
                }
            }
        }, onError = {
            Log.d("Error", it)
        })

    }

}