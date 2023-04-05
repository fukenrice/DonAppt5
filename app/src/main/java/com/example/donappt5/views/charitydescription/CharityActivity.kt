package com.example.donappt5.views.charitydescription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import com.example.donappt5.R
import com.example.donappt5.data.model.Charity
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.data.util.Status
import com.example.donappt5.databinding.ActivityCharitydescBinding
import com.example.donappt5.viewmodels.CharityViewModel
import com.example.donappt5.views.QiwiPaymentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso

class CharityActivity : AppCompatActivity() {
    lateinit var ctx: Context
    var pagerAdapter: PagerAdapter? = null
    var myGlobals: MyGlobals? = null
    var bottomNavigationView: BottomNavigationView? = null
    lateinit var viewModel: CharityViewModel
    private lateinit var binding: ActivityCharitydescBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        ctx = this
        super.onCreate(savedInstanceState)
        binding = ActivityCharitydescBinding.inflate(layoutInflater)
        val view = binding.root

        viewModel = ViewModelProvider(this)[CharityViewModel::class.java]
        viewModel.setCharity(
            Charity(
                intent.getStringExtra("firestoreID"),
                intent.getStringExtra("chname"),
                intent.getStringExtra("bdesc"),
                intent.getStringExtra("fdesc"),
                intent.getFloatExtra("trust", 0f),
                intent.getIntExtra("image", 0),
                intent.getIntExtra("id", 0),
                intent.getStringExtra("url"),
                intent.getStringExtra("qiwiPaymentUrl")
            )
        )

        myGlobals = MyGlobals(ctx)
        bottomNavigationView = binding.bottomNavigation
        myGlobals!!.setupBottomNavigation(ctx, this, bottomNavigationView!!)

        setupView()
        setupObserver()
        setContentView(view)
    }

    private fun setupObserver() {
        viewModel.getCharity().observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.apply {
                        tvName.text = it.data!!.name
                        tvTrustRating.text = it.data.trust.toString()
                        if (it.data.photourl.isNotEmpty()) {
                            ivImage.setImageResource(R.drawable.ic_sync)
                            Picasso.with(ctx).load(it.data.photourl).fit().into(ivImage)
                        }
                        pagerAdapter = MyPagerAdapter(supportFragmentManager)
                        viewPager.adapter = pagerAdapter
                    }
                    viewModel.loadFav(it.data!!.firestoreID)
                }

                Status.LOADING -> {
                    // Handle loading
                }

                Status.ERROR -> {
                    // Handle error
                }
            }
        })

        viewModel.isFavourite().observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.apply {
                        if (it.data == true) {
                            ivFavorite.setImageResource(R.drawable.ic_favorite_on)
                        } else {
                            ivFavorite.setImageResource(R.drawable.ic_favorite_off)
                        }
                    }
                }

                Status.LOADING -> {
                    // Handle loading
                }

                Status.ERROR -> {
                    // Handle error
                }
            }
        })
    }

    private fun setupView() {
        binding.apply {
            btnDonate.setOnClickListener {
                viewModel.logAnalytics()

                if (viewModel.getCharity().value?.data?.paymentUrl != null && viewModel.getCharity().value?.data?.paymentUrl != "") {
                    val intent1 = Intent(ctx, QiwiPaymentActivity::class.java)
                    intent1.putExtra("firestoreID", viewModel.getCharity().value?.data?.firestoreID)
                    intent1.putExtra("charityname", viewModel.getCharity().value?.data?.name)
                    intent1.putExtra("qiwiPaymentUrl", viewModel.getCharity().value?.data?.paymentUrl)
                    startActivity(intent1)
                } else {
                    Toast.makeText(
                        applicationContext,
                        R.string.no_payment_credentials_message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            ivFavorite.setOnClickListener { viewModel.changeFav() }
        }
    }

    public override fun onResume() {
        super.onResume()
        myGlobals!!.setSelectedItem(this, bottomNavigationView!!)
    }

    private inner class MyPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        override fun getItem(pos: Int): Fragment {
            return when (pos) {
                0 -> CharityDescFragment.newInstance(viewModel.getCharity().value!!.data)
                else -> CharityDescFragment.newInstance(viewModel.getCharity().value!!.data)
            }
        }

        override fun getCount(): Int {
            return 1
        }
    }
}
