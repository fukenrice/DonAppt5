package com.example.donappt5.paymentsstuff;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

public class CardSubmitActivity extends AppCompatActivity {
    EditText etamount;
    Button btnSubmitCard;
    Button btnSubmitPayment;
    Context ctx;
    Spinner dropdown;
    private Stripe stripe;

    //private ActivityConnectWithStripeBinding viewBinding;
    private String paymentIntentClientSecret;
    private String state = "asdflkj"; // generate a unique value for this
    //private String clientId = "ca_32D88BD1qLklliziD7gYQvctJIhWBSQ7"; // the client ID found in your platform settings
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
                if ((connected_account_id != null) && (connected_account_id != "none")) {
                    Log.d("stripeconnacc", "2connected acc id != null but " + connected_account_id);
                    btnConnectStripe.setVisibility(View.GONE);
                    stripe = new Stripe(
                            ctx,
                            PaymentConfiguration.getInstance(ctx).getPublishableKey(),
                            connected_account_id
                    );
                }
            }
        });
        if ((connected_account_id != null) && (connected_account_id != "none")) {
            Log.d("stripeconnacc", "connected acc id != null but " + connected_account_id);
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
        startCheckout();

        //WeakReference<CardSubmitActivity> weakActivity = new WeakReference<>(this);
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


    private void startCheckout() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String endpointUrl = "https://donapp-d2378.firebaseapp.com/create-payment-intent-customer"
                + "?currency=" + "usd"
                + "&amount=" + "228322"
                + "&destination_id=" + user.getUid();
        Log.d("stripeasync", "executing " + endpointUrl);
        new RequestTask().execute(endpointUrl);
        Log.d("stripeasync", "executing in process ");
        //https://donapp-d2378.firebaseapp.com/create-payment-intent-customer?currency=usd&amount=132&destination_id=EUNOaNRQfyYlAummUev37EKg2qH3

        // Create a PaymentIntent by calling the sample server's /create-payment-intent endpoint.
        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.btnPayStripeConnect);
        payButton.setOnClickListener((View view) -> {
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                Log.d("stripepayment", "paramsnotnull " + paymentIntentClientSecret + "/" );
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                stripe.confirmPayment(this, confirmParams);
            }
        });
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            //Toast.makeText(ctx, "request begin", Toast.LENGTH_SHORT).show();
            // make a get request to uri[0] and return string
            throw new UnsupportedOperationException("not yet implemented");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.e("stripetokenresult", "result: " + result);
            Toast.makeText(ctx, "request result: " + result, Toast.LENGTH_LONG).show();
            paymentIntentClientSecret = result;
            //Do anything with response..
        }
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<CardSubmitActivity> activityRef;

        PaymentResultCallback(@NonNull CardSubmitActivity activity) {
            Log.d("stripepaycback", "PaymentResultCallback constructor");
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            Log.d("stripe payments", "payment onSuccess");
            final CardSubmitActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                //Toast.makeText(ctx, "Payment completed", Toast.LENGTH_LONG).show();
                Log.d("Stripe async", "payment succeded");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert(
                        "Payment completed",
                        gson.toJson(paymentIntent),
                        true
                );
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed
                Log.d("Stripe async", "payment failed");
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage(),
                        false
                );
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final CardSubmitActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString(), false);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("stripe payments", "stripe confirming payment");
        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    void displayAlert(String log, String mes, boolean wha) {
        Log.d(log + " stripe", mes);
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
