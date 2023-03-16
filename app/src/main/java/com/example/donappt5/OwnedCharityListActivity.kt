package com.example.donappt5

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.donappt5.databinding.ActivityOwnedCharityListBinding
import com.example.donappt5.helpclasses.Charity
import com.example.donappt5.helpclasses.MyGlobals
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class OwnedCharityListActivity : AppCompatActivity() {

    lateinit var binding: ActivityOwnedCharityListBinding
    lateinit var charAdapter: CharityAdapter
    var fillingData = false
    var lastVisible: DocumentSnapshot? = null
    private var preLast = 0
    var queryInput: String = ""
    lateinit var myGlobals: MyGlobals

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnedCharityListBinding.inflate(layoutInflater)
        myGlobals = MyGlobals(this)
        val ctx: Context = this
        myGlobals.setupBottomNavigation(ctx, this, binding.bottomNavigation)
        setupView()
    }

    override fun onResume() {
        super.onResume()
        charAdapter.objects.clear()
        lastVisible = null
        fillAllData()
    }

    private fun setupView() {
        val view = binding.root
        setContentView(view)

        handleIntent(intent)

        charAdapter = CharityAdapter(this, arrayListOf<Charity>())

        fillAllData()

        binding.apply {
            pullToRefresh.setOnRefreshListener {
                charAdapter.objects = ArrayList<Charity>()
                charAdapter.notifyDataSetChanged()
                fillAllData()
                pullToRefresh.isRefreshing = false

            }
            lvMain.isClickable = true
            lvMain.adapter = charAdapter
            lvMain.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
                override fun onScroll(
                    view: AbsListView,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    onMyScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)
                }
            })
        }

        binding.lvMain.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val clickedCharity = charAdapter.getCharity(position)
                Log.d(
                    "Click", "itemClick: position = " + position + ", id = "
                            + id + ", name = " + clickedCharity.name + "url = " + clickedCharity.photourl + ", payment url = " + clickedCharity.paymentUrl
                )
                val intent = Intent(this, CharityEditActivity::class.java)
                intent.putExtra("chname", clickedCharity.name)
                intent.putExtra("bdesc", clickedCharity.briefDescription)
                intent.putExtra("fdesc", clickedCharity.fullDescription)
                intent.putExtra("trust", clickedCharity.trust)
                intent.putExtra("image", clickedCharity.image)
                intent.putExtra("id", clickedCharity.id)
                intent.putExtra("url", clickedCharity.photourl)
                intent.putExtra("qiwiPaymentUrl", clickedCharity.paymentUrl)
                startActivity(intent)
            }
    }


    fun onMyScroll(
        lw: AbsListView, firstVisibleItem: Int,
        visibleItemCount: Int, totalItemCount: Int
    ) {
        when (lw.id) {
            R.id.lvMain -> {
                val lastItem = firstVisibleItem + visibleItemCount
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) {
                        Log.d("Last", "Last")
                        preLast = lastItem
//                        fillAllData()
                    }
                }
            }
        }
    }


    // генерируем данные для адаптера
    fun fillAllData() {
        if (fillingData) return
        fillingData = true
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val taggedquery = db.collection("charities").whereEqualTo("creatorid", user?.uid);
        if (lastVisible != null && charAdapter.objects.size >= 20) {
            taggedquery
                .startAfter(lastVisible!!)
                .limit(20)
                .get()
                .addOnSuccessListener(OnSuccessListener { documentSnapshots ->
                    var i = 0
                    if (documentSnapshots.size() == 0) return@OnSuccessListener
                    lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
                    for (document in documentSnapshots) {
                        Log.d("CharitylistLog", document.id + " => " + document.data)
                        val name = document.getString("name")
                        val desc = document.getString("description")
                        val url = document.getString("photourl")
                        val qiwiPaymentUrl = document.getString("qiwiurl")
                        Log.d(
                            "CharitylistLog",
                            "recieved: $name $desc $url $qiwiPaymentUrl"
                        )
                        charAdapter.objects.add(
                            Charity(
                                name,
                                desc!!.substring(0, Math.min(desc.length, 50)),
                                desc,
                                (-1.0).toFloat(),
                                R.drawable.ic_launcher_foreground,
                                i,
                                url,
                                qiwiPaymentUrl
                            )
                        )
                        charAdapter.notifyDataSetChanged()
                        i++
                    }
                    fillingData = false
                })
        } else {
            taggedquery
                .limit(20)
                .get()
                .addOnSuccessListener { documentSnapshots ->
                    if (documentSnapshots.size() == 0) {
                        return@addOnSuccessListener
                    }
                    var i = 0
                    lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
                    for (document in documentSnapshots) {
                        Log.d("CharitylistLog", document.id + " => " + document.data)
                        val name = document.getString("name")
                        val desc = document.getString("description")
                        val url = document.getString("photourl")
                        val qiwiPaymentUrl = document.getString("qiwiurl")
                        Log.d(
                            "CharitylistLog",
                            "recieved: $name $desc $url $qiwiPaymentUrl"
                        )
                        charAdapter.objects.add(
                            Charity(
                                name,
                                desc!!.substring(0, Math.min(desc.length, 50)),
                                desc,
                                (-1.0).toFloat(),
                                R.drawable.ic_launcher_foreground,
                                i,
                                url,
                                qiwiPaymentUrl
                            )
                        )
                        charAdapter.notifyDataSetChanged()
                        i++
                    }
                    fillingData = false
                }
        }
        charAdapter.notifyDataSetChanged()
    }

    fun doMySearch(querys: String) {
        Log.d("searchfunction", "input = $querys")
        queryInput = querys
        val db = FirebaseFirestore.getInstance()
        db.collection("charities").orderBy("name").startAt(querys).endAt(querys + "\uf8ff").get()
            .addOnSuccessListener { documentSnapshots ->
                var i = 0
                for (document in documentSnapshots) {
                    ++i
                    Log.d("CharitylistLog", document.id + " => " + document.data)
                    val name = document.getString("name")
                    val desc = document.getString("description")
                    val url = document.getString("photourl")
                    val qiwiPaymentUrl = document.getString("qiwiurl")
                    Log.d("CharitylistLog", "recieved: $name $desc $url")
                    charAdapter.objects.add(
                        Charity(
                            name,
                            desc!!.substring(0, Math.min(desc.length, 50)),
                            desc,
                            (-1.0).toFloat(),
                            R.drawable.ic_launcher_foreground,
                            i,
                            url,
                            qiwiPaymentUrl
                        )
                    )
                    charAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun handleIntent(intent: Intent) {
        Log.d("searchfunction", "intent: " + Intent.ACTION_SEARCH + " " + intent.action)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.d("searchfunction", "search")
            doMySearch(query!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isIconifiedByDefault = false // Do not iconify the widget; expand it by default
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            android.R.id.home -> {
                myGlobals?.drawerlayout?.openDrawer(GravityCompat.START)
                return true // manage other entries if you have it ...
            }
            R.id.action_search -> {
                Toast.makeText(
                    this@OwnedCharityListActivity,
                    "Menu action clicked",
                    Toast.LENGTH_LONG
                )
                    .show()
                //onSearchRequested();
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}