package com.example.donappt5.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.donappt5.views.charitycreation.CharityCreateDesc
import com.example.donappt5.views.charitycreation.CharityCreatePaymentCredentials
import com.example.donappt5.views.charitycreation.popups.LocatorActivity
import com.example.donappt5.views.charitycreation.popups.TagsActivity
import com.example.donappt5.R
import com.example.donappt5.databinding.ActivityCharityeditBinding
import com.example.donappt5.data.model.Charity
import com.example.donappt5.data.util.Status
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.viewmodels.CharityEditViewModel
import com.squareup.picasso.Picasso

class CharityEditActivity : AppCompatActivity() {

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
    lateinit var viewModel: CharityEditViewModel
    lateinit var fragDesc: CharityCreateDesc
    lateinit var fragCredentials: CharityCreatePaymentCredentials

    //lateinit var pager: ViewPager
    var SELECT_PICTURE = 2878
    var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        descChar = Charity(
            intent.getStringExtra("firestoreID"),
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
        binding = ActivityCharityeditBinding.inflate(layoutInflater)
        var myGlobals = MyGlobals(this)
        myGlobals.setupBottomNavigation(ctx, this, binding.bottomNavigation)
        viewModel = ViewModelProvider(this)[CharityEditViewModel::class.java]
        viewModel.charity = descChar

        setupObserver()
        setupView(descChar)
    }

    override fun onResume() {
        super.onResume()
        var myGlobals = MyGlobals(this)
        myGlobals.setSelectedItem(this, binding.bottomNavigation)
    }

    private fun setupObserver() {
        viewModel.edited.observe(this, Observer {
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
                    Toast.makeText(this, "An error occurred while editing the charity.", Toast.LENGTH_SHORT).show()
                    Log.d("charityEdit", "setupObserverEdited: ${it.message}")
                }
            }
        })

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
                            btnConfirmChanges.setClickable(true)
                        }
                    } else {
                        binding.apply {
                            imgbtnNameCheck.setImageResource(R.drawable.ic_warning_foreground)
                            tvCharityNameCheck.setText("charity with such name already exists. If you are it's owner, you can change it's contents")
                            btnConfirmChanges.setClickable(false)
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

    private fun setupView(charity: Charity) {
        val view = binding.root
        setContentView(view)

        fragDesc = CharityCreateDesc.newInstance(charity.fullDescription)
        fragCredentials = CharityCreatePaymentCredentials.newInstance(charity.paymentUrl ?: "")

        binding.apply {
            ivChangeImage.setImageResource(R.drawable.ic_sync)
            if (!charity.photourl.isEmpty()) {
                Picasso.with(this@CharityEditActivity).load(charity.photourl).fit()
                    .into(ivChangeImage)
            }

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

                    viewModel.checkName(binding.etName.getText().toString())
                }
            })

            imgbtnNameCheck.setOnClickListener {
                viewModel.checkName(
                    binding.etName.getText().toString()
                )
            }

            relLayoutImage.setOnClickListener {
                openImageChooser()
            }

            ChangePager.adapter =
                MyPagerAdapter(supportFragmentManager, listOf(fragDesc, fragCredentials))

            btnConfirmChanges.setOnClickListener {
                btnConfirm()
            }

            btnDelete.setOnClickListener {
                viewModel.deleteCharity()
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

        if (binding.etName.getText().toString().contains("/")) {
            Toast.makeText(
                this,
                "I am afraid your charity's name cannot contain '/' symbol",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
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

    private fun btnConfirm() {
        if (validateFields()) {
            val charity: MutableMap<String, Any> = HashMap()
            charity["name"] = binding.etName.getText().toString()
            charity["description"] = fragDesc.getText()
            charity["qiwiurl"] = fragCredentials.getText()
            viewModel.editCharity(charity)
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
                onTagsActivityResult(data)
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
                            viewModel.imageUri.postValue(selectedImageUri)
                            Log.i("imageloader", "Image URI : $imageUri")
                            // Set the image in ImageView
                            binding.ivChangeImage.post(Runnable {
                                binding.ivChangeImage.setImageURI(selectedImageUri)
                            })
                        }
                    }
                }
            }.start()
        }
    }

    // TODO
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
        } else {
            Toast.makeText(this, "coordinates not given", Toast.LENGTH_SHORT).show()
        }
    }

    fun onTagsActivityResult(data: Intent) {
        val tags = mutableMapOf(
            "art" to false,
            "kids" to false,
            "poverty" to false,
            "science&research" to false,
            "healthcare" to false,
            "education" to false
        )
        tags["art"] = data.getBooleanExtra("art", false)
        tags["kids"] = data.getBooleanExtra("kids", false)
        tags["poverty"] = data.getBooleanExtra("poverty", false)
        tags["science&research"] = data.getBooleanExtra("science&research", false)
        tags["healthcare"] = data.getBooleanExtra("healthcare", false)
        tags["education"] = data.getBooleanExtra("education", false)
        viewModel.putTags(tags)
    }
}
