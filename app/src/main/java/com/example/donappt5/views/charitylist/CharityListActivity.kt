package com.example.donappt5.views.charitylist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.donappt5.R
import com.example.donappt5.data.adapters.CharityAdapter
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.util.MyGlobals
import com.example.donappt5.viewmodels.ProgramEntryViewModel
import com.example.donappt5.views.charitycreation.CharityCreationActivity
import com.example.donappt5.views.charitycreation.popups.ActivityConfirm
import com.example.donappt5.views.charitycreation.popups.LocatorActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.koalap.geofirestore.GeoQuery

//import com.google.firebase.analytics.FirebaseAnalytics;
//TODO in general: change support mail in firebase console settings AND project name
class CharityListActivity : AppCompatActivity() {
    private val preLast = 0
    var chars = ArrayList<Charity>()
    var geochars = ArrayList<String>()
    var charAdapter: CharityAdapter? = null
    lateinit var ctx: Context

    //private FirebaseAnalytics mFirebaseAnalytics;
    lateinit var pullToRefresh: SwipeRefreshLayout
    var lastVisible: DocumentSnapshot? = null
    private val prelast = 0
    var fillingData = false
    var fillingmode = 0
    var fdistance = 0
    var myGlobals: MyGlobals? = null
    var queryInput: String? = null
    lateinit var fillingQuery: GeoQuery
    var tag = "none"
    lateinit var pager: ViewPager
    var pagerAdapter: PagerAdapter? = null
    var bottomNavigationView: BottomNavigationView? = null
    lateinit var fabAddCharity: FloatingActionButton
    lateinit var viewModel: ProgramEntryViewModel
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("ProgressTracker", "position 0")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charitylist)
        viewModel = ViewModelProvider(this)[ProgramEntryViewModel::class.java]
        ctx = this
        viewModel.userHasLocationsOfInterest.observe(this) { data ->
            launchLocatorActivity()
        }
        pullToRefresh = findViewById(R.id.pullToRefresh)
        pullToRefresh.setOnRefreshListener {
            val selectedItem = pager.currentItem
            pagerAdapter =
                MyPagerAdapter(
                    supportFragmentManager
                )
            pager.adapter = pagerAdapter
            pager.currentItem = selectedItem
            pagerAdapter!!.notifyDataSetChanged()
            pullToRefresh.isRefreshing = false
        }
        fabAddCharity = findViewById(R.id.fab)
        fabAddCharity.setOnClickListener {
            val intent = Intent(ctx, CharityCreationActivity::class.java)
            startActivity(intent)
        }

        // создаем адаптер
        charAdapter = CharityAdapter(this, chars)
        pager = findViewById(R.id.cpOverview)
        changePageTitle(0)
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                changePageTitle(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE)
            }
        })
        pagerAdapter = MyPagerAdapter(
            supportFragmentManager
        )
        pager.setAdapter(pagerAdapter)
        myGlobals = MyGlobals(ctx)
        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        myGlobals!!.setupBottomNavigation(ctx, this, bottomNavigationView!!)
    }

    fun toggleRefreshing(enabled: Boolean) {
        pullToRefresh.isEnabled = enabled
    }

    fun launchLocatorActivity() {
        val intent = Intent(ctx, ActivityConfirm::class.java)
        intent.putExtra("CancelButtonTitle", "Cancel setting location")
        intent.putExtra("ConfirmButtonTitle", "Set location")
        intent.putExtra(
            "PopupText", "You seem not to have locations of interest." +
                    "Location of interest is a place you are interested in hearing about, such as your local community, your city or your district." +
                    "When a charity is registered near your location of interest you recieve a notification." +
                    "We recommend setting a location of interest. You will be able to delete it or turn off notifications at any time."
        )
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        intent.putExtra("width", (size.x.toDouble() * 0.9).toInt())
        intent.putExtra("height", (size.y.toDouble() * 0.7).toInt())
        startActivityForResult(intent, 3)
    }

    private fun changePageTitle(pos: Int) {
        val underlineAll = findViewById<View>(R.id.underlineViewAll)
        val underlineRecommended = findViewById<View>(R.id.underlineViewRecommended)
        val tvAll = findViewById<TextView>(R.id.tvAll)
        val tvRecommended = findViewById<TextView>(R.id.tvRecommended)
        if (pos == 0) {
            tvAll.setTextColor(ContextCompat.getColor(this, R.color.colorSelectedTextHighlight))
            underlineAll.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorSelectedTextHighlight
                )
            )
            underlineAll.visibility = View.VISIBLE
            tvRecommended.setTextColor(Color.BLACK)
            underlineRecommended.visibility = View.GONE
        } else {
            tvRecommended.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorSelectedTextHighlight
                )
            )
            underlineRecommended.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorSelectedTextHighlight
                )
            )
            underlineRecommended.visibility = View.VISIBLE
            tvAll.setTextColor(Color.BLACK)
            underlineAll.visibility = View.GONE
        }
    }

    private inner class MyPagerAdapter(fm: FragmentManager?) :
        FragmentStatePagerAdapter(fm!!) {
        override fun getItem(pos: Int): Fragment {
            return when (pos) {
                1 -> CharityRecommendationsFragment.newInstance()
                else -> CharityListFragment.newInstance(fillingmode)
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    public override fun onResume() {
        super.onResume()
        myGlobals!!.setSelectedItem(this, bottomNavigationView!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        val resultingactivity = data.getStringExtra("resultingactivity")
        Log.d("progresstracker", "resulted activity $resultingactivity")
        if (resultingactivity != null) {
            if (resultingactivity == "LocatorActivity") {
                viewModel.onLocatorActivityResult(data)
            } else {
                if (resultingactivity == "ActivityConfirm") {
                    val result = data.getStringExtra("result")
                    if (result == "confirmed") {
                        Log.d("progresstracker", "confirmedresult")
                        val intent = Intent(ctx, LocatorActivity::class.java)
                        intent.putExtra(
                            "headertext",
                            "Set a location of interest. Hold on the marker and drag it."
                        )
                        intent.putExtra("btnaccept", "Here")
                        intent.putExtra("btncancel", "Skip this step")
                        startActivityForResult(intent, 3)
                    }
                }
            }
        }
    }

}