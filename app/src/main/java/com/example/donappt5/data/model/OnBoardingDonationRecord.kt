package com.example.donappt5.data.model

@kotlinx.serialization.Serializable
data class OnBoardingDonationRecord(
    val charityName: String,
    val monthlyDonation: Int?
)
