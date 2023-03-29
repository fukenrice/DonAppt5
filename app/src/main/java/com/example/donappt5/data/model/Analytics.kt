package com.example.donappt5.data.model

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class Analytics {
    companion object {
        private val firebaseAnalytics = Firebase.analytics

        fun logAnalyticsEvent(id: String) {
            val user = FirebaseAuth.getInstance().currentUser
            firebaseAnalytics.setUserId(user?.uid)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, id)
            }
        }
    }
}