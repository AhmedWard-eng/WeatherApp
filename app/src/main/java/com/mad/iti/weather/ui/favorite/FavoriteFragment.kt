package com.mad.iti.weather.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.mad.iti.weather.MapsActivity
import com.mad.iti.weather.databinding.FragmentFavoriteBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.model.FavWeatherRepo
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.utils.statusUtils.FavListAPiStatus
import kotlinx.coroutines.launch

private const val TAG = "FavoriteFragment"

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: FavViewModel by lazy {
        val factory = FavViewModel.Factory(
            FavWeatherRepo.getInstance(
                APIClient,
                DefaultLocalDataSource.getInstance(getDatabase(requireActivity().application).weatherDao)
            )
        )
        ViewModelProvider(this, factory)[FavViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {


        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val adapter = FavAdapter(FavAdapter.OnClickListener(itemClickListener = {
            viewModel.updateWeatherFavInfo(it)
            val action = FavoriteFragmentDirections.actionNavigationFavoritesToShowFavDetailsFragment(it.id)
            Navigation.findNavController(requireView())
                .navigate(action)
        }, removeClickListener = {
            viewModel.deleteItemFromFav(it)
        }))

        binding.recyclerView2.adapter = adapter
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favWeatherData.collect { status ->
                    when (status) {
                        is FavListAPiStatus.Loading -> {
                            binding.progressBar.visibility = VISIBLE
                        }
                        is FavListAPiStatus.Success -> {
                            adapter.submitList(status.favWeatherDataList)
                            if (status.favWeatherDataList.isNotEmpty()) {
                                binding.progressBar.visibility = GONE
                                binding.recyclerView2.visibility = VISIBLE
                                binding.groupNoFav.visibility = GONE
                            } else {
                                binding.progressBar.visibility = GONE
                                binding.recyclerView2.visibility = GONE
                                binding.groupNoFav.visibility = VISIBLE
                            }
                        }
                        else -> {
                            binding.progressBar.visibility = GONE
                            binding.recyclerView2.visibility = GONE
                            binding.groupNoFav.visibility = VISIBLE
                            Log.e(TAG, (status as FavListAPiStatus.Failure).throwable)
                        }
                    }
                }
            }
        }

        binding.floatingActionButton.setOnClickListener {
            with(Intent(requireContext(), MapsActivity::class.java)) {
                startActivity(this)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}