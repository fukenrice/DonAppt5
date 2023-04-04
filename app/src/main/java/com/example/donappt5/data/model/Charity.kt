package com.example.donappt5.data.model

import android.util.Log
import com.example.donappt5.R
import com.google.firebase.firestore.DocumentSnapshot

class Charity {
    @kotlin.jvm.JvmField
    var image: Int = R.drawable.ic_launcher_foreground

    @kotlin.jvm.JvmField
    var id: Int = 0

    @kotlin.jvm.JvmField
    var trust: Float = 0.0f

    lateinit var firestoreID: String
    lateinit var name: String //TODO change to setters and getters
    lateinit var briefDescription: String
    lateinit var fullDescription: String
    lateinit var photourl: String
    lateinit var paymentUrl: String

    constructor(
        gfirestoreID: String?,
        gname: String?,
        gbdesc: String?,
        gfdesc: String?,
        gtrust: Float,
        gim: Int,
        gid: Int,
        gphotourl: String?
    ) {
        firestoreID = gfirestoreID?: ""
        name = gname?: ""
        briefDescription = gbdesc?: ""
        fullDescription = gfdesc?: ""
        trust = gtrust
        image = gim
        id = gid
        photourl = gphotourl?: ""
        paymentUrl = ""
    }

    constructor(
        gfirestoreID: String?,
        gname: String?,
        gbdesc: String?,
        gfdesc: String?,
        gtrust: Float,
        gim: Int,
        gid: Int,
        gphotourl: String?,
        paymentUrl: String?
    ) {
        firestoreID = gfirestoreID?: ""
        name = gname?: ""
        briefDescription = gbdesc?: ""
        fullDescription = gfdesc?: ""
        trust = gtrust
        image = gim
        id = gid
        photourl = gphotourl?: ""
        this.paymentUrl = paymentUrl?: ""
    }

    constructor() {
        name = "enter charity name here"
        briefDescription = "enter your charity description here"
        fullDescription = "enter your charity description here, enter qiwi url on the page to the right"
        trust = -1f
        id = -2
        paymentUrl = ""
        photourl = ""
    }

    constructor(document: DocumentSnapshot?) {
        if (document == null) {
            return
        }
        firestoreID = document.id
        name = document.getString("name")?: "No name"
        fullDescription = document.getString("description")?: "No description"
        briefDescription = fullDescription.substring(0, Math.min(fullDescription.length, 50))
        photourl = document.getString("photourl")?: ""
        paymentUrl = document.getString("qiwiurl")?: ""
        image = R.drawable.ic_launcher_foreground
        trust = -1f
        id = -2
    }

    companion object {
        fun DocumentSnapshot.toCharity(): Charity? {
            try {
                return Charity(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting charity", e)
                return null
            }
        }
        private const val TAG = "Charity"
    }
}