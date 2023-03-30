package com.example.donappt5.data.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.drawerlayout.widget.DrawerLayout
import com.example.donappt5.*
import com.example.donappt5.views.*
import com.example.donappt5.views.charitycreation.CharityCreationActivity
import com.example.donappt5.views.charitydescription.CharityActivity
import com.example.donappt5.views.charitylist.CharityListActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import org.json.JSONException

class MyGlobals     // constructor
    (var mContext: Context) {
    var drawerlayout: DrawerLayout? = null

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
            // is BrowseActivity -> { R.id.charitylist }
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