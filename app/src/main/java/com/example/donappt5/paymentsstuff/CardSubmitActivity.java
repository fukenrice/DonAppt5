package com.example.donappt5.paymentsstuff;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.donappt5.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CardSubmitActivity extends AppCompatActivity {
    EditText etnumber;
    EditText etccv;
    EditText etzip;
    EditText etamount;
    Button btnSubmitCard;
    Button btnSubmitPayment;
    Context ctx;
    Spinner dropdown;
    ArrayAdapter<String> adapter;
    private Stripe stripe;

    //private ActivityConnectWithStripeBinding viewBinding;
    private String state = "asdflkj"; // generate a unique value for this
    private String clientId = "ca_32D88BD1qLklliziD7gYQvctJIhWBSQ7"; // the client ID found in your platform settings
    String connected_account_id = "none";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestcard);
        etamount = findViewById(R.id.etAmount);
        btnSubmitCard = findViewById(R.id.btnSubmitCard);
        btnSubmitPayment = findViewById(R.id.btnCharge);
        ctx = this;
        Button btnConnectStripe = findViewById(R.id.connect_with_stripe);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("stripe_customers").document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                connected_account_id = (String)documentSnapshot.get("connected_account_id");
            }
        });
        if (connected_account_id != null) {
            btnConnectStripe.setVisibility(View.GONE);
            stripe = new Stripe(
                    this,
                    PaymentConfiguration.getInstance(this).getPublishableKey(),
                    connected_account_id
            );
        }

        dropdown = findViewById(R.id.spinnerMethods);
        String[] array = {"undefined"};
        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(array));
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lst);
        dropdown.setAdapter(adapter);

        WeakReference<CardSubmitActivity> weakActivity = new WeakReference<>(this);
        btnSubmitCard.setOnClickListener((View view) -> {
            // Get the card details from the card widget
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            Card card = cardInputWidget.getCard();
            if (card != null) {
                // Create a Stripe token from the card details
                Stripe stripe = new Stripe(getApplicationContext(), PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey());
                stripe.createToken(card, new ApiResultCallback<Token>() {
                    @Override
                    public void onSuccess(@NonNull Token result) {
                        String tokenID = result.getId();
                        // Send the token identifier to the server...
                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Map<String, Object> tokenmap = new HashMap<>();
                        tokenmap.put("token", tokenID);
                        Log.d("tokenstripe", tokenID);
                        db.collection("stripe_customers").document(user.getUid()).collection("tokens").document(tokenID).set(tokenmap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("tokenstripe", "successfully written" + tokenID);
                                adapter.add(tokenID);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        Log.d("tokenstripe", "errorhier: " + e);
                    }
                });
            }
        });
        btnSubmitPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPayment();
            }
        });
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_GSMF14GK1NPKphtwTYRYl60W0083LGv2jw"
        );


        db.collection("stripe_customers").document(user.getUid()).collection("sources")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("sourcestripe", document.getId() + " => " + document.getData());
                                adapter.add(document.getId().toString());
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("tokenstripe", "Error getting documents: ", task.getException());
                        }
                    }
                });
        btnConnectStripe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String redirect = "https://donapp-d2378.firebaseapp.com/connect/oauth";

                String clientid = "ca_HJmhlTQk9zic5GGPiru0oXn269oZ5kBM";

                byte[] array = new byte[14];
                new Random().nextBytes(array);


                state = randomAlphaNumeric(14);
                Map<String, Object> creatingstate = new HashMap<>();
                creatingstate.put("state", state);

                db.collection("stripe_customers").document(user.getUid()).update(creatingstate);

                String url = "https://connect.stripe.com/oauth/authorize" +
                        "?response_type=" + "code" +
                        "&state=" + state +
                        "&client_id=" + clientid +
                        //"&stripe_user=" + user.getUid() +
                        "&scope=" + "read_write" +
                        "&redirect_uri=" + redirect;

                Log.d("tokenstripe", url);
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(v.getContext(), Uri.parse(url));
            }
        });
    }

    String BACKEND_URL = "https://donapp-d2378.firebaseapp.com/";
    private OkHttpClient httpClient = new OkHttpClient();
    private void startCheckout() {
        // Create a PaymentIntent by calling the sample server's /create-payment-intent endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = "{"
                + "\"currency\":\"usd\","
                + "\"items\":["
                + "{\"id\":\"photo_subscription\"}"
                + "]"
                + "}";
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));

        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.btnPayStripeConnect);
        payButton.setOnClickListener((View view) -> {
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                stripe.confirmPayment(this, confirmParams);
            }
        });
    }

    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<CardSubmitActivity> activityRef;

        PayCallback(@NonNull CardSubmitActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final CardSubmitActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final CardSubmitActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG
                        ).show()
                );
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private String paymentIntentClientSecret;
    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );

        // The response from the server includes the Stripe publishable key and
        // PaymentIntent details.
        // For added security, our sample app gets the publishable key from the server
        String stripePublishableKey = responseMap.get("publishableKey");
        paymentIntentClientSecret = responseMap.get("clientSecret");

        // Configure the SDK with your Stripe publishable key so that it can make requests to the Stripe API
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull(stripePublishableKey)
        );
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    String sourcecard;
    void submitPayment() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> paymentmap = new HashMap<>();
        String token;

        int amount = Integer.valueOf(etamount.getText().toString());
        paymentmap.put("amount", amount);
        paymentmap.put("customer_id", user.getUid());
        //paymentmap.put("token", dropdown.getSelectedItem().toString());
        db.collection("stripe_customers").document(user.getUid()).collection("sources")
                .document(dropdown.getSelectedItem().toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                sourcecard = (String)documentSnapshot.get("id");
            }
        });
        paymentmap.put("source", sourcecard);
        Log.d("tokenstripe", "paying" + user.getUid());
        db.collection("stripe_customers").document(user.getUid()).collection("charges").add(paymentmap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("tokenstripe", "succesfully written charge");
                etamount.setText("");
                Toast.makeText(ctx, "Payment succesful!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
