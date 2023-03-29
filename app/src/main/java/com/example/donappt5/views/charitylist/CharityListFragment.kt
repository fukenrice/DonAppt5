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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.donappt5.R
import com.example.donappt5.data.adapters.CharityAdapter
import com.example.donappt5.databinding.FragmentCharityListBinding
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.model.Charity.Companion.toCharity
import com.example.donappt5.viewmodels.CharityListViewModel
import com.example.donappt5.views.charitydescription.CharityActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CharityListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharityListFragment : Fragment() {
    private lateinit var binding: FragmentCharityListBinding
    private lateinit var viewModel: CharityListViewModel
    var fillingmode = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCharityListBinding.inflate(inflater, container, false)
        val view = binding.root
        viewModel = ViewModelProvider(this)[CharityListViewModel::class.java]
        viewModel.fillingmode = fillingmode
        setupView()
        viewModel.fillData()
        return view
    }

    fun setupView() {
        viewModel.adapter.value =
            CharityAdapter(context, viewModel.chars.value)
        binding.apply {
            lvMain.isClickable = true
            lvMain.adapter = viewModel.adapter.value
            lvMain.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
                override fun onScroll(
                    view: AbsListView,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    viewModel.onMyScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)

                    // Refresh only when scrolled to the top
                    val refreshLayout = activity?.findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
                    val topRowVerticalPosition =
                        if (view.childCount == 0) 0 else lvMain.getChildAt(
                            0
                        ).getTop()
                    if (refreshLayout != null) {
                        refreshLayout.isEnabled = topRowVerticalPosition == 0
                    }
                }
            })

            lvMain.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    val clickedCharity: Charity = viewModel.adapter.value?.getCharity(position)
                        ?: return@OnItemClickListener
                    Log.d(
                        "Click", "itemClick: position = " + position + ", id = "
                                + id + ", name = " + clickedCharity.name + "url = " + clickedCharity.photourl + ", payment url = " + clickedCharity.paymentUrl
                    )
                    val intent = Intent(context, CharityActivity::class.java)
                    intent.putExtra("firestoreID", clickedCharity.firestoreID);
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
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(gfillingmode: Int) =
            CharityListFragment().apply { fillingmode = gfillingmode }
    }
}