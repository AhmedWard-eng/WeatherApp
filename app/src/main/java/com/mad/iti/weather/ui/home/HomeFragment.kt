package com.mad.iti.weather.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mad.iti.weather.MainViewModel
import com.mad.iti.weather.databinding.FragmentHomeBinding
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.model.OneCallRepo
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.networkUtils.APIStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val factory = HomeViewModel.Factory(_repo = OneCallRepo.getInstance(APIClient))
        val homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                homeViewModel.weather.collectLatest { status ->
                    Log.d(TAG, "onCreateView: ${status.javaClass}")
                    when (status) {
                        is APIStatus.Loading -> {
                            Log.d(TAG, "onCreateView: Loading")
                        }
                        is APIStatus.Success -> {
                            Log.d(TAG, "onCreateView: Success")
                            Log.d(TAG, "${status.oneCallWeatherResponse}")
                            withContext(Dispatchers.IO) {
                                getDatabase(requireContext()).weatherDao.insertAll(status.oneCallWeatherResponse)
                            }
                        }
                        else -> {
                            Log.d(TAG, (status as APIStatus.Failure).throwable)
                        }
                    }
                }
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}