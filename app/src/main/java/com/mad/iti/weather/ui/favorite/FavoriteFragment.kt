package com.mad.iti.weather.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mad.iti.weather.MapsActivity
import com.mad.iti.weather.R
import com.mad.iti.weather.databinding.FragmentFavoriteBinding
import com.mad.iti.weather.db.DefaultLocalDataSource
import com.mad.iti.weather.db.getDatabase
import com.mad.iti.weather.model.FavAlertsWeatherRepo
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.network.APIClient
import com.mad.iti.weather.sharedPreferences.SettingSharedPreferences
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
            FavAlertsWeatherRepo.getInstance(
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
            val action =
                FavoriteFragmentDirections.actionNavigationFavoritesToShowFavDetailsFragment(it.id)
            Navigation.findNavController(requireView()).navigate(action)
        }, removeClickListener = {
            checkDeleteDialog(it)
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
                            adapter.submitList(status.favWeatherEntityList)
                            if (status.favWeatherEntityList.isNotEmpty()) {
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
                putExtra(
                    SettingSharedPreferences.NAVIGATE_TO_MAP,
                    SettingSharedPreferences.ADD_T0_FAV_IN_THIS_LOCATION
                )
                startActivity(this)
            }
        }
        return root
    }

    private fun checkDeleteDialog(_favWeatherEntity: FavWeatherEntity) {
        val deleteAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        deleteAlertDialogBuilder.setBackground(
            ResourcesCompat.getDrawable(
                resources, R.drawable.dialogue_background, requireActivity().theme
            )
        ).setTitle(getString(R.string.assure_deleting)).setCancelable(false).setPositiveButton("Yes")
        { _, _ ->
                viewModel.deleteItemFromFav(_favWeatherEntity)
            }.setNegativeButton(
                "No"
            ) { _, _ -> }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}