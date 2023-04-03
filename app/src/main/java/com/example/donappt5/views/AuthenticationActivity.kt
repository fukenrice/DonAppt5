package com.example.donappt5.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.donappt5.data.model.User
import com.example.donappt5.data.util.Status
import com.example.donappt5.util.MyGlobals
import com.example.donappt5.viewmodels.AuthenticationViewModel
import com.example.donappt5.views.charitylist.CharityListActivity
import com.example.donappt5.views.onboarding.OnBoardingActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AuthenticationActivity : AppCompatActivity() {
    var ctx: Context? = null
    var RC_SIGN_IN = 57
    lateinit var viewModel: AuthenticationViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        ctx = this
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        viewModel.user.observe(this) {
            if (it.status == Status.SUCCESS) {
                val intent = Intent(ctx, OnBoardingActivity::class.java)
                startActivity(intent)
            }
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val intent = Intent(ctx, CharityListActivity::class.java)
            Toast.makeText(ctx, "welcome, " + user.displayName, Toast.LENGTH_LONG).show()
            startActivity(intent)
        } else {
            val providers = Arrays.asList(
                EmailBuilder().build(),
                GoogleBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                viewModel.onLoginActivityResult()
            }
        }
    }
}