package com.example.donappt5

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.donappt5.databinding.FragmentCharityListBinding
import com.example.donappt5.helpclasses.Charity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CharityListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharityListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCharityListBinding
    private lateinit var adapter: CharityAdapter
    var chars = ArrayList<Charity>()
    var fillingmode = 0
    private var preLast = 0
    var fillingData = false
    var lastVisible: DocumentSnapshot? = null
    var currentTag = "none"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCharityListBinding.inflate(inflater, container, false)
        val view = binding.root
        setupView()
        return view
    }

    private fun setupView() {
        adapter = CharityAdapter(context, chars)
        binding.apply {

            lvMain.isClickable = true
            lvMain.adapter = adapter
            lvMain.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
                override fun onScroll(
                    view: AbsListView,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    onMyScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)

                    // Refresh only when scrolled to the top
                    val refreshLayout = activity?.findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
                    val topRowVerticalPosition =
                        if (view.getChildCount() === 0) 0 else lvMain.getChildAt(
                            0
                        ).getTop()
                    if (refreshLayout != null) {
                        refreshLayout.setEnabled(topRowVerticalPosition == 0)
                    }
                }
            })

            lvMain.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    val clickedCharity: Charity = adapter.getCharity(position)
                    Log.d(
                        "Click", "itemClick: position = " + position + ", id = "
                                + id + ", name = " + clickedCharity.name + "url = " + clickedCharity.photourl + ", payment url = " + clickedCharity.paymentUrl
                    )
                    val intent = Intent(context, CharityActivity::class.java)
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
        fillData()
    }


    private fun onMyScroll(
        lw: AbsListView, firstVisibleItem: Int,
        visibleItemCount: Int, totalItemCount: Int
    ) {
        if (fillingmode == CharityListActivity.FILLING_FAVORITES) return
        when (lw.id) {
            R.id.lvMain -> {
                val lastItem = firstVisibleItem + visibleItemCount
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) {
                        //to avoid multiple calls for last item
                        Log.d("Last", "Last")
                        preLast = lastItem
                        fillData()
//                        Log.d("georad", fdistance.toString())
                    }
                }
            }
        }
    }

    fun fillData() {
        Log.d("fillingmode", fillingmode.toString())
        if (fillingmode == CharityListActivity.FILLING_ALPHABET) {
            fillAllData()
        } else if (fillingmode == CharityListActivity.FILLING_SEARCH) {
            return
        } else if (fillingmode == CharityListActivity.FILLING_DISTANCE) {
//            fillDistanceData()
        } else if (fillingmode == CharityListActivity.FILLING_FAVORITES) {
//            fillFavoritesData()
        }
    }


    fun fillAllData() {
        Log.d("listfrag", "filling started")
        if (fillingData) return
        fillingData = true
        val db = FirebaseFirestore.getInstance()
        var taggedquery: Query
        taggedquery = if (currentTag !== "none") {
            db.collection("charities").whereEqualTo(currentTag, true)
        } else {
            db.collection("charities")
        }

        if (lastVisible != null && adapter.objects.size >= 20) {
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
                        adapter.objects.add(
                            Charity(
                                name,
                                desc!!.substring(0, Math.min(desc.length, 50)),
                                desc,
                                -1f,
                                R.drawable.ic_launcher_foreground,
                                i,
                                url,
                                qiwiPaymentUrl
                            )
                        )
                        adapter.notifyDataSetChanged()
                        i++
                    }
                    fillingData = false
                }) //*/
        } else {
            taggedquery
                .limit(20)
                .get()
                .addOnSuccessListener { documentSnapshots ->
                    var i = 0
                    if (documentSnapshots.size() == 0) {
                        return@addOnSuccessListener
                    }
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
                        adapter.objects.add(
                            Charity(
                                name,
                                desc!!.substring(0, Math.min(desc.length, 50)),
                                desc,
                                -1f,
                                R.drawable.ic_launcher_foreground,
                                i,
                                url,
                                qiwiPaymentUrl
                            )
                        )
                        adapter.notifyDataSetChanged()
                        i++
                    }
                    fillingData = false
                }
        }
        adapter.notifyDataSetChanged()
        Log.d("listfrag", adapter.objects.size.toString())
        //chars.add(new Charity(recievedCharities.elementAt(0).name, recievedCharities.elementAt(0).name, "wha?", -1, R.drawable.ic_launcher_foreground, -1));
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CharityListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            CharityListFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
            }
    }
}