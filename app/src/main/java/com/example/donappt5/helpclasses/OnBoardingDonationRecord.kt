package com.example.donappt5.helpclasses

@kotlinx.serialization.Serializable
data class OnBoardingDonationRecord(
    val charityName: String,
    val monthlyDonation: Int?
)
