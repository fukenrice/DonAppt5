package com.example.donappt5.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.donappt5.data.util.MyGlobals;
import com.example.donappt5.data.model.User;
import com.example.donappt5.views.charitylist.CharityListActivity;
import com.example.donappt5.views.onboarding.OnBoardingActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AuthenticationActivity extends AppCompatActivity {
    Context ctx;
    int RC_SIGN_IN = 57; //just a constant
    FirebaseFirestore db;
    public void onCreate(Bundle savedInstanceState) {
        ctx = this;
        super.onCreate(savedInstanceState);
// Create and launch sign-in intent

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(ctx, CharityListActivity.class);
            Toast.makeText(ctx, "welcome, " + user.getDisplayName(), Toast.LENGTH_LONG).show();
            startActivity(intent);
        }
        else {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().setPermissions(Arrays.asList("public_profile", "user_friends", "email")).build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private void initFaceBook() {
        CallbackManager callbackManager;
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//Show loading progress
                MyGlobals mg = new MyGlobals(ctx);
                mg.getFriendsList();
            }

            @Override
            public void onCancel() {
                Log.d("Facebook-Login-Cancel", "Cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook-Login-onError", exception.toString());
            }
        });
    }

    /**
     * This method allows you to login with Facebook with few Permissions.
     * Use {@link #loginWithFacebook()}
     *
     * @return Nothing.
     */
    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                db = FirebaseFirestore.getInstance();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DocumentReference docRef = db.collection("users").document(user.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("userlogin", "DocumentSnapshot data: " + document.getData());

                                    Toast.makeText(ctx, "welcome, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
                                    AccessToken token = AccessToken.getCurrentAccessToken();
                                    //handleFacebookAccessToken(token);
                                    initFaceBook();
                                    MyGlobals mg = new MyGlobals(ctx);
                                    List<String> fl = mg.getFriendsList();
                                    Log.d("friendslist", fl.toString());

                                    //param.putString("fields", "friendlist", "members");

                                    Intent intent = new Intent(ctx, OnBoardingActivity.class);
                                    startActivity(intent);
                                } else {
                                    User curuser = new User(user.getDisplayName(), user.getEmail(), user.getPhotoUrl(), user.getUid());

                                    Map<String, Object> usermap = new HashMap<>();
                                    if (curuser.username != null) {usermap.put("name", curuser.username);}
                                    if (curuser.email != null) {usermap.put("mail", curuser.email);}
                                    if (curuser.photoUrl != null) {usermap.put("photo", curuser.photoUrl.toString());}

                                    Log.d("enteringUser", curuser.username + curuser.email + curuser.photoUrl);

                                    db.collection("users").document(curuser.uid)
                                            .set(usermap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("Userlogin", "DocumentSnapshot successfully written!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("Userlogin", "Error writing document", e);
                                                }
                                            });
                                    Toast.makeText(ctx, "welcome, " + curuser.username, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ctx, OnBoardingActivity.class);
                                    startActivity(intent);
                                }
                            } else {
                                Log.d("Charitycreationlog", "get failed with ", task.getException());
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(ctx, "Something went wrong! User == null!", Toast.LENGTH_LONG).show();
                }

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Toast.makeText(ctx, "Something went wrong! Can't log in!", Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("facebookauth", "handleFacebookAccessToken:" + token);
        FirebaseAuth mAuth;

        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signin", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("signingfacebook", "signInWithCredential:failure", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }


}
