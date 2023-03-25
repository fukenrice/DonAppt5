package com.example.donappt5.CharityDescriptionFragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.donappt5.FriendsAdapter;
import com.example.donappt5.MessagesAdapter;
import com.example.donappt5.R;
import com.example.donappt5.helpclasses.Charity;
import com.example.donappt5.helpclasses.Friend;
import com.example.donappt5.helpclasses.Message;
import com.example.donappt5.helpclasses.MyGlobals;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class CharityForumFragment extends Fragment {
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;
    ListView lvMessages;
    Context ctx;
    static Charity dchar;
    Button btnAddMessage;
    EditText etMessage;
    TextView tvName;
    ImageView ivCommentator;
    String photourlfromstore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("progresstracker", "entered charity forum");
        View v = inflater.inflate(R.layout.fragment_charityforum, container, false);
        Log.d("progresstracker", "entered charity forum");
        ctx = getContext();
        lvMessages = v.findViewById(R.id.lvMessages);
        messages = new ArrayList<Message>();
        messagesAdapter = new MessagesAdapter(ctx, messages);
        MyGlobals mg = new MyGlobals(ctx);
        lvMessages.setAdapter(messagesAdapter);
        loadMessages();
        btnAddMessage = v.findViewById(R.id.btnAddMessage);
        btnAddMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMessage();
            }
        });
        etMessage = v.findViewById(R.id.etMessage);
        tvName = v.findViewById(R.id.tvName);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        tvName.setText(user.getDisplayName());
        ivCommentator = v.findViewById(R.id.ivCommentator);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    photourlfromstore = document.getString("photourl");
                    Log.d("photourlchat", photourlfromstore);
                    Picasso.with(ctx).load(photourlfromstore).into(ivCommentator);
                } else {
                    Log.d("fuck", "get failed with ", task.getException());
                }
            }
        });
        return v;
    }

    void createMessage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String msgid = getAlphaNumericString(16);
        String datetime = Calendar.getInstance().getTime().toString();
        Message creatingmessage = new Message(msgid, user.getUid(), user.getDisplayName(), photourlfromstore, etMessage.getText().toString(), datetime);
        messagesAdapter.objects.add(creatingmessage);
        messagesAdapter.notifyDataSetChanged();
        lvMessages.setSelection(messagesAdapter.getCount() - 1);
        HashMap<String, Object> msgmap = new HashMap<String, Object>();
        msgmap.put("messageid", msgid);
        msgmap.put("profileurl", photourlfromstore);
        msgmap.put("comment", etMessage.getText().toString());
        msgmap.put("datetime", datetime);
        msgmap.put("userid", user.getUid());
        msgmap.put("username", user.getDisplayName());
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("charities").document(dchar.firestoreID).collection("forum").document(msgid).set(msgmap);
        etMessage.setText("");
    }

    void loadMessages() { //TODO add pagination, why not?
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("charities").document(dchar.firestoreID).collection("forum").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot document : task.getResult()) {
                    Message loadedmessage = new Message();
                    loadedmessage.messageid = document.getId();
                    loadedmessage.profileurl = (String)document.get("profileurl");
                    loadedmessage.comment = (String)document.get("comment");
                    loadedmessage.datetime= (String)document.get("datetime");
                    loadedmessage.userid = (String)document.get("userid");
                    loadedmessage.username = (String)document.get("username");
                    messagesAdapter.objects.add(loadedmessage);
                    messagesAdapter.notifyDataSetChanged();
                    lvMessages.setSelection(messagesAdapter.getCount() - 1);
                }
            }
        });
    }

    public static CharityForumFragment newInstance(Charity given) {

        CharityForumFragment f = new CharityForumFragment();
        dchar = given;
        Bundle b = new Bundle();
        b.putString("firestoreID", given.firestoreID);
        b.putString("name", given.name);
        b.putString("bdesc", given.briefDescription);
        b.putString("fdesc", given.fullDescription);
        b.putFloat("trust", given.trust);
        b.putInt("id", given.id);
        b.putInt("img", given.image);

        f.setArguments(b);

        return f;
    }

    static String getAlphaNumericString(int n)
    {
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }
}
