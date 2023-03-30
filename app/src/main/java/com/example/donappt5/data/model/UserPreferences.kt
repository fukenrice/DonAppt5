package com.example.donappt5.data.model

import android.util.Log
import com.example.donappt5.R
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UserPreferences {
    lateinit var previousDonations: ArrayList<OnBoardingDonationRecord>
    lateinit var currentDonations: ArrayList<OnBoardingDonationRecord>
    lateinit var likelyDonations: ArrayList<OnBoardingDonationRecord>

    var art: Boolean = false
    var kids: Boolean = false
    var poverty: Boolean = false
    var education: Boolean = false
    var scienceAndResearch: Boolean = false
    var healthcare: Boolean = false

    constructor(
        previousDonations: ArrayList<OnBoardingDonationRecord>?,
        currentDonations: ArrayList<OnBoardingDonationRecord>?,
        likelyDonations: ArrayList<OnBoardingDonationRecord>?,
    ) {
        this.likelyDonations = likelyDonations?: arrayListOf()
        this.currentDonations = currentDonations?: arrayListOf()
        this.previousDonations = previousDonations?: arrayListOf()
    }

    constructor(document: DocumentSnapshot?) {
        if (document == null) {
            return
        }
        val prevDon = document.getString("previousDonations")
        if (prevDon != null) {
            previousDonations = Json.decodeFromString(prevDon)
        } else {
            previousDonations = arrayListOf()
        }
        val currDon = document.getString("currentDonations")
        if (currDon != null) {
            currentDonations =
                Json.decodeFromString(currDon)
        } else {
            currentDonations = arrayListOf()
        }
        val likelyDon = document.getString("likelyDonations")
        if (likelyDon != null) {
            likelyDonations =
                Json.decodeFromString(likelyDon)
        } else {
            likelyDonations = arrayListOf()
        }

        art = document.getBoolean("art") == true
        kids = document.getBoolean("kids") == true
        poverty = document.getBoolean("poverty") == true
        education = document.getBoolean("education") == true
        scienceAndResearch = document.getBoolean("science&research") == true
        healthcare = document.getBoolean("healthcare") == true
    }

    companion object {
        fun DocumentSnapshot.toUserPreferences(): UserPreferences? {
            try {
                return UserPreferences(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting UserPreferences", e)
                return null
            }
        }
        private const val TAG = "UserPreferences"
    }
}
