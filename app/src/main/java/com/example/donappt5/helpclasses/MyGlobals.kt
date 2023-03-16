package com.example.donappt5.helpclasses

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.donappt5.*
import com.example.donappt5.QRStuff.QRGenerateActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_charitylist.view.*
import kotlinx.android.synthetic.main.activity_owned_charity_list.*
import okhttp3.internal.threadFactory
import org.json.JSONException

class MyGlobals     // constructor
    (var mContext: Context) {
    val userName: String
        get() = "test"
    var photourlfromstore: String? = null
    var drawerlayout: DrawerLayout? = null

    // There are no active networks.
    val isNetworkConnected: Boolean
        get() {
            val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val ni = cm.activeNetworkInfo
            return ni != null
        }

    fun setupBottomNavigation(
        ctx: Context,
        activity: Activity,
        view: BottomNavigationView
    ) {
        view.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED;

        activity.overridePendingTransition(0,0)

        setSelectedItem(activity, view)

        view.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.charitylist -> {
                    val intent5 = Intent(ctx, CharityListActivity::class.java)
                    activity.startActivity(intent5)
                    true
                }
                R.id.map -> {
                    val intent3 = Intent(ctx, CharitiesMapActivity::class.java)
                    activity.startActivity(intent3)
                    true
                }
                R.id.donations -> {
                    val intent6 = Intent(ctx, OwnedCharityListActivity::class.java)
                    activity.startActivity(intent6)
                    true
                }
                R.id.profile -> {
                    val intent = Intent(ctx, ProfileActivity::class.java)
                    activity.startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    fun setSelectedItem(activity: Activity,
        view: BottomNavigationView) {
        view.menu.findItem(when(activity) {
            is CharityListActivity -> { R.id.charitylist }
            is BrowseActivity -> { R.id.charitylist }
            is CharitiesMapActivity -> { R.id.map }
            is CharityActivity -> { R.id.charitylist }
            is CharityCreationActivity -> { R.id.donations }
            is CharityEditActivity -> { R.id.donations }
            is OwnedCharityListActivity -> { R.id.donations }
            is ProfileActivity -> { R.id.profile }
            is SettingsActivity -> { R.id.profile }
            else -> R.id.charitylist
        }).isChecked = true
    }

    /* handle the result */
    val friendsList: List<String>
        get() {
            val friendslist: MutableList<String> = ArrayList()
            GraphRequest(
                AccessToken.getCurrentAccessToken(), "/me/friends", null, HttpMethod.GET
            ) { response -> /* handle the result */
                Log.e("Friends List: 1", response.toString())
                try {
                    val responseObject = response.jsonObject
                    val dataArray = responseObject.getJSONArray("data")
                    for (i in 0 until dataArray.length()) {
                        val dataObject = dataArray.getJSONObject(i)
                        val fbId = dataObject.getString("id")
                        val fbName = dataObject.getString("name")
                        Log.e("FbId", fbId)
                        Log.e("FbName", fbName)
                        friendslist.add(fbId)
                        Log.d("friendslist", "fbid: $fbId")
                    }
                    Log.e("fbfriendList", friendslist.toString())
                    val list: List<String> = friendslist
                    var friends = ""
                    if (list != null && list.size > 0) {
                        friends = list.toString()
                        if (friends.contains("[")) {
                            friends = friends.substring(1, friends.length - 1)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                    Log.d("friendslist", "hideLoadingProgress();")
                }
            }.executeAsync()
            return friendslist
        }

}