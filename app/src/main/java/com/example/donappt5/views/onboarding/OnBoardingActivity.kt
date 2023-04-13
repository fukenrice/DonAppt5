package com.example.donappt5.views.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.donappt5.databinding.ActivityOnBoardingBinding
import com.example.donappt5.data.model.OnBoardingDonationRecord
import com.example.donappt5.data.util.Status
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.viewmodels.OnBoardingViewModel
import com.example.donappt5.views.charitylist.CharityListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding
    private lateinit var fragPreviousDonations: PreviousDonationsFragment
    private lateinit var fragCurrentDonations: PreviousDonationsFragment
    private lateinit var fragLikelyDonations: PreviousDonationsFragment
    private lateinit var viewModel: OnBoardingViewModel
    private lateinit var fragTags: TagsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        val view = binding.root
        viewModel = ViewModelProvider(this)[OnBoardingViewModel::class.java]
        setupObserver()
        setContentView(view)
        // MyGlobals(baseContext).setupBottomNavigation(baseContext, this, binding.bottomNavigation)
    }

    fun setupObserver() {
        viewModel.userPreferences.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    fragPreviousDonations = PreviousDonationsFragment.newInstance(
                        "Please enter data about your previous donations and move to the next screen",
                        it.data?.previousDonations
                    )
                    fragCurrentDonations = PreviousDonationsFragment.newInstance(
                        "Please enter data about your current donations and move to the next screen",
                        it.data?.currentDonations
                    )
                    fragLikelyDonations = PreviousDonationsFragment.newInstance(
                        "Please enter data about your likely donations and check tags you like in the next screen",
                        it.data?.likelyDonations
                    )

                    fragTags = TagsFragment.newInstance(
                        art = (it.data?.art ?: false),
                        kids = (it.data?.kids ?: false),
                        poverty = (it.data?.poverty ?: false),
                        education = (it.data?.education ?: false),
                        scienceAndResearch = (it.data?.scienceAndResearch ?: false),
                        healthcare = (it.data?.healthcare ?: false),
                    )
                    setupView()
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {
                    Toast.makeText(this, "Something went wrong while getting preferences", Toast.LENGTH_SHORT).show()
                    Log.d("onBoarding", "setupObserverGet: ${it.message}")
                }
            }
        })

        viewModel.postedPreferences.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, "Preferences have been successfully edited", Toast.LENGTH_SHORT).show()
                    // TODO: Открывать след активити
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                    Log.d("onBoarding", "setupObserverPost: ${it.message}")
                }
            }
        })
    }

    fun setupView() {
        binding.apply {
            btnConfirmChanges.setOnClickListener {
                confirmChanges()
                val intent = Intent(baseContext, CharityListActivity::class.java)
                startActivity(intent)
            }

            ChangePager.adapter =
                MyPagerAdapter(
                    supportFragmentManager,
                    listOf(
                        fragPreviousDonations,
                        fragCurrentDonations,
                        fragLikelyDonations,
                        fragTags
                    )
                )
        }
    }

    private fun confirmChanges() {
            // TODO: собрать объект и отправить вьюмодели
            val donationPreferences: MutableMap<String, Any> = HashMap()
            if (fragPreviousDonations.isAdded) {
                val previousDonations = fragPreviousDonations.getData()
                donationPreferences["previousDonations"] = Json.encodeToString(previousDonations)
            } else {
                donationPreferences["previousDonations"] = Json.encodeToString(viewModel.userPreferences.value?.data!!.previousDonations)
            }
            if (fragCurrentDonations.isAdded) {
                val currentDonations = fragCurrentDonations.getData()
                donationPreferences["currentDonations"] = Json.encodeToString(currentDonations)
            } else {
                donationPreferences["currentDonations"] = Json.encodeToString(viewModel.userPreferences.value?.data!!.currentDonations)
            }
            if (fragLikelyDonations.isAdded) {
                val likelyDonations = fragLikelyDonations.getData()
                donationPreferences["likelyDonations"] = Json.encodeToString(likelyDonations)
            } else {
                donationPreferences["likelyDonations"] = Json.encodeToString(viewModel.userPreferences.value?.data!!.likelyDonations)
            }
            if (fragTags.isAdded) {
                donationPreferences += fragTags.getData()
            } else {
                donationPreferences += mapOf(
                    "art" to viewModel.userPreferences.value?.data!!.art,
                    "kids" to viewModel.userPreferences.value?.data!!.kids,
                    "poverty" to viewModel.userPreferences.value?.data!!.poverty,
                    "science&research" to viewModel.userPreferences.value?.data!!.scienceAndResearch,
                    "healthcare" to viewModel.userPreferences.value?.data!!.healthcare,
                    "education" to viewModel.userPreferences.value?.data!!.education,
                )
            }
        viewModel.setUserPreferences(donationPreferences)
    }

    private class MyPagerAdapter(fm: FragmentManager, val fragmentList: List<Fragment>) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return fragmentList.count()
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}
