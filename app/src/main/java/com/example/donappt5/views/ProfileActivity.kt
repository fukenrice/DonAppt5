package com.example.donappt5.views
//import com.squareup.picasso.Picasso;
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.donappt5.R
import com.example.donappt5.data.services.FirestoreService
import com.example.donappt5.data.util.Status
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.viewmodels.ProfileViewModel
import com.example.donappt5.views.charitylist.CharityListActivity
import com.example.donappt5.views.onboarding.OnBoardingActivity
import com.facebook.login.LoginManager
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    lateinit var btnLogOut: Button
    lateinit var ctx: Context
    lateinit var btnLoadProfile: Button
    lateinit var btnFavs: Button
    lateinit var tvUserName: TextView
    lateinit var btnChangeName: Button
    lateinit var viewModel: ProfileViewModel
    var ivProfile: ImageView? = null
    var SELECT_PICTURE = 12341
    var bottomNavigationView: BottomNavigationView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_profile)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        ctx = this
        btnChangeName = findViewById(R.id.btnChangeName)
        btnChangeName.setOnClickListener { requestNameChange() }
        btnLogOut = findViewById(R.id.btnLogOut)

        with(btnLogOut) {
            setOnClickListener { v: View? ->
                LoginManager.getInstance().logOut()
                AuthUI.getInstance()
                    .signOut(ctx)
                    .addOnCompleteListener { task: Task<Void?>? ->
                        val intent = Intent(
                            ctx,
                            AuthenticationActivity::class.java
                        )
                        startActivity(intent)
                    }
            }
        }

        ivProfile = findViewById(R.id.ivProfilePhoto)
        tvUserName = findViewById(R.id.tvUserName)

        val user = FirebaseAuth.getInstance().currentUser

        viewModel.photourl.observe(this) {
            if (it.status == Status.SUCCESS) {
                if (it.data != null) {
                    Picasso.with(ctx).load(it.data).fit().into(ivProfile)
                } else {
                    if (user!!.photoUrl != null) {
                        Picasso.with(ctx).load(user.photoUrl.toString()).fit().into(ivProfile)
                    }
                }
            }
        }

        val llwithimage = findViewById<LinearLayout>(R.id.llImage)
        llwithimage.setOnClickListener { loadImage() }
        btnLoadProfile = findViewById(R.id.btnLoadProfile)
        btnLoadProfile.setOnClickListener { FirestoreService.uploadImage(viewModel.loadedUri) }
        btnFavs = findViewById(R.id.btnFavs)
        btnFavs.setOnClickListener { onFavsClick() }

        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        MyGlobals(ctx).setupBottomNavigation(ctx, this, bottomNavigationView!!)

        val btnGoToOnboarding = findViewById<Button>(R.id.btnGoToOnboarding)
        btnGoToOnboarding.setOnClickListener {
            val intent = Intent(ctx, OnBoardingActivity::class.java)
            startActivity(intent)
        }
    }

    fun onFavsClick() {
        val intent = Intent(ctx, CharityListActivity::class.java)
        intent.putExtra("fillingfavorites", true)
        startActivity(intent)
    }

    fun loadImage() {
        openImageChooser()
    }

    fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.i("ProgressTracker", "position a")
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    fun requestNameChange() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Title")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setTitle("Enter your username")
        builder.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val user = FirebaseAuth.getInstance().currentUser
            val ans = input.text.toString()
            val db = FirebaseFirestore.getInstance()
            val update =
                HashMap<String, Any>()
            update["name"] = ans
            tvUserName.text = ans
            db.collection("users").document(user!!.uid)
                .update(update)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri = data.data
                if (null != selectedImageUri) {
                    ivProfile!!.post {
                        ivProfile!!.setImageURI(selectedImageUri)
                        viewModel.loadedUri = selectedImageUri
                    }
                }
            }
        }
    }
}