package com.example.donappt5

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.donappt5.CharityCreationFragments.CharityCreateDesc
import com.example.donappt5.CharityCreationFragments.CharityCreatePaymentCredentials
import com.example.donappt5.PopupActivities.LocatorActivity
import com.example.donappt5.PopupActivities.TagsActivity
import com.example.donappt5.databinding.ActivityCharityeditBinding
import com.example.donappt5.helpclasses.Charity
import com.example.donappt5.helpclasses.MyGlobals
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.koalap.geofirestore.GeoFire
import com.koalap.geofirestore.GeoLocation
import com.koalap.geofirestore.GeoQueryEventListener
import com.squareup.picasso.Picasso

class CharityEditActivity : AppCompatActivity() {


    //    lateinit var ctags: BooleanArray
    var ctags = mutableMapOf(
        "cart" to false,
        "ckids" to false,
        "cpov" to false,
        "csci" to false,
        "cheal" to false,
        "cedu" to false
    )
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    lateinit var descChar: Charity
    lateinit var binding: ActivityCharityeditBinding
    lateinit var ivPhotoEdit: ImageView
    lateinit var ivCheckName: ImageView
    lateinit var tvName: TextView
    lateinit var fragDesc: CharityCreateDesc
    lateinit var fragCredentials: CharityCreatePaymentCredentials
    lateinit var btnConfirm: Button
    lateinit var btnDelete: Button
    lateinit var btnChangeTags: Button
    lateinit var btnChangeLocation: Button

    //lateinit var pager: ViewPager
    var SELECT_PICTURE = 2878
    var imageUri: Uri? = null
    var loadedUri: Uri? = null
    var fileUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        descChar = Charity(
            intent.getStringExtra("chname"),
            intent.getStringExtra("bdesc"),
            intent.getStringExtra("fdesc"),
            intent.getFloatExtra("trust", 0f),
            intent.getIntExtra("image", 0),
            intent.getIntExtra("id", -1),
            intent.getStringExtra("url"),
            intent.getStringExtra("qiwiPaymentUrl")
        )

        var ctx: Context = this
        var myGlobals = MyGlobals(this)
        val bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        myGlobals.setupBottomNavigation(ctx, this, bottomNavigationView)
        binding = ActivityCharityeditBinding.inflate(layoutInflater)
        setupView(descChar)
    }

    private fun setupView(charity: Charity) {
        val view = binding.root
        setContentView(view)

        fragDesc = CharityCreateDesc.newInstance(charity.fullDescription)
        fragCredentials = CharityCreatePaymentCredentials.newInstance(charity.paymentUrl)

        binding.apply {
            ivChangeImage.setImageResource(R.drawable.ic_sync)
            Picasso.with(this@CharityEditActivity).load(charity.photourl).fit().into(ivChangeImage)

            etName.setText(charity.name)

            etName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    binding.imgbtnNameCheck.setImageResource(R.drawable.ic_sync)
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    checkName()
                }
            })

            imgbtnNameCheck.setOnClickListener { checkName() }

            relLayoutImage.setOnClickListener {
                openImageChooser()
            }

            ChangePager.adapter =
                MyPagerAdapter(supportFragmentManager, listOf(fragDesc, fragCredentials))

            btnConfirmChanges.setOnClickListener {
                btnConfirm()
            }

            btnDelete.setOnClickListener {
                deleteOrganization()
            }

            btnEditLocation.setOnClickListener {
                changeGeo()
            }

            btnEditTags.setOnClickListener {
                changeTags()
            }
        }
    }


    private class MyPagerAdapter(fm: FragmentManager, val fragmentList: List<Fragment>) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return fragmentList.count()
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

    }

    private fun checkName() {
        val checkingname: String = binding.etName.getText().toString()
        if (checkingname == "") return
        val rootRef = FirebaseFirestore.getInstance()
        val docIdRef = rootRef.collection("charities").document(checkingname)
        docIdRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document!!.exists() && checkingname != descChar.name) {
                    Log.d("namechecker", "Document exists!")
                    binding.apply {
                        imgbtnNameCheck.setImageResource(R.drawable.ic_warning_foreground)
                        tvCharityNameCheck.setText("charity with such name already exists. If you are it's owner, you can change it's contents")
                        btnConfirmChanges.setClickable(false)
                    }

                } else {
                    Log.d("namechecker", "Document does not exist!")
                    binding.apply {
                        imgbtnNameCheck.setImageResource(R.drawable.ic_check_foreground)
                        tvCharityNameCheck.setText("charity with such name does not exist. You can create one!")
                        btnConfirmChanges.setClickable(true)
                    }
                }
            } else {
                Log.d("namechecker", "Failed with: ", task.exception)
            }
        }
    }

    /* Choose an image from Gallery */
    fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    private fun validateFields(): Boolean {
        if (binding.etName.text.isEmpty()) {
            Toast.makeText(
                this,
                "I am afraid your charity's name cannot be empty",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        //GeoFire geoFire = new GeoFire(ref); //TODO geofire???
        //if(etName.getText().toString().contains(" ")) {
        //    Toast.makeText(context, "I am afraid your charity's name cannot contain space symbol", Toast.LENGTH_LONG).show();
        //    return;
        //}
        if (binding.etName.getText().toString().contains("/")) {
            Toast.makeText(
                this,
                "I am afraid your charity's name cannot contain '/' symbol",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // TODO: Проверять занятосьть имени, если занято, то не редактировать

        return true
    }

    private fun changeTags() {
        val intent = Intent(this, TagsActivity::class.java)
        startActivityForResult(intent, 2)
    }

    private fun changeGeo() {
        val intent = Intent(this, LocatorActivity::class.java)
        intent.putExtra(
            "headertext",
            "Give us location of your charity, although not mandatory, it will help raise awareness in your local community. Hold on the marker and it."
        )
        intent.putExtra("btnaccept", "We are here")
        intent.putExtra("btncancel", "Cancel")
        startActivityForResult(intent, 1)
    }

    private fun deleteOrganization() {
        val db = FirebaseFirestore.getInstance()
        db.collection("charities").document(descChar.name).delete()
        finish()
    }

    private fun btnConfirm() {
        if (validateFields()) {
            confirmChanges()
        }
    }

    private fun confirmChanges() {
        Log.d("progresstracker", "createCharity")

        var creatingChar = Charity()
        creatingChar.name = binding.etName.getText().toString()
        creatingChar.fullDescription = fragDesc.getText()
        creatingChar.paymentUrl = fragCredentials.getText()
        val db = FirebaseFirestore.getInstance()

        val charity: MutableMap<String, Any> = HashMap()
        val user = FirebaseAuth.getInstance().currentUser
        charity["name"] = creatingChar.name
        charity["description"] = creatingChar.fullDescription
        charity["qiwiurl"] = creatingChar.paymentUrl
        charity["creatorid"] = user!!.uid
        if (creatingChar.name == descChar.name) {

        } else {

        }


        Log.d("storageprogresstracker", "-1")
        if (imageUri != null) {
            Log.d("storageprogresstracker", "0")
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val file = loadedUri!! //Uri.fromFile(new File(pathtoimage));
            val imgsref = storageRef.child("charities" + creatingChar.name + "/photo")
            // TODO: Чистить папку перед добавлением фото(а может все само работает???)

            val uploadTask = imgsref.putFile(file)
            uploadTask.addOnFailureListener { exception -> // Handle unsuccessful uploads
                Log.d("storageprogresstracker", "dam$exception")
            }.addOnSuccessListener {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d("storageprogresstracker", "1")
                storageRef.child("charities" + creatingChar.name + "/photo").downloadUrl.addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                    Log.d("urlgetter", uri.toString())
                    fileUrl = uri.toString()
                    charity["photourl"] = fileUrl!!
                    db.collection("charities").document(descChar.name)
                        .set(charity).addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Information was successfully edited",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                    db.collection("charities").document(descChar.name).get()
                        .addOnSuccessListener { documentSnapshot ->

                            val data = documentSnapshot.data?.putAll(charity)
                            if (data != null) {
                                db.collection("charities").document(creatingChar.name).set(data).addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Information was successfully edited",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            if (charity["name"] != descChar.name) {
                                db.collection("charities").document(descChar.name).delete()
                            }
                            finish()
                        }
                }.addOnFailureListener { exception -> // Handle any errors
                    Log.d("storageprogresstracker", "2$exception")
                }
            }
        } else {
            Log.d("storageprogresstracker", "3")

            db.collection("charities").document(descChar.name).get()
                .addOnSuccessListener { documentSnapshot ->
                    val data = documentSnapshot.data
                    if (data != null) {
                        data.putAll(charity)
                    }
                    Log.d("mytag", documentSnapshot.data.toString())

                    if (data != null) {
                        db.collection("charities").document(creatingChar.name).set(data).addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Information was successfully edited",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener { e ->
                            Log.w(
                                "Charitycreationlog",
                                "Error writing document",
                                e
                            )
                        }
                    }
                    if (charity["name"] != descChar.name) {
                        db.collection("charities").document(descChar.name).delete()
                    }
                    finish()
                }
        }
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
                onLocatorActivityResult(requestCode, resultCode, data)
            } else if (resultingactivity == "TagsActivity") {
                onTagsActivityResult(requestCode, resultCode, data)
            }
        } else {
            Thread {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url from data
                        val selectedImageUri = data.data
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            imageUri = selectedImageUri
                            Log.i("imageloader", "Image URI : $imageUri")
                            // Set the image in ImageView
                            binding.ivChangeImage.post(Runnable {
                                binding.ivChangeImage.setImageURI(selectedImageUri)
                                loadedUri = selectedImageUri
                            })
                        }
                    }
                }
            }.start()
        }
    }

    protected fun onLocatorActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        val coordsgiven = data.getBooleanExtra("locationgiven", false)
        latitude = data.getDoubleExtra("latitude", 0.0)
        longitude = data.getDoubleExtra("longitude", 0.0)
        if (coordsgiven) {
            Toast.makeText(this, "lat: $latitude long: $longitude", Toast.LENGTH_LONG).show()
            putGeoQuery()
        } else {
            Toast.makeText(this, "coordinates not given", Toast.LENGTH_SHORT).show()
        }
    }

    fun onTagsActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        ctags["cart"] = data.getBooleanExtra("art", false)
        ctags["ckids"] = data.getBooleanExtra("kids", false)
        ctags["cpov"] = data.getBooleanExtra("poverty", false)
        ctags["csci"] = data.getBooleanExtra("science&research", false)
        ctags["cheal"] = data.getBooleanExtra("healthcare", false)
        ctags["cedu"] = data.getBooleanExtra("education", false)
        putTags()
    }


    fun putGeoQuery() {
        val db = FirebaseFirestore.getInstance()
        if (latitude > -990) {
            val location: MutableMap<String, Any> = java.util.HashMap()
            location["latitude"] = latitude
            location["longitude"] = longitude
            Log.d("geoquery", "Am I even here?4")
            db.collection("charities").document(descChar.name).collection("locations")
                .document("FirstLocation").set(location)
                .addOnSuccessListener {
                    Log.d("geoquery", "Am I even here?3")
                    val colref =
                        FirebaseFirestore.getInstance().collection("charities")
                            .document(descChar.name)
                            .collection("locations")
                    val geoFirestore = GeoFire(colref)
                    geoFirestore.setLocation(
                        "FirstLocation",
                        GeoLocation(latitude, longitude)
                    )
                    val colref2 = FirebaseFirestore.getInstance().collection("charitylocations")
                    val creatingdoc: Map<String, Any> =
                        java.util.HashMap()
                    colref2.document(descChar.name).set(creatingdoc)
                    val geoFirestore2 = GeoFire(colref2)
                    geoFirestore2.setLocation(
                        descChar.name,
                        GeoLocation(latitude, longitude)
                    )
                    Log.d("geoquery", "Am I even here?1")
                    val ref = FirebaseFirestore.getInstance().collection("userlocations")
                    val geoFireuserlocation = GeoFire(ref)
                    val geoQuery = geoFireuserlocation.queryAtLocation(
                        GeoLocation(
                            latitude,
                            longitude
                        ), 25.0
                    )
                    Log.d("geoquery", "Am I even here?2")
                    geoQuery.addGeoQueryEventListener(object :
                        GeoQueryEventListener {
                        override fun onKeyEntered(
                            key: String,
                            location: GeoLocation
                        ) {
                            println(
                                String.format(
                                    "Key %s entered the search area at [%f,%f]",
                                    key,
                                    location.latitude,
                                    location.longitude
                                )
                            )
                            Log.d("geoquery", "entereddoc:$key")
                            val notification = java.util.HashMap<String, Any>()
                            notification["notificationMessage"] = descChar.name
                            notification["notificationTitle"] = "new charity created nearby"
                            FirebaseFirestore.getInstance().collection("users")
                                .document(key).collection("Notifications")
                                .document(descChar.name).set(notification)
                        }

                        override fun onKeyExited(key: String) {
                            println(String.format("Key %s is no longer in the search area", key))
                            Log.d("geoquery", "exiteddoc:$key")
                            val notification = java.util.HashMap<String, Any>()
                            notification["notificationMessage"] = descChar.name
                            notification["notificationTitle"] = "new charity created nearby"
                            FirebaseFirestore.getInstance().collection("users")
                                .document(key).collection("Notifications")
                                .document(descChar.name).set(notification)
                        }

                        override fun onKeyMoved(
                            key: String,
                            location: GeoLocation
                        ) {
                            Log.d("geoquery", "moveddoc:$key")
                        }

                        override fun onGeoQueryReady() {
                            Log.d("geoquery", "ready")
                        }

                        override fun onGeoQueryError(exception: Exception) {
                            Log.d("geoquery", "dam:$exception")
                        }
                    })
                }
        }
    }

    fun putTags() {
        val db = FirebaseFirestore.getInstance()
        val namemap: Map<String, Any> = java.util.HashMap()
        val tagsmap: MutableMap<String, Any> = java.util.HashMap()
        if (ctags["cart"] == true) {
            db.collection("tags").document("art").collection("list").document(descChar.name)
                .set(namemap)
            tagsmap["art"] = true
        } else tagsmap["art"] = false
        if (ctags["cpov"] == true) {
            db.collection("tags").document("poverty").collection("list").document(descChar.name)
                .set(namemap)
            tagsmap["poverty"] = true
        } else tagsmap["poverty"] = false
        if (ctags["cedu"] == true) {
            db.collection("tags").document("education").collection("list")
                .document(descChar.name).set(namemap)
            tagsmap["education"] = true
        } else tagsmap["education"] = false
        if (ctags["csci"] == true) {
            db.collection("tags").document("science&research").collection("list")
                .document(descChar.name).set(namemap)
            tagsmap["science&research"] = true
        } else tagsmap["science&research"] = false
        if (ctags["ckids"] == true) {
            db.collection("tags").document("children").collection("list")
                .document(descChar.name).set(namemap)
            tagsmap["children"] = true
        } else tagsmap["children"] = false
        if (ctags["cheal"] == true) {
            db.collection("tags").document("healthcare").collection("list")
                .document(descChar.name).set(namemap)
            tagsmap["healthcare"] = true
        } else tagsmap["healthcare"] = false
        db.collection("charities").document(descChar.name).update(tagsmap)
    }
}
