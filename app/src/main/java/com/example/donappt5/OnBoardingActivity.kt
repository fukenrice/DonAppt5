package com.example.donappt5

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.donappt5.databinding.ActivityOnBoardingBinding
import com.example.donappt5.helpclasses.OnBoardingDonationRecord
import com.example.donappt5.onboardingfragments.PreviousDonationsFragment
import com.example.donappt5.onboardingfragments.TagsFragment
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
    private lateinit var fragTags: TagsFragment
    private var prevDonList = arrayListOf<OnBoardingDonationRecord>()
    private var currDonList = arrayListOf<OnBoardingDonationRecord>()
    private var likelyDonList = arrayListOf<OnBoardingDonationRecord>()
    private var tags: MutableMap<String, Boolean> = mutableMapOf(
        "art" to false,
        "kids" to false,
        "poverty" to false,
        "science&research" to false,
        "healthcare" to false,
        "education" to false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        fillData()
    }

    private fun fillData() {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener {
                Log.d("onboarding", it.data.toString())
                val prevDon = it.getString("previousDonations")
                if (prevDon != null) {
                    prevDonList = Json.decodeFromString(prevDon)
                } else {
                    prevDonList = arrayListOf()
                }
                val currDon = it.getString("currentDonations")
                if (currDon != null) {
                    currDonList =
                        Json.decodeFromString(currDon)
                } else {
                    prevDonList = arrayListOf()
                }
                val likelyDon = it.getString("likelyDonations")
                if (likelyDon != null) {
                    likelyDonList =
                        Json.decodeFromString(likelyDon)
                } else {
                    prevDonList = arrayListOf()
                }
                fragPreviousDonations = PreviousDonationsFragment.newInstance(
                    "Please enter data about your previous donations and move to the next screen",
                    prevDonList
                )
                fragCurrentDonations = PreviousDonationsFragment.newInstance(
                    "Please enter data about your current donations and move to the next screen",
                    currDonList
                )
                fragLikelyDonations = PreviousDonationsFragment.newInstance(
                    "Please enter data about your likely donations and check tags you like in the next screen",
                    likelyDonList
                )

                fragTags = TagsFragment.newInstance(
                    art = it.getBoolean("art") == true,
                    kids = it.getBoolean("kids") == true,
                    poverty = it.getBoolean("poverty") == true,
                    education = it.getBoolean("education") == true,
                    scienceAndResearch = it.getBoolean("science&research") == true,
                    healthcare = it.getBoolean("healthcare") == true
                )

                tags["art"] = it.getBoolean("art") == true
                tags["kids"] = it.getBoolean("kids") == true
                tags["poverty"] = it.getBoolean("poverty") == true
                tags["education"] = it.getBoolean("education") == true
                tags["scienceAndResearch"] = it.getBoolean("science&research") == true
                tags["healthcare"] = it.getBoolean("healthcare") == true

                binding.apply {
                    btnConfirmChanges.setOnClickListener {
                        confirmChanges()
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
        }
    }

    private fun confirmChanges() {
        // TODO: Проблема архитектуры: из-за того, что храним данные в UI нужно каждый раз инициализоваровать все фрагменты перед отправкой(нет модели)
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val donationPreferences: MutableMap<String, Any> = HashMap()
            if (fragPreviousDonations.isAdded) {
                val previousDonations = fragPreviousDonations.getData()
                donationPreferences["previousDonations"] = Json.encodeToString(previousDonations)
            } else {
                donationPreferences["previousDonations"] = Json.encodeToString(prevDonList)
            }
            if (fragCurrentDonations.isAdded) {
                val currentDonations = fragCurrentDonations.getData()
                donationPreferences["currentDonations"] = Json.encodeToString(currentDonations)
            } else {
                donationPreferences["currentDonations"] = Json.encodeToString(currDonList)
            }
            if (fragLikelyDonations.isAdded) {
                val likelyDonations = fragLikelyDonations.getData()
                donationPreferences["likelyDonations"] = Json.encodeToString(likelyDonations)
            } else {
                donationPreferences["likelyDonations"] = Json.encodeToString(likelyDonList)
            }
            if (fragTags.isAdded) {
                donationPreferences += fragTags.getData()
            } else {
                donationPreferences += tags
            }
            db.collection("users").document(user.uid).update(donationPreferences)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Preferences have been successfully edited",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("onboarding", "successfully edited")
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Something went wrong while editing preferences",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("onboarding", it.message.toString())
                    finish()
                }
        }
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
