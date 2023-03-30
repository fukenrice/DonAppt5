package com.example.donappt5.views.charitydescription

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.donappt5.R
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.util.MyGlobals
import com.example.donappt5.data.model.Analytics.Companion.logAnalyticsEvent
import com.example.donappt5.views.QiwiPaymentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.net.URL

//import com.squareup.picasso.Picasso;
class CharityActivity : AppCompatActivity() {
    lateinit var descChar: Charity
    lateinit var ctx: Context
    var addedtofavs = false
    var fragdesc: CharityDescFragment? = null
    var fraggoal: CharityGoalsFragment? = null
    var fragforum: CharityForumFragment? = null
    lateinit var ivFavorite: ImageView

    /**
     * The [ViewPager] that will host the section contents.
     */
    var pager: ViewPager? = null
    var pagerAdapter: PagerAdapter? = null
    private val drawerlayout: DrawerLayout? = null
    var myGlobals: MyGlobals? = null
    lateinit var btnDonate: Button
    var bottomNavigationView: BottomNavigationView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        ctx = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charitydesc)
        Log.i("ActivityTrack", "entered charityactivity")
        val intent = intent
        fragdesc =
            CharityDescFragment()
        fraggoal =
            CharityGoalsFragment()
        fragforum =
            CharityForumFragment()
        btnDonate = findViewById(R.id.DonateButton)
        // TODO: Получать еще ссылку оплаты
        descChar = Charity(
            intent.getStringExtra("firestoreID"),
            intent.getStringExtra("chname"),
            intent.getStringExtra("bdesc"),
            intent.getStringExtra("fdesc"),
            intent.getFloatExtra("trust", 0f),
            intent.getIntExtra("image", 0),
            intent.getIntExtra("id", 0),
            intent.getStringExtra("url"),
            intent.getStringExtra("qiwiPaymentUrl")
        )

        btnDonate.setOnClickListener {
            logAnalyticsEvent(descChar!!.firestoreID);

            if (descChar.paymentUrl != "" && descChar!!.paymentUrl != null) {
                val intent1 = Intent(ctx, QiwiPaymentActivity::class.java)
                intent1.putExtra("firestoreID", descChar!!.firestoreID)
                intent1.putExtra("charityname", descChar!!.name)
                // String token = // TODO: Получать токен или ссылку для оплаты из бд
                intent1.putExtra("qiwiPaymentUrl", descChar!!.paymentUrl)
                startActivity(intent1)
            } else {
                Toast.makeText(
                    applicationContext,
                    R.string.no_payment_credentials_message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val tvName = findViewById<View>(R.id.tvName) as TextView
        val ivImage = findViewById<View>(R.id.ivImage) as ImageView
        if (!descChar!!.photourl.isEmpty()) {
            ivImage.setImageResource(R.drawable.ic_sync)
            Picasso.with(ctx).load(descChar!!.photourl).fit().into(ivImage)
        }
        val tvRating = findViewById<View>(R.id.tvTrustRating) as TextView
        tvName.text = descChar!!.name
        tvRating.text = descChar!!.trust.toString()
        pager = findViewById<View>(R.id.ViewPager) as ViewPager
        pagerAdapter = MyPagerAdapter(supportFragmentManager)
        pager!!.adapter = pagerAdapter
        pager!!.setOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                Log.d("TAG", "onPageSelected, position = $position")
            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }) //*/
        myGlobals = MyGlobals(ctx)
        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        myGlobals!!.setupBottomNavigation(ctx, this, bottomNavigationView!!)
        loadFavs()
        ivFavorite = findViewById(R.id.ivFavorite)
        ivFavorite.setOnClickListener(View.OnClickListener { onFavoriteClick() })
    }

    public override fun onResume() {
        super.onResume()
        myGlobals!!.setSelectedItem(this, bottomNavigationView!!)
    }

    fun loadFavs() {
        val rootRef = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val docIdRef = rootRef.collection("users").document(
            user!!.uid
        ).collection("favorites").document(descChar!!.firestoreID)
        docIdRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    addedtofavs = true
                    ivFavorite!!.setImageResource(R.drawable.ic_favorite_on)
                } else {
                    addedtofavs = false
                    ivFavorite!!.setImageResource(R.drawable.ic_favorite_off)
                }
            } else {
                Log.d("namechecker", "Failed with: ", task.exception)
            }
        }
    }

    fun onFavoriteClick() {
        if (addedtofavs) {
            addedtofavs = false
            ivFavorite!!.setImageResource(R.drawable.ic_favorite_off)
            val user = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user!!.uid).collection("favorites").document(
                descChar!!.firestoreID
            ).delete()
        } else {
            addedtofavs = true
            ivFavorite!!.setImageResource(R.drawable.ic_favorite_on)
            val user = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            val namemap = HashMap<String, Any>()
            namemap["name"] = descChar!!.name
            namemap["description"] = descChar!!.fullDescription
            namemap["photourl"] = descChar!!.photourl?:""
            db.collection("users").document(user!!.uid).collection("favorites").document(
                descChar!!.firestoreID
            ).set(namemap)
        }
    }

    private inner class MyPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        override fun getItem(pos: Int): Fragment {
            return when (pos) {
                0 -> CharityDescFragment.newInstance(descChar)
                1 -> CharityGoalsFragment.newInstance(descChar)
                else -> CharityDescFragment.newInstance(descChar)
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.i("ProgressTracker", "position a")
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            android.R.id.home -> {
                drawerlayout!!.openDrawer(GravityCompat.START)
                return true // manage other entries if you have it ...
            }
            R.id.action_search -> {
                Toast.makeText(this@CharityActivity, "Menu action clicked", Toast.LENGTH_LONG)
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class DownloadImageTask(var bmImage: ImageView) :
        AsyncTask<String?, Void?, Bitmap?>() {
        protected override fun doInBackground(vararg urls: String?): Bitmap? {
            val urldisplay = urls[0]
            var mIcon11: Bitmap? = null
            try {
                val `in` = URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message!!)
                e.printStackTrace()
            }
            return mIcon11
        }

        override fun onPostExecute(result: Bitmap?) {
            bmImage.setImageBitmap(result)
        }
    }
}