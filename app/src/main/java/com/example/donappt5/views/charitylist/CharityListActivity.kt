package com.example.donappt5.views.charitylist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.Slide
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.donappt5.R
import com.example.donappt5.data.model.Charity
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.util.Util
import com.example.donappt5.viewmodels.CharityListViewModel
import com.example.donappt5.viewmodels.ProgramEntryViewModel
import com.example.donappt5.views.adapters.CharityAdapter
import com.example.donappt5.views.charitycreation.popups.ActivityConfirm
import com.example.donappt5.views.charitycreation.popups.LocatorActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.koalap.geofirestore.GeoQuery


//import com.google.firebase.analytics.FirebaseAnalytics;
//TODO in general: change support mail in firebase console settings AND project name
class CharityListActivity : AppCompatActivity() {
    var chars = ArrayList<Charity>()
    var charAdapter: CharityAdapter? = null
    lateinit var ctx: Context

    //private FirebaseAnalytics mFirebaseAnalytics;
    lateinit var pullToRefresh: SwipeRefreshLayout
    var fillingmode = 0
    var myGlobals: MyGlobals? = null
    var queryInput: String? = null
    var tag = "none"
    lateinit var pager: ViewPager
    var pagerAdapter: PagerAdapter? = null
    var bottomNavigationView: BottomNavigationView? = null
    lateinit var fabSearch: FloatingActionButton
    lateinit var programEntryViewModel: ProgramEntryViewModel
    val viewModel: CharityListViewModel by viewModels()
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("ProgressTracker", "position 0")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charitylist)
        programEntryViewModel = ViewModelProvider(this)[ProgramEntryViewModel::class.java]
        ctx = this
        programEntryViewModel.userHasLocationsOfInterest.observe(this) { data ->
            // launchLocatorActivity()
        }

        if (intent.getIntExtra("fillingmode", Util.FILLING_ALPHABET) == Util.FILLING_FAVORITES) {
            fillingmode = Util.FILLING_FAVORITES
        }
        setupView()

        charAdapter = CharityAdapter(this, chars)
        myGlobals = MyGlobals(ctx)
        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        myGlobals!!.setupBottomNavigation(ctx, this, bottomNavigationView!!)
    }

    fun setupView() {
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

        fabSearch = findViewById(R.id.fabOpenSearch)
        fabSearch.setOnClickListener {
            val searchDialog = SearchDialogFragment(fabSearch.size / 2)
            searchDialog.enterTransition = Slide(Gravity.BOTTOM);
            searchDialog.exitTransition = Slide(Gravity.TOP);
            searchDialog.dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            searchDialog.show(supportFragmentManager, "search_dialog")
        }

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
        pager.adapter = pagerAdapter
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
                programEntryViewModel.onLocatorActivityResult(data)
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