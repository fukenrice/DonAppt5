package com.example.donappt5.views.charitycreation
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.donappt5.R
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.services.FirestoreService.setCharityLocation
import com.example.donappt5.data.util.Status
import com.example.donappt5.databinding.ActivityCharitycreationBinding
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.util.Util.getRandomString
import com.example.donappt5.viewmodels.CharityEditViewModel
import com.example.donappt5.views.charitycreation.popups.ActivityConfirm
import com.example.donappt5.views.charitycreation.popups.LocatorActivity
import com.example.donappt5.views.charitycreation.popups.TagsActivity
import com.google.android.gms.tasks.Task
import com.google.common.primitives.Ints
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.koalap.geofirestore.GeoFire
import com.koalap.geofirestore.GeoLocation
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.HashMap

class CharityCreationActivity : AppCompatActivity() {
    lateinit var context: Context
    var SELECT_PICTURE = 2878
    var TAGS_COUNT = 6

    lateinit var binding: ActivityCharitycreationBinding
    var creatingChar: Charity? = null
    var cart = 0
    var ckids = 1
    var cpov = 2
    var csci = 3
    var cheal = 4
    var cedu = 5
    var ctags = BooleanArray(TAGS_COUNT)
    var pathtoimage: String? = null
    var fileUrl: String? = null
    var fragDesc: CharityCreateDesc? = null
    var fragCredentials: CharityCreatePaymentCredentials? = null
    var latitude = -1000.0
    var longitude = -1000.0
    lateinit var viewModel: CharityEditViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCharitycreationBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[CharityEditViewModel::class.java]
        context = this

        viewModel.charity = Charity()

        viewModel.imageUri.observe(this) {
            binding.ivChangeImage.post(Runnable {
                binding.ivChangeImage.setImageURI(it)
            })
        }
        setupObserver()
        setupView()
    }

    fun loadImage() {
        openImageChooser()
    }


    private fun setupObserver() {
        viewModel.edited.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(
                        this,
                        "Charity has been successfully edited.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {
                    Toast.makeText(
                        this,
                        "An error occurred while editing the charity.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("charityEdit", "setupObserverEdited: ${it.message}")
                }
            }
        }

        viewModel.tags.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(
                        this,
                        "Tags had been successfully edited.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    Log.d("charityEdit", "setupObserverTags: ${it.message}")
                }
            }
        }

        viewModel.deleted.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(
                        this,
                        "Charity has been successfully deleted.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    Log.d("charityEdit", "setupObserverDelete: ${it.message}")
                }
            }
        }

        viewModel.isNameFree.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data == true) {
                        binding.apply {
                            imgbtnNameCheck.setImageResource(R.drawable.ic_check_foreground)
                            tvCharityNameCheck.setText("charity with such name does not exist. You can create one!")
                            btnCreate.isClickable = true
                        }
                    } else {
                        binding.apply {
                            imgbtnNameCheck.setImageResource(R.drawable.ic_warning_foreground)
                            tvCharityNameCheck.setText("charity with such name already exists. If you are it's owner, you can change it's contents")
                            btnCreate.isClickable = false
                        }
                    }
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    Log.d("charityEdit", "setupObserverNameCheck: ${it.message}")
                }
            }
        }

    }

    private fun setupView() {
        val view = binding.root
        setContentView(view)

        fragDesc = CharityCreateDesc.newInstance("")
        fragCredentials = CharityCreatePaymentCredentials.newInstance("")

        binding.apply {
            ivChangeImage.setImageResource(R.drawable.ic_sync)

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
                    viewModel.checkCreationName(binding.etName.getText().toString())
                }
            })

            imgbtnNameCheck.setOnClickListener {
                viewModel.checkCreationName(
                    binding.etName.getText().toString()
                )
            }

            relLayoutImage.setOnClickListener {
                openImageChooser()
            }

            ChangePager.adapter =
                MyPagerAdapter(
                    supportFragmentManager
                )

            btnCreate.setOnClickListener {

                if (validateFields()) {
                    val intent = Intent(context, LocatorActivity::class.java)
                    intent.putExtra(
                        "headertext",
                        "Give us location of your charity, although not mandatory, it will help raise awareness in your local community. Hold on the marker and it."
                    )
                    intent.putExtra("btnaccept", "We are here")
                    intent.putExtra("btncancel", "Skip this step")
                    startActivityForResult(intent, 1)
                }
            }
        }
    }

    fun validateFields(): Boolean {
        if (binding.etName.text.toString().contains("/")) {
            Toast.makeText(
                context,
                "I am afraid your charity's name cannot contain '/' symbol",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (binding.etName.text.toString() == "") {
            Toast.makeText(
                this,
                "I am afraid your charity's name cannot be empty",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
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
            } else if (resultingactivity == "ActivityConfirm") {
                val result = data.getStringExtra("result")
                if (result == "confirmed") {
                    Log.d("progresstracker", "confirmedresult")
                    createCharity()
                }
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
                            val path = getPathFromURI(selectedImageUri)
                            pathtoimage = path
                            Log.i("imageloader", "Image Path : $path")
                            // Set the image in ImageView
                            binding.ivChangeImage.post {
                                binding.ivChangeImage.setImageURI(selectedImageUri)
                                viewModel.imageUri.value = selectedImageUri
                            }
                        }
                    }
                }
            }.start()
        }
    }

    fun createCharity() {
        val db = FirebaseFirestore.getInstance()
        db.collection("charities")
            .whereEqualTo("name", binding.etName!!.text.toString()).get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful && task.result.documents.size != 0) {
                    createCharityWithID(task.result.documents[0].id)
                } else {
                    createCharityWithID(getRandomString(28))
                }
            }
    }

    fun createCharityWithID(id: String?) {
        Log.d("progresstracker", "createCharity")
        creatingChar = Charity()
        creatingChar!!.name = binding.etName!!.text.toString()
        creatingChar!!.firestoreID = id!!
        creatingChar!!.fullDescription = fragDesc!!.text
        creatingChar!!.paymentUrl = fragCredentials!!.getText()
        creatingChar!!.briefDescription = creatingChar!!.fullDescription.substring(
            0, Ints.min(
                creatingChar!!.fullDescription.length, 50
            )
        )
        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()
        val charity: MutableMap<String, Any> = HashMap()
        val user = FirebaseAuth.getInstance().currentUser
        charity["name"] = creatingChar!!.name
        charity["description"] = creatingChar!!.fullDescription
        charity["qiwiurl"] = creatingChar!!.paymentUrl
        charity["creatorid"] = user!!.uid
        Log.d("storageprogresstracker", "-1")
        if (viewModel.imageUri.value != null) {
            Log.d("storageprogresstracker", "0")
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val file: Uri? = viewModel.imageUri.value //Uri.fromFile(new File(pathtoimage));
            val imgsref = storageRef.child("charities" + creatingChar!!.firestoreID + "/photo")
            val uploadTask = imgsref.putFile(file!!)
            uploadTask.addOnFailureListener { exception -> // Handle unsuccessful uploads
                Log.d("storageprogresstracker", "dam$exception")
            }.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d("storageprogresstracker", "1")
                storageRef.child("charities" + creatingChar!!.firestoreID + "/photo")
                    .downloadUrl.addOnSuccessListener { uri: Uri ->
                        // Got the download URL for 'users/me/profile.png'
                        Log.d("urlgetter", uri.toString())
                        fileUrl = uri.toString()
                        charity["photourl"] = fileUrl!!
                        db.collection("charities").document(creatingChar!!.firestoreID)
                            .set(charity)
                            .addOnSuccessListener { aVoid: Void? ->
                                setCharityLocation(
                                    creatingChar!!.firestoreID,
                                    GeoLocation(latitude, longitude)
                                )
                                putTags()
                            }
                    }
                    .addOnFailureListener { exception: Exception ->
                        // Handle any errors
                        Log.d("storageprogresstracker", "2$exception")
                    }
            }

            //Log.d("urlgetter", fileUrl);
        } else {
            Log.d("storageprogresstracker", "3")
            db.collection("charities").document(creatingChar!!.firestoreID)
                .set(charity)
                .addOnSuccessListener {
                    Log.d("Charitycreationlog", "DocumentSnapshot successfully written!")
                    setCharityLocation(
                        creatingChar!!.firestoreID,
                        GeoLocation(latitude, longitude)
                    )
                    putTags()
                }
                .addOnFailureListener { e: Exception? ->
                    Log.w(
                        "Charitycreationlog",
                        "Error writing document",
                        e
                    )
                }
        }
    }

    fun putTags() {
        val db = FirebaseFirestore.getInstance()
        val namemap: Map<String, Any> = HashMap()
        val tagsmap: MutableMap<String, Any> = HashMap()
        if (ctags[cart]) {
            db.collection("tags").document("art").collection("list")
                .document(creatingChar!!.firestoreID).set(namemap)
            tagsmap["art"] = true
        } else tagsmap["art"] = false
        if (ctags[cpov]) {
            db.collection("tags").document("poverty").collection("list")
                .document(creatingChar!!.firestoreID).set(namemap)
            tagsmap["poverty"] = true
        } else tagsmap["poverty"] = false
        if (ctags[cedu]) {
            db.collection("tags").document("education").collection("list")
                .document(creatingChar!!.firestoreID).set(namemap)
            tagsmap["education"] = true
        } else tagsmap["education"] = false
        if (ctags[csci]) {
            db.collection("tags").document("science&research").collection("list").document(
                creatingChar!!.firestoreID
            ).set(namemap)
            tagsmap["science&research"] = true
        } else tagsmap["science&research"] = false
        if (ctags[ckids]) {
            db.collection("tags").document("children").collection("list")
                .document(creatingChar!!.firestoreID).set(namemap)
            tagsmap["children"] = true
        } else tagsmap["children"] = false
        if (ctags[cheal]) {
            db.collection("tags").document("healthcare").collection("list")
                .document(creatingChar!!.firestoreID).set(namemap)
            tagsmap["healthcare"] = true
        } else tagsmap["healthcare"] = false
        db.collection("charities").document(creatingChar!!.firestoreID).update(tagsmap).addOnSuccessListener { finish() }
    }

    fun onTagsActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        ctags[cart] = data.getBooleanExtra("art", false)
        ctags[ckids] = data.getBooleanExtra("kids", false)
        ctags[cpov] = data.getBooleanExtra("poverty", false)
        ctags[csci] = data.getBooleanExtra("science&research", false)
        ctags[cheal] = data.getBooleanExtra("healthcare", false)
        ctags[cedu] = data.getBooleanExtra("education", false)
        val intent = Intent(context, ActivityConfirm::class.java)
        intent.putExtra("CancelButtonTitle", "go back to charity creation")
        intent.putExtra("ConfirmButtonTitle", "confirm and create charity")
        intent.putExtra("PopupText", "Create charity?")
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        intent.putExtra("width", (size.x.toDouble() * 0.9).toInt())
        intent.putExtra("height", (size.y.toDouble() * 0.5).toInt())
        startActivityForResult(intent, 2)
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
            Toast.makeText(context, "lat: $latitude long: $longitude", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "coordinates not given", Toast.LENGTH_SHORT).show()
        }
        val intent = Intent(context, TagsActivity::class.java)
        startActivityForResult(intent, 2)
    }

    private inner class MyPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        override fun getItem(pos: Int): Fragment {
            return when (pos) {
                0 -> fragDesc!!
                1 -> fragCredentials!!
                else -> fragDesc!!
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
                binding.activityCharitycreation.openDrawer(GravityCompat.START)
                return true // manage other entries if you have it ...
            }
            R.id.action_search -> {
                Toast.makeText(
                    this@CharityCreationActivity,
                    "Menu action clicked",
                    Toast.LENGTH_LONG
                ).show()
                val colref = FirebaseFirestore.getInstance().collection("userlocations")
                val geoFirestore = GeoFire(colref)
                val creatingtest = "Doc" + Random().nextInt()
                val docref = FirebaseFirestore.getInstance().collection("userlocations")
                    .document(creatingtest)
                val nameMap = HashMap<String, Any>()
                creatingChar?.name?.let { nameMap.put("name", it) }
                docref.set(nameMap)
                geoFirestore.setLocation(creatingtest, GeoLocation(55.8800407, 36.5754417))
                Log.d("geoquery", "setting$creatingtest")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SELECT_PICTURE -> {
                var i = 0
                while (i < permissions.size) {
                    val permission = permissions[i]
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        val showRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                        if (!showRationale) {
                            showSettingsAlert()
                        }
                    }
                    i++
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /* Choose an image from Gallery */
    fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    /* Get the real path from the URI */
    fun getPathFromURI(contentUri: Uri?): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri!!, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor.close()
        return res
    }

    private fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("App needs to access the Camera.")
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW"
        ) { dialog, which ->
            dialog.dismiss()
            //finish();
        }
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "SETTINGS"
        ) { dialog, which ->
            dialog.dismiss()
            openAppSettings(this@CharityCreationActivity)
        }
        alertDialog.show()
    }

    companion object {
        fun openAppSettings(context: Activity?) {
            if (context == null) {
                return
            }
            val i = Intent()
            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:" + context.packageName)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            context.startActivity(i)
        }
    }
}