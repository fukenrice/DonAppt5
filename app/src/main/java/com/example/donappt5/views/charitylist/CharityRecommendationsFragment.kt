package com.example.donappt5.views.charitylist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.donappt5.R
import com.example.donappt5.views.adapters.CharityAdapter
import com.example.donappt5.databinding.FragmentCharityListBinding
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.util.ModelConfig
import com.example.donappt5.data.services.RecommendationClient
import com.example.donappt5.viewmodels.CharityRecommendationsViewModel
import com.example.donappt5.views.charitydescription.CharityActivity
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [CharityListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharityRecommendationsFragment : Fragment() {
    private lateinit var binding: FragmentCharityListBinding
    var chars = ArrayList<Charity>()
    lateinit var viewModel: CharityRecommendationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("CharityRecommendationsFragment", "entered")
        binding = FragmentCharityListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CharityRecommendationsViewModel::class.java]
        setupView()
        val view = binding.root
        return view
    }

    private fun setupView() {
        viewModel.adapter.value =
            CharityAdapter(context, chars)
        binding.apply {
            lvMain.isClickable = true
            lvMain.adapter = viewModel.adapter.value

            lvMain.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    val clickedCharity: Charity = viewModel.adapter.value!!.getCharity(position)
                    Log.d(
                        "Click", "itemClick: position = " + position + ", id = "
                                + id + ", name = " + clickedCharity.name + "url = " + clickedCharity.photourl + ", payment url = " + clickedCharity.paymentUrl
                    )
                    val intent = Intent(context, CharityActivity::class.java)
                    intent.putExtra("firestoreID", clickedCharity.firestoreID)
                    intent.putExtra("chname", clickedCharity.name)
                    intent.putExtra("bdesc", clickedCharity.briefDescription)
                    intent.putExtra("fdesc", clickedCharity.fullDescription)
                    intent.putExtra("trust", clickedCharity.trust)
                    intent.putExtra("image", clickedCharity.image)
                    intent.putExtra("id", clickedCharity.id)
                    intent.putExtra("url", clickedCharity.photourl)
                    intent.putExtra("qiwiPaymentUrl", clickedCharity.paymentUrl)
                    startActivity(intent)
                }
            lvMain.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
                override fun onScroll(
                    view: AbsListView,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    val refreshLayout =
                        activity?.findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
                    refreshLayout?.isEnabled = false

                }
            })
        }
        viewModel.client.value = RecommendationClient(requireContext(), viewModel.config.value?: ModelConfig())
        lifecycleScope.launch {
            viewModel.fillData()
        }
    }

    override fun onStop() {
        lifecycleScope.launch {
            viewModel.client.value?.unload()
        }
        super.onStop()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CharityRecommendationsFragment().apply {
            }
    }
}