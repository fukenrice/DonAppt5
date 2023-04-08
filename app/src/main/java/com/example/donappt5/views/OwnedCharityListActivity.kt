package com.example.donappt5.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.donappt5.R
import com.example.donappt5.views.adapters.CharityAdapter
import com.example.donappt5.databinding.ActivityOwnedCharityListBinding
import com.example.donappt5.data.model.Charity
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.data.util.Status
import com.example.donappt5.viewmodels.OwnedCharityListVeiwModel
import com.example.donappt5.views.charitycreation.CharityCreationActivity

class OwnedCharityListActivity : AppCompatActivity() {

    lateinit var binding: ActivityOwnedCharityListBinding
    lateinit var adapter: CharityAdapter
    private var preLast = 0
    lateinit var myGlobals: MyGlobals
    private lateinit var viewModel: OwnedCharityListVeiwModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnedCharityListBinding.inflate(layoutInflater)
        myGlobals = MyGlobals(this)
        val ctx: Context = this
        myGlobals.setupBottomNavigation(ctx, this, binding.bottomNavigation)
        viewModel = ViewModelProvider(this)[OwnedCharityListVeiwModel::class.java]
        viewModel.fillData()
        setupObserver()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        myGlobals.setSelectedItem(this, binding.bottomNavigation)
        viewModel.fillData()
    }

    private fun setupObserver() {
        viewModel.getChars().observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { it1 -> renderList(it1) }
                }
                Status.LOADING -> {
                    // Handle Loading
                }
                Status.ERROR -> {
                    // Handle Error
                }
            }
        })
    }


    private fun setupView() {
        val view = binding.root
        setContentView(view)


        adapter = CharityAdapter(
            this,
            arrayListOf<Charity>()
        )

        binding.apply {
            pullToRefresh.setOnRefreshListener {
                viewModel.fillData()
                pullToRefresh.isRefreshing = false
            }
            lvMain.isClickable = true
            lvMain.adapter = adapter
            lvMain.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
                override fun onScroll(
                    view: AbsListView,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    onMyScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)

                    // Refresh only when scrolled to the top
                    val refreshLayout = pullToRefresh
                    val topRowVerticalPosition =
                        if (view.childCount == 0) 0 else lvMain.getChildAt(
                            0
                        ).getTop()
                    refreshLayout.isEnabled = topRowVerticalPosition == 0
                }
            })

            lvMain.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    val clickedCharity = adapter.getCharity(position)
                    Log.d(
                        "Click", "itemClick: position = " + position + ", id = "
                                + id + ", name = " + clickedCharity.name + "url = " + clickedCharity.photourl + ", payment url = " + clickedCharity.paymentUrl
                    )
                    val intent = Intent(baseContext, CharityEditActivity::class.java)
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

            addFab.setOnClickListener {
                val intent = Intent(this@OwnedCharityListActivity, CharityCreationActivity::class.java)
                startActivity(intent)
            }
        }


    }


    fun onMyScroll(
        lw: AbsListView, firstVisibleItem: Int,
        visibleItemCount: Int, totalItemCount: Int
    ) {
        when (lw.id) {
            R.id.lvMain -> {
                val lastItem = firstVisibleItem + visibleItemCount
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) {
                        Log.d("Last", "Last")
                        preLast = lastItem
//                        fillAllData()
                    }
                }
            }
        }
    }




    private fun renderList(projects: List<Charity>) {
        adapter.clear()
        adapter.addData(projects)
        adapter.notifyDataSetChanged()
    }


}